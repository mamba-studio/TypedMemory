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

/**
 *
 * @author joemw
 */
public final class AddExpr {
    private AddExpr() {}
    
    public static NumericExpr of(NumericExpr left, NumericExpr right) {
        return switch (NumericExpr.promote(left, right)) {
            case INT -> new IntAddExpr(NumericExpr.asInt(left), NumericExpr.asInt(right));
            case LONG -> new LongAddExpr(NumericExpr.asLong(left), NumericExpr.asLong(right));
            case FLOAT -> new FloatAddExpr(NumericExpr.asFloat(left), NumericExpr.asFloat(right));
            case DOUBLE -> new DoubleAddExpr(NumericExpr.asDouble(left), NumericExpr.asDouble(right));
            default -> throw new IllegalStateException("Unexpected promoted add type");
        };
    }
    
    public record IntAddExpr(IntExpr left, IntExpr right) implements IntExpr {
        @Override
        public void emit(CodeEmitter out) {
            left.emit(out);
            right.emit(out);
            out.iadd();
        }
    }
    
    public record LongAddExpr(LongExpr left, LongExpr right) implements LongExpr {
        @Override
        public void emit(CodeEmitter out) {
            left.emit(out);
            right.emit(out);
            out.ladd();
        }
    }
    
    public record FloatAddExpr(FloatExpr left, FloatExpr right) implements FloatExpr {
        @Override
        public void emit(CodeEmitter out) {
            left.emit(out);
            right.emit(out);
            out.fadd();
        }
    }
    
    public record DoubleAddExpr(DoubleExpr left, DoubleExpr right) implements DoubleExpr {
        @Override
        public void emit(CodeEmitter out) {
            left.emit(out);
            right.emit(out);
            out.dadd();
        }
    }
}
