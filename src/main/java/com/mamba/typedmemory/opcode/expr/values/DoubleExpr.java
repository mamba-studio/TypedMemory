package com.mamba.typedmemory.opcode.expr.values;

import com.mamba.typedmemory.opcode.emitter.CodeEmitter;
import com.mamba.typedmemory.opcode.expr.numeric.NumericExpr;
import java.lang.classfile.TypeKind;

public interface DoubleExpr extends NumericExpr {
    @Override
    default TypeKind typeKind() {
        return TypeKind.DOUBLE;
    }

    record DoubleLiteralExpr(double value) implements DoubleExpr {
        @Override
        public void emit(CodeEmitter out) {
            out.dconst(value);
        }
    }
}
