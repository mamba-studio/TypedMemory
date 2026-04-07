/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.op.stmt;

import test.op.Stmt;
import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import java.lang.constant.ClassDesc;
import test.op.Expr;

/**
 *
 * @author joemw
 */
public record PutStaticStmt(ClassDesc owner, String fieldName, ClassDesc fieldDesc, Expr value) implements Stmt {
    @Override
    public void emit(CodeEmitter out) {
        value.emit(out);              // push value
        out.putstatic(owner, fieldName, fieldDesc); // consume value
    }
}
