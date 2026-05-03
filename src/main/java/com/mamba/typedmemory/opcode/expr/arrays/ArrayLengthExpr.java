/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.typedmemory.opcode.expr.arrays;

import com.mamba.typedmemory.opcode.emitter.CodeEmitter;
import com.mamba.typedmemory.opcode.expr.Expr;
import com.mamba.typedmemory.opcode.expr.values.IntExpr;

/**
 *
 * @author joemw
 */
public record ArrayLengthExpr(Expr array) implements IntExpr {
    @Override
    public void emit(CodeEmitter out) {
        array.emit(out);
        out.arraylength();
    }
}
