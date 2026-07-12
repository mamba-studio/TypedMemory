package com.mamba.typedmemory.opcode.lowering;

import com.mamba.typedmemory.api.MemLayout;
import com.mamba.typedmemory.api.size;
import com.mamba.typedmemory.opcode.OpcodeHelper;
import com.mamba.typedmemory.util.MemLayoutString;
import com.mamba.typedmemory.opcode.ArrayAccessKind;
import com.mamba.typedmemory.opcode.LocalAllocator;
import com.mamba.typedmemory.opcode.LocalAllocator.LocalBinding;
import com.mamba.typedmemory.opcode.MemberRef.ConstructorRef;
import com.mamba.typedmemory.opcode.MemberRef.FieldRef;
import com.mamba.typedmemory.opcode.MemberRef.MethodRef;
import com.mamba.typedmemory.opcode.expr.Expr;
import com.mamba.typedmemory.opcode.expr.arrays.ArrayLengthExpr;
import com.mamba.typedmemory.opcode.expr.arrays.NewObjectArrayExpr;
import com.mamba.typedmemory.opcode.expr.arrays.NewPrimitiveArrayExpr;
import com.mamba.typedmemory.opcode.expr.numeric.NumericExpr;
import com.mamba.typedmemory.opcode.expr.ops.MulExpr;
import com.mamba.typedmemory.opcode.expr.values.LocalExpr;
import com.mamba.typedmemory.opcode.fields.GetFieldExpr;
import com.mamba.typedmemory.opcode.fields.GetStaticFieldExpr;
import com.mamba.typedmemory.opcode.fields.GetStaticNumericFieldExpr;
import com.mamba.typedmemory.opcode.methods.ConstructorExpr;
import com.mamba.typedmemory.opcode.methods.InstanceMethodExpr;
import com.mamba.typedmemory.opcode.stmt.ArrayStoreStmt;
import com.mamba.typedmemory.opcode.stmt.BlockStmt;
import com.mamba.typedmemory.opcode.stmt.CountedLoopStmt;
import com.mamba.typedmemory.opcode.stmt.SimpleStmt;
import com.mamba.typedmemory.opcode.stmt.Stmt;
import java.lang.constant.ClassDesc;
import static java.lang.constant.ConstantDescs.CD_VarHandle;
import static java.lang.constant.ConstantDescs.CD_long;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.RecordComponent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static com.mamba.typedmemory.opcode.expr.numeric.PrimitiveConversionExpr.longToIntExpr;
import com.mamba.typedmemory.opcode.expr.values.IntExpr.IntLiteralExpr;

public final class GetLowering {
    private GetLowering() {}

    record ReadContext(ClassDesc owner, Expr segmentExpr, Expr baseOffsetExpr) {}
    record LowerResult(List<Stmt> setup, Expr valueExpr, boolean materialised) {}

    public static Stmt lower(ClassDesc owner, Class<? extends Record> recordType, MemLayout memLayout) {
        // method signature for: T get(long index)
        var getType = MethodTypeDesc.of(ClassDesc.ofDescriptor(recordType.descriptorString()), CD_long);

        // allocator based on method signature
        var allocator = new LocalAllocator(false, getType);

        var segmentExpr = new GetFieldExpr(
                                new LocalExpr(LocalAllocator.THIS),
                                new FieldRef(owner, "segment", OpcodeHelper.CD_MemorySegment));

        var baseOffsetExpr = MulExpr.of(
                                    new LocalExpr(new LocalBinding(1, OpcodeHelper.JVMType.LONG, "index")),
                                    new GetStaticNumericFieldExpr(new FieldRef(owner, "STRIDE", CD_long)));
        
        // segmentExpr and baseOffsetExpr above will be used repeatedly hence let's store in readcontext
        var ctx = new ReadContext(owner, segmentExpr, baseOffsetExpr);
        
        var handles = new ArrayDeque<>(MemLayoutString.of(memLayout).varHandleNames()).iterator();

        var result = lowerRecord(recordType, List.of(), ctx, allocator, handles, true);

        var out = new ArrayList<Stmt>(result.setup());
        out.add(new SimpleStmt(result.valueExpr()::emit));
        return new BlockStmt(out);
    }

    static LowerResult lowerRecord(
            Class<?> recordType, List<Expr> coordinates,  ReadContext ctx,
            LocalAllocator allocator, Iterator<String> handles, boolean materialiseComponents) {
        var setup = new ArrayList<Stmt>();
        var args = new ArrayList<Expr>();

        for (var component : recordType.getRecordComponents()) {
            var part = lowerComponent(component, coordinates, ctx, allocator, handles);

            if (materialiseComponents) 
                part = materialiseToLocal(part, component.getType(), component.getName(), allocator);            

            setup.addAll(part.setup());
            args.add(part.valueExpr());
        }

        var ctor = new ConstructorExpr(
                new ConstructorRef(
                        ClassDesc.ofDescriptor(recordType.descriptorString()),
                        OpcodeHelper.constructorRecordTypeDesc(recordType.asSubclass(Record.class))),
                        args.toArray(Expr[]::new));

        return new LowerResult(setup, ctor, false);
    }

