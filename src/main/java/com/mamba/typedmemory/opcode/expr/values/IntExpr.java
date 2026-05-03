/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mamba.typedmemory.opcode.expr.values;

import com.mamba.typedmemory.opcode.emitter.CodeEmitter;
import com.mamba.typedmemory.opcode.expr.numeric.NumericExpr;
import java.lang.classfile.TypeKind;
/**
 *
 * @author joemw
 */
public interface IntExpr extends NumericExpr {
    @Override
    default TypeKind typeKind() {
        return TypeKind.INT;
    }
    
    public record IntLiteralExpr(int value) implements IntExpr {
        @Override
        public void emit(CodeEmitter out) {
            out.iconst(value);
        }
    }
}
