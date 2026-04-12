/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.op.stmt;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import test.op.Expr;
import test.op.Stmt;

/**
 *
 * @author joemw
 */
public record EvalStmt(Expr expr) implements Stmt {
    @Override
    public void emit(CodeEmitter out) {
        expr.emit(out);
        out.pop(); // discard result
    }
}
