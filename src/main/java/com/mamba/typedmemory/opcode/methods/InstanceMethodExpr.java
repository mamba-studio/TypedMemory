/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.mamba.typedmemory.opcode.methods;

import com.mamba.typedmemory.opcode.emitter.CodeEmitter;
import com.mamba.typedmemory.opcode.OpcodeHelper;
import static com.mamba.typedmemory.opcode.OpcodeHelper.InvokeKind.INTERFACE;
import static com.mamba.typedmemory.opcode.OpcodeHelper.InvokeKind.SPECIAL;
import static com.mamba.typedmemory.opcode.OpcodeHelper.InvokeKind.VIRTUAL;
import com.mamba.typedmemory.opcode.MemberRef.MethodRef;
import com.mamba.typedmemory.opcode.expr.Expr;

///
/// @author joemw

public record InstanceMethodExpr(Expr receiver, MethodRef method, OpcodeHelper.InvokeKind kind, Expr... args) implements Expr {
    @Override
    public void emit(CodeEmitter out) {
        receiver.emit(out);

        for (Expr arg : args) {
            arg.emit(out);
        }

        switch (kind) {
            case VIRTUAL -> out.invokevirtual(method.owner(), method.name(), method.type());
            case INTERFACE -> out.invokeinterface(method.owner(), method.name(), method.type());
            case SPECIAL -> out.invokespecial(method.owner(), method.name(), method.type());
            default -> throw new IllegalStateException();
        }
    }
}

