/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.exprtest.expr.methods;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import test.exprtest.expr.Expr;

/**
 *
 * @author joemw
 */
public record StaticMethodExpr(ClassDesc owner, String name, MethodTypeDesc type, Expr... args) implements Expr {
    @Override
    public void emit(CodeEmitter out) {
        for (Expr arg : args) {
            arg.emit(out);
        }
        out.invokestatic(owner, name, type);
    }
}
