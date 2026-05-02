/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.op.expr.bind;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import java.util.List;
import test.op.Expr;
import test.op.Stmt;

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
