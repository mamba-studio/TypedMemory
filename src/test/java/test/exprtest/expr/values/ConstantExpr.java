/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.exprtest.expr.values;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import java.lang.constant.ConstantDesc;
import test.exprtest.expr.Expr;

/**
 *
 * @author joemw
 */
public record ConstantExpr(ConstantDesc value) implements Expr {
    @Override
    public void emit(CodeEmitter out) {
        if (value == null) {
            out.aconst_null();
        } else {
            out.ldc(value);
        }
    }
}
