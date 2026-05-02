package test.op;

import com.mamba.typedmemory.api.MemLayout;
import com.mamba.typedmemory.api.size;
import com.mamba.typedmemory.internal.ir.IRHelper;
import com.mamba.typedmemory.internal.layout.MemLayoutString;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import static java.lang.constant.ConstantDescs.CD_VarHandle;
import static java.lang.constant.ConstantDescs.CD_long;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.RecordComponent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import test.op.LocalAllocator.AllocatedLocal;
import static test.op.expr.Exprs.arrayLengthAsLong;
import static test.op.expr.Exprs.longToInt;
import test.op.MemberRef.ConstructorRef;
import test.op.MemberRef.FieldRef;
import test.op.MemberRef.MethodRef;
import test.op.expr.arrays.NewObjectArrayExpr;
import test.op.expr.arrays.NewPrimitiveArrayExpr;
import test.op.expr.fields.GetFieldExpr;
import test.op.expr.fields.GetStaticFieldExpr;
import test.op.expr.methods.ConstructorExpr;
import test.op.expr.methods.InstanceMethodExpr;
import test.op.expr.ops.MulExpr;
import test.op.expr.values.IntLiteralExpr;
import test.op.expr.values.LocalExpr;
import test.op.stmt.ArrayStoreStmt;
import test.op.stmt.BlockStmt;
import test.op.stmt.CountedLoopStmt;
import test.op.stmt.ReturnRefStmt;
import test.op.stmt.SimpleStmt;

public final class GetLowering {
    private GetLowering() {}

    record ReadContext(ClassDesc owner, Expr segmentExpr, Expr baseOffsetExpr) {}
    record LowerResult(List<Stmt> setup, Expr valueExpr, boolean materialized) {}

    public static Stmt lower(Class<? extends Record> recordType, MemLayout memLayout, ClassDesc owner) {
        // method signature for: T get(long index)
        MethodTypeDesc getType = MethodTypeDesc.of(
                                        ClassDesc.ofDescriptor(recordType.descriptorString()),
                                        CD_long);

        // allocator based on method signature
        var allocator = new LocalAllocator(false, getType);

        var segmentExpr = new GetFieldExpr(
                                new LocalExpr(LocalAllocator.THIS),
                                new FieldRef(owner, "segment", IRHelper.CD_MemorySegment));

        var baseOffsetExpr = new MulExpr(
                                    TypeKind.LONG,
                                    new LocalExpr(new AllocatedLocal(1, IRHelper.JVMType.LONG, "index")),
                                    new GetStaticFieldExpr(new FieldRef(owner, "STRIDE", CD_long)));
        
        // segmentExpr and baseOffsetExpr above will be used repeatedly hence let's store in readcontext
        var ctx = new ReadContext(owner, segmentExpr, baseOffsetExpr);
        
        var handles = new ArrayDeque<>(MemLayoutString.of(memLayout).varHandleNames()).iterator();

        var result = lowerRecord(recordType, List.of(), ctx, allocator, handles, true);

        var out = new ArrayList<Stmt>(result.setup());
        out.add(new SimpleStmt(result.valueExpr()::emit));
        out.add(new ReturnRefStmt());

        return new BlockStmt(out);
    }

    static LowerResult lowerRecord(
            Class<?> recordType,
            List<Expr> coordinates,
            ReadContext ctx,
            LocalAllocator allocator,
            Iterator<String> handles,
            boolean materializeComponents
    ) {
        var setup = new ArrayList<Stmt>();
        var args = new ArrayList<Expr>();

        for (var component : recordType.getRecordComponents()) {
            LowerResult part = lowerComponent(component, coordinates, ctx, allocator, handles);

            if (materializeComponents) {
                part = materializeToLocal(
                        part,
                        component.getType(),
                        component.getName(),
                        allocator
                );
            }

            setup.addAll(part.setup());
            args.add(part.valueExpr());
        }

        Expr ctor = new ConstructorExpr(
                new ConstructorRef(
                    ClassDesc.ofDescriptor(recordType.descriptorString()),
                    IRHelper.constructorRecordTypeDesc((Class<? extends Record>) recordType)),
                args.toArray(Expr[]::new)
        );

        return new LowerResult(setup, ctor, false);
    }

    static LowerResult lowerComponent(
            RecordComponent component,
            List<Expr> coordinates,
            ReadContext ctx,
            LocalAllocator allocator,
            Iterator<String> handles
    ) {
        Class<?> type = component.getType();

        if (type.isPrimitive()) {
            return new LowerResult(
                    List.of(),
                    emitVarHandleGet(ctx, handles.next(), coordinates, type),
                    false
            );
        }

        if (type.isRecord()) {
            return lowerRecord(type, coordinates, ctx, allocator, handles, false);
        }

        if (type.isArray()) {
            size ann = component.getAnnotation(size.class);
            if (ann == null) {
                throw new IllegalStateException(
                        "Missing @size on array component: " + component.getName()
                );
            }

            return lowerArray(
                    type.getComponentType(),
                    ann.value(),
                    coordinates,
                    ctx,
                    allocator,
                    handles
            );
        }

        throw new UnsupportedOperationException("Unsupported component type: " + type);
    }

