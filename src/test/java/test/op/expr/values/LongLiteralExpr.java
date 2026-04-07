/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.op.expr.values;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import test.op.Expr;

/**
 *
 * @author joemw
 */
public record LongLiteralExpr(long value) implements Expr {
    @Override
    public void emit(CodeEmitter out) {
        out.lconst(value);
    }
}
