/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.op;

import com.mamba.typedmemory.api.size;
import com.mamba.typedmemory.internal.layout.MemLayoutString;
import com.mamba.typedmemory.internal.ir.IRHelper;
import static com.mamba.typedmemory.internal.ir.IRHelper.CD_Objects_;
import java.lang.constant.ClassDesc;
import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.CD_void;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodType;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayDeque;
import com.mamba.typedmemory.api.MemLayout;
import java.lang.classfile.TypeKind;
import java.lang.invoke.VarHandle;
import test.op.MemberRef.ConstructorRef;
import test.op.MemberRef.FieldRef;
import test.op.MemberRef.MethodRef;
import test.op.expr.arrays.ArrayLengthExpr;
import test.op.expr.arrays.ArrayLoadExpr;
import test.op.expr.fields.GetFieldExpr;
import test.op.expr.fields.GetStaticFieldExpr;
import test.op.expr.methods.ConstructorExpr;
import test.op.expr.methods.InstanceMethodExpr;
import test.op.expr.methods.StaticMethodExpr;
import test.op.expr.numeric.PrimitiveConversion;
import test.op.expr.numeric.PrimitiveConversionExpr;
import test.op.expr.ops.MulExpr;
import test.op.expr.values.ConstantExpr;
import test.op.expr.values.IntLiteralExpr;
import test.op.expr.values.LocalExpr;
import test.op.stmt.BranchCondition;
import test.op.stmt.BlockStmt;
import test.op.stmt.EvalStmt;
import test.op.stmt.IfStmt;
import test.op.stmt.LabelStmt;
import test.op.stmt.SimpleStmt;
import test.op.stmt.ThrowStmt;

/**
 *
 * @author joemw
 */
public class SetLowering {
    
    record WriteContext(ClassDesc owner, Expr segmentExpr, Expr baseOffsetExpr) {}
    
    public static Stmt lower(Class<? extends Record> recordType, MemLayout memLayout, ClassDesc owner) {
        // method signature for set
        var setType = MethodTypeDesc.ofDescriptor(
                MethodType.methodType(void.class, long.class, recordType).descriptorString());
        
        // for instance set(long index, T t):
        // this=0, index=1..2, t=3.
        // the allocator is only for fresh body locals after these parameters.
        var allocator = new LocalAllocator(false, setType); 
        var root = new LocalExpr(new LocalAllocator.AllocatedLocal(3, IRHelper.JVMType.REFERENCE, "t")); //let's put it on stack

        var segmentExpr = new GetFieldExpr(
                new LocalExpr(LocalAllocator.THIS),
                new FieldRef(owner, "segment", IRHelper.CD_MemorySegment)
        );

        var baseOffsetExpr = new MulExpr( //index * STRIDE
                TypeKind.LONG,
                new LocalExpr(new LocalAllocator.AllocatedLocal(
                        1, IRHelper.JVMType.LONG, "index"
                )),
                new GetStaticFieldExpr(new FieldRef(owner, "STRIDE", ClassDesc.ofDescriptor("J")))
        );

        WriteContext ctx = new WriteContext(owner, segmentExpr, baseOffsetExpr);

        Deque<String> handleNames =
                new ArrayDeque<>(MemLayoutString.of(memLayout).varHandleNames());

        List<Stmt> out = new ArrayList<>();

        emitRecordNullChecks(recordType, root, out);
        emitRecordArrayLengthChecks(recordType, root, out);
        lowerRecord(recordType, root, List.of(), ctx, allocator, handleNames.iterator(), out);

        return new BlockStmt(out);
    }
    
    private static void lowerRecord(Class<?> recordType, Expr recordExpr, List<Expr> coordinates,
            WriteContext ctx, LocalAllocator allocator, Iterator<String> handles, List<Stmt> out) {

        //store all record components in local slot
        var locals = emitRecordComponentLocals(recordType, recordExpr, allocator, out);

        //now let's lower values like set value via varhandle, but this now becomes recursive
        for (RecordComponent component : recordType.getRecordComponents()) {
            var componentExpr = new LocalExpr(locals.get(component.getName()));
            lowerValue(component, component.getType(), componentExpr,
                    coordinates, ctx, allocator, handles, out);
        }
    }
    
