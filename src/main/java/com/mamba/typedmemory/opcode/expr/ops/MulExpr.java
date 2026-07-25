/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.mamba.typedmemory.opcode.expr.ops;

import com.mamba.typedmemory.opcode.emitter.CodeEmitter;
import com.mamba.typedmemory.opcode.expr.numeric.NumericExpr;
import com.mamba.typedmemory.opcode.expr.values.DoubleExpr;
import com.mamba.typedmemory.opcode.expr.values.FloatExpr;
import com.mamba.typedmemory.opcode.expr.values.IntExpr;
import com.mamba.typedmemory.opcode.expr.values.LongExpr;

///
/// @author joemw
public final class MulExpr {
    private MulExpr() {}
    
    public static NumericExpr of(NumericExpr left, NumericExpr right) {
        return switch (NumericExpr.promote(left, right)) {
            case INT -> new IntMulExpr(NumericExpr.asInt(left), NumericExpr.asInt(right));
            case LONG -> new LongMulExpr(NumericExpr.asLong(left), NumericExpr.asLong(right));
            case FLOAT -> new FloatMulExpr(NumericExpr.asFloat(left), NumericExpr.asFloat(right));
            case DOUBLE -> new DoubleMulExpr(NumericExpr.asDouble(left), NumericExpr.asDouble(right));
            default -> throw new IllegalStateException("Unexpected promoted mul type");
        };
    }
    
    public record IntMulExpr(IntExpr left, IntExpr right) implements IntExpr {
        @Override
        public void emit(CodeEmitter out) {
            left.emit(out);
            right.emit(out);
            out.imul();
        }
    }
    
    public record LongMulExpr(LongExpr left, LongExpr right) implements LongExpr {
        @Override
        public void emit(CodeEmitter out) {
            left.emit(out);
            right.emit(out);
            out.lmul();
        }
    }
    
    public record FloatMulExpr(FloatExpr left, FloatExpr right) implements FloatExpr {
        @Override
        public void emit(CodeEmitter out) {
            left.emit(out);
            right.emit(out);
            out.fmul();
        }
    }
    
    public record DoubleMulExpr(DoubleExpr left, DoubleExpr right) implements DoubleExpr {
        @Override
        public void emit(CodeEmitter out) {
            left.emit(out);
            right.emit(out);
            out.dmul();
        }
    }
}    
