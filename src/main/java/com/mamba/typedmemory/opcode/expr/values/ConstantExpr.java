/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.mamba.typedmemory.opcode.expr.values;

import com.mamba.typedmemory.opcode.emitter.CodeEmitter;
import com.mamba.typedmemory.opcode.expr.Expr;
import java.lang.constant.ConstantDesc;

///
/// @author joemw
public record ConstantExpr(ConstantDesc value) implements Expr {
    @Override
    public void emit(CodeEmitter out) {
        if (value == null) {
            out.aconst_null();
        } else {
            out.ldc(value);
        }
    }
}
