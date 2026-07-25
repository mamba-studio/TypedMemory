/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.mamba.typedmemory.opcode.expr.arrays;

import com.mamba.typedmemory.opcode.emitter.CodeEmitter;
import com.mamba.typedmemory.opcode.expr.values.IntExpr;
import com.mamba.typedmemory.opcode.expr.NewArrayExpr;
import java.lang.constant.ClassDesc;

///
/// @author joemw
public record NewObjectArrayExpr(ClassDesc elementType, IntExpr size) implements NewArrayExpr {
    @Override
    public void emit(CodeEmitter out) {
        size.emit(out);
        out.anewarray(elementType);
    }
}
