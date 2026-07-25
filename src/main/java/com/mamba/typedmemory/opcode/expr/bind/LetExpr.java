/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.mamba.typedmemory.opcode.expr.bind;

import com.mamba.typedmemory.opcode.emitter.CodeEmitter;
import com.mamba.typedmemory.opcode.OpcodeHelper;
import com.mamba.typedmemory.opcode.LocalAllocator.LocalBinding;
import com.mamba.typedmemory.opcode.expr.Expr;

///
/// @author joemw
public record LetExpr(LocalBinding binding, Expr init, Expr body) implements Expr {

    @Override
    public void emit(CodeEmitter out) {
        init.emit(out);
        OpcodeHelper.emitStore(out, binding.kind(), binding.slot());
        body.emit(out);
    }

}
