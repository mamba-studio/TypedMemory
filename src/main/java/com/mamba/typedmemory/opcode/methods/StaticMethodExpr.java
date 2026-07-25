/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.mamba.typedmemory.opcode.methods;

import com.mamba.typedmemory.opcode.emitter.CodeEmitter;
import com.mamba.typedmemory.opcode.MemberRef.MethodRef;
import com.mamba.typedmemory.opcode.expr.Expr;

///
/// @author joemw
public record StaticMethodExpr(MethodRef method, boolean isInterface, Expr... args) implements Expr {
    @Override
    public void emit(CodeEmitter out) {
        for (Expr arg : args) {
            arg.emit(out);
        }

        out.invokestatic(method.owner(), method.name(), method.type(), isInterface);
    }
}

