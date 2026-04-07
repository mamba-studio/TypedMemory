/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.op.expr.methods;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import test.op.Expr;

/**
 *
 * @author joemw
 */
public record StaticMethodExpr(ClassDesc owner, String name, MethodTypeDesc type, boolean isInterface, Expr... args) implements Expr {
    
    
    @Override
    public void emit(CodeEmitter out) {
        for (Expr arg : args) {
            arg.emit(out);
        }
        out.invokestatic(owner, name, type, isInterface);
    }
}
