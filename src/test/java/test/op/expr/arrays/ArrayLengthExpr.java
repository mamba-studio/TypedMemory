/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.op.expr.arrays;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import test.op.Expr;

/**
 *
 * @author joemw
 */
public record ArrayLengthExpr(Expr array) implements Expr {

    @Override
    public void emit(CodeEmitter out) {
        array.emit(out);
        out.arraylength();
    }
    
}
