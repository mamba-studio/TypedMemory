/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.op.expr.methods;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import java.lang.constant.ClassDesc;
import static java.lang.constant.ConstantDescs.INIT_NAME;
import java.lang.constant.MethodTypeDesc;
import test.op.Expr;

/**
 *
 * @author joemw
 */
public record ConstructorExpr(ClassDesc owner, MethodTypeDesc type, Expr... args) implements Expr {
    @Override
    public void emit(CodeEmitter out) {
        out.new_(owner);
        out.dup();
        for (Expr arg : args) {
            arg.emit(out);
        }
        out.invokespecial(owner, INIT_NAME, type);
    }
}
