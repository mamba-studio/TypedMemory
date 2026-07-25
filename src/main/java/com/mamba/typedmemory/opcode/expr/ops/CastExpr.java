/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.mamba.typedmemory.opcode.expr.ops;

import com.mamba.typedmemory.opcode.emitter.CodeEmitter;
import com.mamba.typedmemory.opcode.expr.Expr;
import java.lang.constant.ClassDesc;

///
/// @author joemw
public record CastExpr(ClassDesc type, Expr expr) implements Expr {
    @Override
    public void emit(CodeEmitter out) {
        expr.emit(out);
        out.checkcast(type);
    }
}
