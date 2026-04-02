/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.exprtest.expr.arrays;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import com.mamba.typedmemory.internal.ir.Stmt;
import test.exprtest.expr.Expr;

/**
 *
 * @author joemw
 */
public record ArrayStoreStmt(Expr array, Expr index, Expr value) implements Stmt {
    @Override
    public void emit(CodeEmitter out) {
        array.emit(out);
        index.emit(out);
        value.emit(out);
        out.aastore();
    }
}
