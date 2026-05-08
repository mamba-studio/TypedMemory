/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.mamba.typedmemory.opcode.expr.bind;

import com.mamba.typedmemory.opcode.emitter.CodeEmitter;
import com.mamba.typedmemory.opcode.expr.Expr;
import com.mamba.typedmemory.opcode.stmt.Stmt;
import java.util.List;

/**
 *
 * @author joemw
 */
public record BlockExpr(List<Stmt> statements, Expr result) implements Expr {
    @Override
    public void emit(CodeEmitter out) {
        for (Stmt stmt : statements) {
            stmt.emit(out);
        }        
        result.emit(out);
    }  
}
