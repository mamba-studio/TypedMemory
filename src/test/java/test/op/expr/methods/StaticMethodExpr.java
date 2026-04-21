/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.op.expr.methods;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import test.op.Expr;
import test.op.MemberRef.MethodRef;

/**
 *
 * @author joemw
 */
public record StaticMethodExpr(MethodRef method, boolean isInterface, Expr... args) implements Expr {
    @Override
    public void emit(CodeEmitter out) {
        for (Expr arg : args) {
            arg.emit(out);
        }

        out.invokestatic(method.owner(), method.name(), method.type(), isInterface);
    }
}