    static LowerResult lowerComponent(
            RecordComponent component, List<Expr> coordinates,
            ReadContext ctx, LocalAllocator allocator, Iterator<String> handles) {
        var type = component.getType();

        if (type.isPrimitive()) 
            return new LowerResult(List.of(),emitVarHandleGet(ctx, handles.next(), coordinates, type), false);
        
        if (type.isRecord()) 
            return lowerRecord(type, coordinates, ctx, allocator, handles, false);
        
        if (type.isArray()) {
            var annot = component.getAnnotation(size.class);
            if (annot == null)
                throw new IllegalStateException("Missing @size on array component: " + component.getName());
            
            return lowerArray(
                    type.getComponentType(), annot.value(), coordinates,
                    ctx, allocator, handles);
        }

        throw new UnsupportedOperationException("Unsupported component type: " + type);
    }

    static LowerResult lowerArray(
            Class<?> elementType, int fixedSize, List<Expr> coordinates, ReadContext ctx,
            LocalAllocator allocator, Iterator<String> handles) {
        var setup = new ArrayList<Stmt>();

        var arrayLocal = allocator.allocate(OpcodeHelper.JVMType.REFERENCE, "arr");
        var arrayExpr = new LocalExpr(arrayLocal);

        var newArrayExpr = elementType.isPrimitive()
                                ? new NewPrimitiveArrayExpr(OpcodeHelper.primitiveTypeKind(elementType), new IntLiteralExpr(fixedSize))
                                : new NewObjectArrayExpr(
                                        ClassDesc.ofDescriptor(elementType.descriptorString()),
                                        new IntLiteralExpr(fixedSize));

        setup.add(new SimpleStmt(emitter -> {
            newArrayExpr.emit(emitter);
            OpcodeHelper.emitStore(emitter, arrayLocal.kind(), arrayLocal.slot());
        }));

        allocator.enterScope();
        try {
            var indexLocal = allocator.allocate(OpcodeHelper.JVMType.LONG, "span0");

            var indexAsInt = longToIntExpr(new LocalExpr(indexLocal));

            var nestedCoords = new ArrayList<Expr>(coordinates);
            nestedCoords.add(new LocalExpr(indexLocal));

            LowerResult elementResult;
            if(elementType.isPrimitive()) {
                elementResult = 
                        new LowerResult(List.of(), emitVarHandleGet(ctx, handles.next(), nestedCoords, elementType), false);
            }
            else if(elementType.isRecord())
                elementResult = lowerRecord(elementType, nestedCoords, ctx, allocator, handles, false);           
            else if(elementType.isArray())
                throw new UnsupportedOperationException("nested arrays");            
            else
                throw new UnsupportedOperationException("Unsupported array element type: " + elementType);
            
            setup.add(new CountedLoopStmt(
                    indexLocal,
                    NumericExpr.asLong(new ArrayLengthExpr(arrayExpr)),
                    new BlockStmt(List.of(
                            new BlockStmt(elementResult.setup()),
                            new ArrayStoreStmt(
                                    ArrayAccessKind.kind(elementType),
                                    arrayExpr,
                                    indexAsInt,
                                    elementResult.valueExpr())))));
        } 
        finally {
            allocator.exitScope();
        }

        return new LowerResult(setup, arrayExpr, true);
    }

    static Expr emitVarHandleGet(ReadContext ctx, String handleFieldName, List<Expr> coordinates, Class<?> valueType) {
        var handleExpr = new GetStaticFieldExpr(new FieldRef(ctx.owner(), handleFieldName, CD_VarHandle));

        var args = new ArrayList<Expr>();
        args.add(ctx.segmentExpr());
        args.add(ctx.baseOffsetExpr());
        args.addAll(coordinates);

        return new InstanceMethodExpr(
                        handleExpr,
                        new MethodRef(
                            ClassDesc.ofDescriptor(java.lang.invoke.VarHandle.class.descriptorString()),
                            "get",
                            varHandleGetterType(valueType, coordinates.size())),
                        OpcodeHelper.InvokeKind.VIRTUAL,
                        args.toArray(Expr[]::new));
    }

    static MethodTypeDesc varHandleGetterType(Class<?> valueType, int coordinateCount) {
        var params = new ClassDesc[2 + coordinateCount];
        params[0] = OpcodeHelper.CD_MemorySegment;
        params[1] = CD_long;

        for (int i = 0; i < coordinateCount; i++) 
            params[2 + i] = CD_long;
        
        return MethodTypeDesc.of(ClassDesc.ofDescriptor(valueType.descriptorString()), params);
    }
    static LowerResult materialiseToLocal(
            LowerResult result, Class<?> type, String name, LocalAllocator allocator) {
        if (result.materialised()) 
            return result;

        var local = allocator.allocate(OpcodeHelper.jvmType(type), name);

        var setup = new ArrayList<Stmt>(result.setup());
        setup.add(new SimpleStmt(emitter -> {
            result.valueExpr().emit(emitter);
            OpcodeHelper.emitStore(emitter, local.kind(), local.slot());
        }));

        return new LowerResult(setup, new LocalExpr(local), true);
    }
}
