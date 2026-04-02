/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.exprtest.ops;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import test.exprtest.expr.Expr;

/**
 *
 * @author joemw
 */
public record MulExpr(Expr left, Expr right) implements Expr {
    @Override
    public void emit(CodeEmitter out) {
        left.emit(out);
        right.emit(out);
        out.lmul();
    }
}    