    static LowerResult lowerArray(
            Class<?> elementType,
            int fixedSize,
            List<Expr> coordinates,
            ReadContext ctx,
            LocalAllocator allocator,
            Iterator<String> handles
    ) {
        List<Stmt> setup = new ArrayList<>();

        LocalAllocator.AllocatedLocal arrayLocal =
                allocator.allocate(IRHelper.JVMType.REFERENCE, "arr");
        Expr arrayExpr = new LocalExpr(arrayLocal);

        Expr newArrayExpr = elementType.isPrimitive()
                ? new NewPrimitiveArrayExpr(IRHelper.primitiveTypeKind(elementType), new IntLiteralExpr(fixedSize))
                : new NewObjectArrayExpr(
                        ClassDesc.ofDescriptor(elementType.descriptorString()),
                        new IntLiteralExpr(fixedSize)
                );

        setup.add(new SimpleStmt(emitter -> {
            newArrayExpr.emit(emitter);
            IRHelper.emitStore(emitter, arrayLocal.kind(), arrayLocal.slot());
        }));

        allocator.enterScope();
        try {
            LocalAllocator.AllocatedLocal indexLocal =
                    allocator.allocate(IRHelper.JVMType.LONG, "span0");

            Expr indexAsInt = longToInt(new LocalExpr(indexLocal));

            List<Expr> nestedCoords = new ArrayList<>(coordinates);
            nestedCoords.add(new LocalExpr(indexLocal));

            LowerResult elementResult;
            if (elementType.isPrimitive()) {
                elementResult = new LowerResult(
                        List.of(),
                        emitVarHandleGet(ctx, handles.next(), nestedCoords, elementType),
                        false
                );
            } else if (elementType.isRecord()) {
                elementResult = lowerRecord(elementType, nestedCoords, ctx, allocator, handles, false);
            } else if (elementType.isArray()) {
                throw new UnsupportedOperationException("nested arrays");
            } else {
                throw new UnsupportedOperationException("Unsupported array element type: " + elementType);
            }

            setup.add(new CountedLoopStmt(
                    indexLocal,
                    arrayLengthAsLong(arrayExpr),
                    new BlockStmt(List.of(
                            new BlockStmt(elementResult.setup()),
                            new ArrayStoreStmt(
                                    arrayAccessKind(elementType),
                                    arrayExpr,
                                    indexAsInt,
                                    elementResult.valueExpr()
                            )
                    ))
            ));
        } finally {
            allocator.exitScope();
        }

        return new LowerResult(setup, arrayExpr, true);
    }

    static Expr emitVarHandleGet(
            ReadContext ctx,
            String handleFieldName,
            List<Expr> coordinates,
            Class<?> valueType
    ) {
        Expr handleExpr = new GetStaticFieldExpr(new FieldRef(ctx.owner(), handleFieldName, CD_VarHandle));

        List<Expr> args = new ArrayList<>();
        args.add(ctx.segmentExpr());
        args.add(ctx.baseOffsetExpr());
        args.addAll(coordinates);

        return new InstanceMethodExpr(
                handleExpr,
                new MethodRef(
                    ClassDesc.ofDescriptor(java.lang.invoke.VarHandle.class.descriptorString()),
                    "get",
                    varHandleGetterType(valueType, coordinates.size())),
                IRHelper.InvokeKind.VIRTUAL,
                args.toArray(Expr[]::new)
        );
    }

    static MethodTypeDesc varHandleGetterType(Class<?> valueType, int coordinateCount) {
        ClassDesc[] params = new ClassDesc[2 + coordinateCount];
        params[0] = IRHelper.CD_MemorySegment;
        params[1] = CD_long;

        for (int i = 0; i < coordinateCount; i++) {
            params[2 + i] = CD_long;
        }

        return MethodTypeDesc.of(
                ClassDesc.ofDescriptor(valueType.descriptorString()),
                params
        );
    }

    static ArrayAccessKind arrayAccessKind(Class<?> elementType) {
        if (!elementType.isPrimitive()) {
            return ArrayAccessKind.REFERENCE;
        }
        if (elementType == int.class) {
            return ArrayAccessKind.INT;
        }
        throw new UnsupportedOperationException(
                "Primitive array kind not supported yet: " + elementType
        );
    }
    
    static LowerResult materializeToLocal(
            LowerResult result,
            Class<?> type,
            String name,
            LocalAllocator allocator
    ) {
        if (result.materialized()) {
            return result;
        }

        LocalAllocator.AllocatedLocal local =
                allocator.allocate(IRHelper.jvmType(type), name);

        List<Stmt> setup = new ArrayList<>(result.setup());
        setup.add(new SimpleStmt(emitter -> {
            result.valueExpr().emit(emitter);
            IRHelper.emitStore(emitter, local.kind(), local.slot());
        }));

        return new LowerResult(setup, new LocalExpr(local), true);
    }
}