    private static void lowerValue(
            RecordComponent component, Class<?> type, Expr valueExpr, List<Expr> coordinates,
            WriteContext ctx, LocalAllocator allocator, Iterator<String> handles, List<Stmt> out) {
        //set value in segment via varhandle
        if (type.isPrimitive()) {
            emitVarHandleSet(ctx, handles.next(), coordinates, valueExpr,
                    varHandleSetterType(type, coordinates.size()), out);
            return;
        }

        if (type.isRecord()) {
            lowerRecord(type, valueExpr, coordinates, ctx, allocator, handles, out);
            return;
        }

        if (type.isArray()) {
            int size = component.getAnnotation(size.class).value();
            lowerArray(type.getComponentType(), size, valueExpr,
                    coordinates, ctx, allocator, handles, out);
            return;
        }

        throw new UnsupportedOperationException();
    }

    private static void lowerArray(
            Class<?> elementType,
            int fixedSize,
            Expr arrayExpr,
            List<Expr> coordinates,
            WriteContext ctx,
            LocalAllocator allocator,
            Iterator<String> handles,
            List<Stmt> out
    ) {
        allocator.enterScope();
        try {
            var iLocal = allocator.allocate(IRHelper.JVMType.LONG, "span0");
            var elemLocal = allocator.allocate(IRHelper.jvmType(elementType), "elem0");

            ArrayAccessKind accessKind = arrayAccessKind(elementType);

            List<Expr> nestedCoords = new ArrayList<>(coordinates);
            nestedCoords.add(new LocalExpr(iLocal));

            List<Stmt> nested = new ArrayList<>();
            if (elementType.isPrimitive()) {
                emitVarHandleSet(
                        ctx,
                        handles.next(),
                        nestedCoords,
                        new LocalExpr(elemLocal),
                        varHandleSetterType(elementType, nestedCoords.size()),
                        nested
                );
            } else if (elementType.isRecord()) {
                lowerRecord(
                        elementType,
                        new LocalExpr(elemLocal),
                        nestedCoords,
                        ctx,
                        allocator,
                        handles,
                        nested
                );
            } else if (elementType.isArray()) {
                throw new UnsupportedOperationException("array elements cannot themselves be arrays yet");
            } else {
                throw new UnsupportedOperationException("unsupported element type: " + elementType);
            }

            out.add(new SimpleStmt(emitter -> {
                var loopStart = emitter.newLabel();
                var loopDone = emitter.newLabel();

                emitter.lconst(0L);
                emitter.lstore(iLocal.slot());

                emitter.bind(loopStart);
                emitter.lload(iLocal.slot());
                emitter.lconst(fixedSize);
                emitter.lcmp();
                emitter.ifge(loopDone);

                Expr load = new ArrayLoadExpr(
                        accessKind,
                        arrayExpr,
                        new PrimitiveConversionExpr(
                                PrimitiveConversion.LONG_TO_INT,
                                new LocalExpr(iLocal)
                        )
                );

                load.emit(emitter);
                IRHelper.emitStore(emitter, elemLocal.kind(), elemLocal.slot());

                if (!elementType.isPrimitive()) {
                    requireNonNull(new LocalExpr(elemLocal)).emit(emitter);
                    emitter.pop();
                }

                new BlockStmt(nested).emit(emitter);

                emitter.lload(iLocal.slot());
                emitter.lconst(1L);
                emitter.ladd();
                emitter.lstore(iLocal.slot());
                emitter.goto_(loopStart);
                emitter.bind(loopDone);
            }));
        } finally {
            allocator.exitScope();
        }
    }
    
    private static void emitVarHandleSet(
            WriteContext ctx,  String handleFieldName, List<Expr> coordinates, 
            Expr valueExpr, MethodTypeDesc setterType, List<Stmt> out){
        Expr handleExpr = new GetStaticFieldExpr(
                new FieldRef(ctx.owner(), handleFieldName, ClassDesc.ofDescriptor(VarHandle.class.descriptorString())));

        List<Expr> args = new java.util.ArrayList<>();
        args.add(ctx.segmentExpr());
        args.add(ctx.baseOffsetExpr());
        args.addAll(coordinates);
        args.add(valueExpr);

        Expr call = new InstanceMethodExpr(
                handleExpr,
                new MethodRef(ClassDesc.ofDescriptor(VarHandle.class.descriptorString()), "set", setterType),
                IRHelper.InvokeKind.VIRTUAL,
                args.toArray(Expr[]::new)
        );

        out.add(new SimpleStmt(call::emit));
    }
    
    
    //store record components as variables in slots
    private static Map<String, LocalAllocator.AllocatedLocal> emitRecordComponentLocals(
            Class<?> recordClass, Expr root, LocalAllocator allocator, List<Stmt> out) {
        
        var locals = new LinkedHashMap<String, LocalAllocator.AllocatedLocal>();

        for (RecordComponent component : recordClass.getRecordComponents()) {
            var kind = IRHelper.jvmType(component.getType());
            var local = allocator.allocate(kind, component.getName());

            var access = recordAccessor(root, component);

            out.add(new SimpleStmt(emitter -> {
                access.emit(emitter);
                IRHelper.emitStore(emitter, local.kind(), local.slot());
            }));

            locals.put(component.getName(), local);
        }

        return locals;
    }

