/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.typedmemory.opcode.stmt;

import com.mamba.typedmemory.opcode.emitter.CodeEmitter;
import com.mamba.typedmemory.opcode.expr.Expr;

///
/// @author joemw
public record EvalStmt(Expr expr) implements Stmt {
    @Override
    public void emit(CodeEmitter out) {
        expr.emit(out);
        out.pop(); // discard result
    }
}
