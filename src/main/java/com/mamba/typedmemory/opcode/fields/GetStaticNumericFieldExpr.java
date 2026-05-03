package com.mamba.typedmemory.opcode.fields;

import com.mamba.typedmemory.opcode.MemberRef.FieldRef;
import com.mamba.typedmemory.opcode.emitter.CodeEmitter;
import com.mamba.typedmemory.opcode.expr.numeric.NumericExpr;
import java.lang.classfile.TypeKind;

public record GetStaticNumericFieldExpr(FieldRef field) implements NumericExpr {
    public GetStaticNumericFieldExpr {
        NumericExpr.from(field.type());
    }

    @Override
    public TypeKind typeKind() {
        return NumericExpr.from(field.type());
    }

    @Override
    public void emit(CodeEmitter out) {
        out.getstatic(field.owner(), field.name(), field.type());
    }
}
