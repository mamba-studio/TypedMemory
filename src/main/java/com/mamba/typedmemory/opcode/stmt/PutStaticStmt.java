package com.mamba.typedmemory.opcode.stmt;

import com.mamba.typedmemory.opcode.emitter.CodeEmitter;
import com.mamba.typedmemory.opcode.MemberRef.FieldRef;
import com.mamba.typedmemory.opcode.expr.Expr;

/**
 *
 * @author joemw
 */
public record PutStaticStmt(FieldRef field, Expr value) implements Stmt {
    @Override
    public void emit(CodeEmitter out) {
        value.emit(out);              // push value
        out.putstatic(field.owner(), field.name(), field.type()); // consume value
    }
}
