/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.op.expr.values;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import test.op.expr.IntExpr;

/**
 *
 * @author joemw
 */
public record IntLiteralExpr(int value) implements IntExpr {
    @Override
    public void emit(CodeEmitter out) {
        out.iconst(value);
    }
}