    private static void lowerRecord(
            Class<?> recordClass,
            Expr root,
            List<Expr> coordinates,
            ClassDesc owner,
            WriteContext ctx,
            LocalAllocator allocator,
            Iterator<String> handleNames,
            List<Stmt> out) {
        Map<String, LocalAllocator.AllocatedLocal> locals =
                emitRecordComponentLocals(recordClass, root, allocator, out);

        for (RecordComponent component : recordClass.getRecordComponents()) {
            Expr componentExpr = new LocalExpr(locals.get(component.getName()));
            lowerComponent(
                    component,
                    component.getType(),
                    componentExpr,
                    coordinates,
                    owner,
                    ctx,
                    allocator,
                    handleNames,
                    out
            );
        }
    }

    private static void lowerComponent(
            RecordComponent component,
            Class<?> type,
            Expr valueExpr,
            List<Expr> coordinates,
            ClassDesc owner,
            WriteContext ctx,
            LocalAllocator allocator,
            Iterator<String> handleNames,
            List<Stmt> out) {
        if (type.isPrimitive()) {
            emitVarHandleSet(
                    ctx,
                    handleNames.next(),
                    coordinates,
                    valueExpr,
                    varHandleSetterType(type, coordinates.size()),
                    out
            );
            return;
        }

        if (type.isRecord()) {
            lowerRecord(type, valueExpr, coordinates, owner, ctx, allocator, handleNames, out);
            return;
        }

        if (type.isArray()) {
            size ann = component.getAnnotation(size.class);
            if (ann == null) {
                throw new IllegalStateException(
                        "Missing @size on array component: " + component.getName()
                );
            }

            emitArrayLoop(
                    component.getName(),
                    type.getComponentType(),
                    ann.value(),
                    valueExpr,
                    coordinates,
                    owner,
                    ctx,
                    allocator,
                    handleNames,
                    out
            );
            return;
        }

        throw new UnsupportedOperationException("Unsupported component type: " + type);
    }

    private static void emitArrayLoop(
            String label,
            Class<?> elementType,
            int fixedSize,
            Expr arrayExpr,
            List<Expr> coordinates,
            ClassDesc owner,
            WriteContext ctx,
            LocalAllocator allocator,
            Iterator<String> handleNames,
            List<Stmt> out) {
        LocalAllocator.AllocatedLocal indexLocal = allocator.allocate(IRHelper.JVMType.LONG, label + "$idx");
        LocalAllocator.AllocatedLocal elemLocal = allocator.allocate(
                IRHelper.jvmType(elementType),
                label + "$elem"
        );

        ArrayAccessKind accessKind = arrayAccessKind(elementType);
        Expr arrayIndexExpr = new PrimitiveConversionExpr(
                PrimitiveConversion.LONG_TO_INT,
                new LocalExpr(indexLocal)
        );

        List<Stmt> loopBody = new ArrayList<>();
        Expr elementLoad = new ArrayLoadExpr(accessKind, arrayExpr, arrayIndexExpr);

        loopBody.add(new SimpleStmt(emitter -> {
            elementLoad.emit(emitter);
            IRHelper.emitStore(emitter, elemLocal.kind(), elemLocal.slot());
        }));

        Expr elemExpr = new LocalExpr(elemLocal);
        if (!elementType.isPrimitive()) {
            loopBody.add(new EvalStmt(requireNonNull(elemExpr)));
        }

        List<Expr> nestedCoordinates = new ArrayList<>(coordinates);
        nestedCoordinates.add(new LocalExpr(indexLocal));

        if (elementType.isPrimitive()) {
            emitVarHandleSet(
                    ctx,
                    handleNames.next(),
                    nestedCoordinates,
                    elemExpr,
                    varHandleSetterType(elementType, nestedCoordinates.size()),
                    loopBody
            );
        } else if (elementType.isRecord()) {
            lowerRecord(elementType, elemExpr, nestedCoordinates, owner, ctx, allocator, handleNames, loopBody);
        } else {
            throw new UnsupportedOperationException("Nested arrays not supported yet");
        }

        out.add(new SimpleStmt(emitter -> {
            var loopStart = emitter.newLabel();
            var loopDone = emitter.newLabel();

            emitter.lconst(0L);
            emitter.lstore(indexLocal.slot());

            emitter.bind(loopStart);
            emitter.lload(indexLocal.slot());
            emitter.lconst(fixedSize);
            emitter.lcmp();
            emitter.ifeq(loopDone);

            new BlockStmt(loopBody).emit(emitter);

            emitter.lload(indexLocal.slot());
            emitter.lconst(1L);
            emitter.ladd();
            emitter.lstore(indexLocal.slot());
            emitter.goto_(loopStart);
            emitter.bind(loopDone);
        }));
    }
    
