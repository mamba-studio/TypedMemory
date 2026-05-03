/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.mamba.typedmemory.opcode.stmt;

import com.mamba.typedmemory.opcode.emitter.CodeEmitter;
import com.mamba.typedmemory.opcode.ArrayAccessKind;
import com.mamba.typedmemory.opcode.expr.Expr;
import com.mamba.typedmemory.opcode.expr.values.IntExpr;

/**
 *
 * @author joemw
 */
public record ArrayStoreStmt(ArrayAccessKind kind, Expr array, IntExpr index, Expr value) implements Stmt {
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
