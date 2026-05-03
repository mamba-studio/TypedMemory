package com.mamba.typedmemory.opcode.expr.values;

import com.mamba.typedmemory.opcode.emitter.CodeEmitter;
import com.mamba.typedmemory.opcode.expr.numeric.NumericExpr;
import java.lang.classfile.TypeKind;

public interface FloatExpr extends NumericExpr {
    @Override
    default TypeKind typeKind() {
        return TypeKind.FLOAT;
    }

    record FloatLiteralExpr(float value) implements FloatExpr {
        @Override
        public void emit(CodeEmitter out) {
            out.fconst(value);
        }
    }
}