    private static void emitRecordArrayLengthChecks(Class<?> recordClass, Expr root, List<Stmt> out) {
        for (var component : recordClass.getRecordComponents()) {
            Class<?> type = component.getType();
            Expr access = recordAccessor(root, component);

            if (type.isArray()) {
                size ann = component.getAnnotation(size.class);
                if (ann == null) {
                    throw new IllegalStateException(
                            "Missing @size on array component: " + component.getName()
                    );
                }

                out.add(lengthCheck(
                        access,
                        ann.value(),
                        recordClass.getSimpleName() + "." + component.getName()
                ));
            } else if (type.isRecord()) {
                emitRecordArrayLengthChecks(type, access, out);
            }
        }
    }
    
    private static Stmt lengthCheck(Expr arrayExpr, int expected, String label) {
        return new SimpleStmt(out -> {
            var ok = out.newLabel();

            new IfStmt(
                    BranchCondition.IF_ICMP_EQ,
                    new ArrayLengthExpr(arrayExpr),
                    new IntLiteralExpr(expected),
                    ok
            ).emit(out);

            new ThrowStmt(
                new ConstructorExpr(
                    new ConstructorRef(
                            ClassDesc.ofDescriptor(IllegalArgumentException.class.descriptorString()),
                            MethodTypeDesc.ofDescriptor(MethodType.methodType(void.class, String.class).descriptorString())),
                    new ConstantExpr(label + " length must be " + expected)
                )
            ).emit(out);

            new LabelStmt(ok).emit(out);
        });
    }
      
    private static void emitRecordNullChecks(Class<?> recordClass, Expr root, List<Stmt> out) {
        out.add(new EvalStmt(requireNonNull(root)));        
        emitNestedRecordNullChecks(recordClass, root, out);
    }
    
    private static void emitNestedRecordNullChecks(Class<?> recordClass, Expr root, List<Stmt> out) {
        for (var component : recordClass.getRecordComponents()) {
            Class<?> type = component.getType();
            if (type.isPrimitive()) continue;

            Expr access = recordAccessor(root, component);
            out.add(new EvalStmt(requireNonNull(access)));

            if (type.isRecord()) {
                emitNestedRecordNullChecks(type, access, out);
            }
        }
    } 
    
    private static Expr recordAccessor(Expr receiver, RecordComponent component) {
        return new InstanceMethodExpr(
                    receiver,
                    new MethodRef(
                        ClassDesc.ofDescriptor(component.getDeclaringRecord().descriptorString()),
                        component.getName(),
                        MethodTypeDesc.of(ClassDesc.ofDescriptor(component.getType().descriptorString()))),
                            IRHelper.InvokeKind.VIRTUAL
                    );
    }
    
    private static Expr requireNonNull(Expr value) {
        return new StaticMethodExpr(
                new MethodRef(CD_Objects_, "requireNonNull", MethodTypeDesc.of(CD_Object, CD_Object)),
                false,
                value);
    }

    private static MethodTypeDesc varHandleSetterType(Class<?> valueType, int coordinateCount) {
        ClassDesc[] params = new ClassDesc[2 + coordinateCount + 1];
        params[0] = IRHelper.CD_MemorySegment;
        params[1] = ClassDesc.ofDescriptor("J");

        for (int i = 0; i < coordinateCount; i++) {
            params[2 + i] = ClassDesc.ofDescriptor("J");
        }

        params[params.length - 1] = ClassDesc.ofDescriptor(valueType.descriptorString());
        return MethodTypeDesc.of(CD_void, params);
    }

    private static ArrayAccessKind arrayAccessKind(Class<?> elementType) {
        if (!elementType.isPrimitive()) {
            return ArrayAccessKind.REFERENCE;
        }
        if (elementType == int.class) {
            return ArrayAccessKind.INT;
        }
        throw new UnsupportedOperationException("Primitive array kind not supported yet: " + elementType);
    }
}
