/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.op.stmt;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import com.mamba.typedmemory.internal.ir.Stmt;
import test.op.Expr;
import test.op.ArrayAccessKind;

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
