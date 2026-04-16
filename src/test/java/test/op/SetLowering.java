/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.op;

import com.mamba.typedmemory.api.size;
import com.mamba.typedmemory.internal.ir.IRHelper;
import static com.mamba.typedmemory.internal.ir.IRHelper.CD_Objects_;
import java.lang.constant.ClassDesc;
import static java.lang.constant.ConstantDescs.CD_Object;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodType;
import java.lang.reflect.RecordComponent;
import java.util.List;
import test.op.expr.arrays.ArrayLengthExpr;
import test.op.expr.methods.ConstructorExpr;
import test.op.expr.methods.InstanceMethodExpr;
import test.op.expr.methods.StaticMethodExpr;
import test.op.expr.values.ConstantExpr;
import test.op.expr.values.IntLiteralExpr;
import test.op.stmt.BlockStmt;
import test.op.stmt.BranchCondition;
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
    
    static void emitRecordArrayLengthChecks(Class<?> recordClass, Expr root, List<Stmt> out) {
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
    
    static Stmt lengthCheck(Expr arrayExpr, int expected, String label) {
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
                            ClassDesc.ofDescriptor(IllegalArgumentException.class.descriptorString()),
                            MethodTypeDesc.ofDescriptor(
                                    MethodType.methodType(void.class, String.class).descriptorString()
                            ),
                            new ConstantExpr(label + " length must be " + expected)
                    )
            ).emit(out);

            new LabelStmt(ok).emit(out);
        });
    }
      
    static void emitRecordNullChecks(Class<?> recordClass, Expr root, List<Stmt> out) {
        out.add(new EvalStmt(requireNonNull(root)));        
        emitNestedRecordNullChecks(recordClass, root, out);
    }
    
    static void emitNestedRecordNullChecks(Class<?> recordClass, Expr root, List<Stmt> out) {
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
    
    static Expr recordAccessor(Expr receiver, RecordComponent component) {
        return new InstanceMethodExpr(
                receiver,
                ClassDesc.ofDescriptor(component.getDeclaringRecord().descriptorString()),
                component.getName(),
                MethodTypeDesc.of(
                        ClassDesc.ofDescriptor(component.getType().descriptorString())
                ),
                IRHelper.InvokeKind.VIRTUAL
        );
    }
    
    static Expr requireNonNull(Expr value) {
        return new StaticMethodExpr(
                CD_Objects_,
                "requireNonNull",
                MethodTypeDesc.of(
                        CD_Object,
                        CD_Object
                ),
                false,
                value
        );
    }
}
