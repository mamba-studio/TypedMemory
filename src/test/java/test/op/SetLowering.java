package test.op;

import com.mamba.typedmemory.api.size;
import com.mamba.typedmemory.internal.layout.MemLayoutString;
import com.mamba.typedmemory.internal.ir.IRHelper;
import static com.mamba.typedmemory.internal.ir.IRHelper.CD_Objects_;
import java.lang.constant.ClassDesc;
import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.CD_long;
import static java.lang.constant.ConstantDescs.CD_void;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodType;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayDeque;
import com.mamba.typedmemory.api.MemLayout;
import java.lang.classfile.TypeKind;
import java.lang.invoke.VarHandle;
import test.op.LocalAllocator.AllocatedLocal;
import static test.op.expr.Exprs.longToInt;
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
import test.op.expr.ops.MulExpr;
import test.op.expr.values.ConstantExpr;
import test.op.expr.values.IntLiteralExpr;
import test.op.expr.values.LongLiteralExpr;
import test.op.expr.values.LocalExpr;
import test.op.stmt.BranchCondition;
import test.op.stmt.BlockStmt;
import test.op.stmt.CountedLoopStmt;
import test.op.stmt.EvalStmt;
import test.op.stmt.IfStmt;
import test.op.stmt.LabelStmt;
import test.op.stmt.SimpleStmt;
import test.op.stmt.ThrowStmt;

/**
 *
 * @author joemw
 * 
 * A nightmare to design this class
 * 
 */
public class SetLowering {
    
    record WriteContext(ClassDesc owner, Expr segmentExpr, Expr baseOffsetExpr) {}
    
    public static Stmt lower(Class<? extends Record> recordType, MemLayout memLayout, ClassDesc owner) {
        // method signature for set(long index, T t)
        var setType = MethodTypeDesc.ofDescriptor(
                MethodType.methodType(void.class, long.class, recordType).descriptorString());
        
        // for instance set(long index, T t):
        // this=0, index=1..2, t=3.
        // the allocator is only for fresh body locals after these parameters.
        var allocator = new LocalAllocator(false, setType); 
        var root = new LocalExpr(new AllocatedLocal(3, IRHelper.JVMType.REFERENCE, "t")); //let's put it on stack

        var segmentExpr = new GetFieldExpr(
                new LocalExpr(LocalAllocator.THIS),
                new FieldRef(owner, "segment", IRHelper.CD_MemorySegment));

        var baseOffsetExpr = new MulExpr( //index * STRIDE
                TypeKind.LONG,
                new LocalExpr(new AllocatedLocal(1, IRHelper.JVMType.LONG, "index")),
                new GetStaticFieldExpr(new FieldRef(owner, "STRIDE", CD_long)));

        var ctx = new WriteContext(owner, segmentExpr, baseOffsetExpr);

        var handleNames = new ArrayDeque<String>(MemLayoutString.of(memLayout).varHandleNames());

        var out = new ArrayList<Stmt>();

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
        for (var component : recordType.getRecordComponents()) {
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
                    varHandleSetMethodTypeDesc(type, coordinates.size()), out);
            return;
        }

        if (type.isRecord()) {
            lowerRecord(type, valueExpr, coordinates, ctx, allocator, handles, out);
            return;
        }

        if (type.isArray()) {
            var size = component.getAnnotation(size.class).value();
            lowerArray(type.getComponentType(), size, valueExpr,
                    coordinates, ctx, allocator, handles, out);
            return;
        }

