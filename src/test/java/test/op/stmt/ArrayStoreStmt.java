/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.op.stmt;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import test.op.Expr;
import test.op.ArrayAccessKind;
import test.op.Stmt;

/**
 *
 * @author joemw
 */
public record ArrayStoreStmt(ArrayAccessKind kind, Expr array, Expr index, Expr value) implements Stmt {
    @Override
    public void emit(CodeEmitter out) {
        array.emit(out);
        index.emit(out);
        value.emit(out);
        switch (kind) {
            case REFERENCE -> out.aastore();
            case INT -> out.iastore();
        }

    }
}
