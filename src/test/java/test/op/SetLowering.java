/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.op;

import com.mamba.typedmemory.internal.ir.IRHelper;
import java.lang.constant.ClassDesc;
import static java.lang.constant.ConstantDescs.CD_Object;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.RecordComponent;
import java.util.List;
import test.op.expr.methods.InstanceMethodExpr;
import test.op.expr.methods.StaticMethodExpr;
import test.op.stmt.EvalStmt;

/**
 *
 * @author joemw
 */
public class SetLowering {
    
      
    static void emitRecordNullChecks(Class<?> recordClass, Expr root, List<Stmt> out) {
        out.add(new EvalStmt(requireNonNull(root)));

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
                ClassDesc.of("java.util.Objects"),
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