        throw new UnsupportedOperationException();
    }

    private static void lowerArray(
            Class<?> elementType,  int fixedSize,  Expr arrayExpr, List<Expr> coordinates, WriteContext ctx,
            LocalAllocator allocator, Iterator<String> handles, List<Stmt> out) {
        
        allocator.enterScope();
        
        try {
            var iLocal = allocator.allocate(IRHelper.JVMType.LONG, "span0");
            var elemLocal = allocator.allocate(IRHelper.jvmType(elementType), "elem0");

            var accessKind = ArrayAccessKind.kind(elementType);

            var nestedCoords = new ArrayList<Expr>(coordinates);
            nestedCoords.add(new LocalExpr(iLocal));

            var nested = new ArrayList<Stmt>();
            if (elementType.isPrimitive()) {
                emitVarHandleSet(
                        ctx, handles.next(), nestedCoords, new LocalExpr(elemLocal),
                        varHandleSetMethodTypeDesc(elementType, nestedCoords.size()), nested);
            } else if (elementType.isRecord()) {
                lowerRecord(
                        elementType, new LocalExpr(elemLocal), nestedCoords, ctx,
                        allocator, handles, nested);
            } else if (elementType.isArray()) {
                throw new UnsupportedOperationException("array elements cannot themselves be arrays yet");
            } else {
                throw new UnsupportedOperationException("unsupported element type: " + elementType);
            }

            var loopBody = new ArrayList<Stmt>();
            loopBody.add(new SimpleStmt(emitter -> {
                new ArrayLoadExpr(
                            accessKind,
                            arrayExpr,
                            longToInt(new LocalExpr(iLocal))
                    ).emit(emitter);
                IRHelper.emitStore(emitter, elemLocal.kind(), elemLocal.slot());
            }));

            if (!elementType.isPrimitive()) {
                loopBody.add(new EvalStmt(requireNonNull(new LocalExpr(elemLocal))));
            }

            loopBody.add(new BlockStmt(nested));

            out.add(new CountedLoopStmt(
                    iLocal,
                    new LongLiteralExpr(fixedSize),
                    new BlockStmt(loopBody)
            ));
        } finally {
            allocator.exitScope();
        }
    }
    
    private static void emitVarHandleSet(
            WriteContext ctx,  String handleFieldName, List<Expr> coordinates, 
            Expr valueExpr, MethodTypeDesc setterType, List<Stmt> out){
        var handleExpr = new GetStaticFieldExpr(
                new FieldRef(ctx.owner(), handleFieldName, ClassDesc.ofDescriptor(VarHandle.class.descriptorString())));

        var args = new ArrayList<Expr>();
        args.add(ctx.segmentExpr());
        args.add(ctx.baseOffsetExpr());
        args.addAll(coordinates);
        args.add(valueExpr);

        var call = new InstanceMethodExpr(
                handleExpr, new MethodRef(ClassDesc.ofDescriptor(VarHandle.class.descriptorString()), "set", setterType),
                IRHelper.InvokeKind.VIRTUAL, args.toArray(Expr[]::new));

        out.add(new SimpleStmt(call::emit));
    }
    
    
    //store record components as variables in slots
    private static Map<String, LocalAllocator.AllocatedLocal> emitRecordComponentLocals(
            Class<?> recordClass, Expr root, LocalAllocator allocator, List<Stmt> out) {
        
        var locals = new LinkedHashMap<String, LocalAllocator.AllocatedLocal>();

        for (var component : recordClass.getRecordComponents()) {
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

    private static void emitRecordArrayLengthChecks(Class<?> recordClass, Expr root, List<Stmt> out) {
        for (var component : recordClass.getRecordComponents()) {
            var type = component.getType();
            var access = recordAccessor(root, component);

            if (type.isArray()) {
                size ann = component.getAnnotation(size.class);
                if (ann == null) {
                    throw new IllegalStateException(
                            "Missing @size on array component: " + component.getName()
                    );
                }

                out.add(lengthCheck(access, ann.value(), recordClass.getSimpleName() + "." + component.getName()));
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
            var type = component.getType();
            if (type.isPrimitive()) continue;

            var access = recordAccessor(root, component);
            out.add(new EvalStmt(requireNonNull(access)));

            if (type.isRecord()) {
                emitNestedRecordNullChecks(type, access, out);
            }
        }
    } 
    
    private static Expr recordAccessor(Expr receiver, RecordComponent component) {
        return new InstanceMethodExpr(
                    receiver,
                    MethodRef.recordAccessor(component),
                    IRHelper.InvokeKind.VIRTUAL
        );
    }
    
    private static Expr requireNonNull(Expr value) {
        return new StaticMethodExpr(
                new MethodRef(CD_Objects_, "requireNonNull", MethodTypeDesc.of(CD_Object, CD_Object)),
                false, value);
    }

    //this factors arrays too through coordinateCount
    private static MethodTypeDesc varHandleSetMethodTypeDesc(Class<?> valueType, int coordinateCount) {
        var params = new ClassDesc[2 + coordinateCount + 1];
        params[0] = IRHelper.CD_MemorySegment;
        params[1] = CD_long;

        for (int i = 0; i < coordinateCount; i++) {
            params[2 + i] = CD_long;
        }

        params[params.length - 1] = ClassDesc.ofDescriptor(valueType.descriptorString()); //final argument is actual value type
        return MethodTypeDesc.of(CD_void, params);
    }    
}
