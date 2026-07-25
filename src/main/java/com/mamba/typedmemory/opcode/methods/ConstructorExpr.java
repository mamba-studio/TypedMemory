/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.mamba.typedmemory.opcode.methods;

import com.mamba.typedmemory.opcode.emitter.CodeEmitter;
import com.mamba.typedmemory.opcode.MemberRef.ConstructorRef;
import com.mamba.typedmemory.opcode.expr.Expr;

///
/// @author joemw
public record ConstructorExpr(ConstructorRef ctor, Expr... args) implements Expr {
    @Override
    public void emit(CodeEmitter out) {
        out.new_(ctor.owner());
        out.dup();
        for (Expr arg : args) {
            arg.emit(out);
        }
        out.invokespecial(ctor.owner(), ctor.name(), ctor.type());
    }
}

