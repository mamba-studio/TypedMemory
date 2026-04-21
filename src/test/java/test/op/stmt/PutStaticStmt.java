/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.op.stmt;

import test.op.Stmt;
import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import test.op.Expr;
import test.op.MemberRef.FieldRef;

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
