/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.exprtest.expr.arrays;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import java.lang.constant.ClassDesc;
import test.exprtest.expr.Expr;
import test.exprtest.expr.IntExpr;

/**
 *
 * @author joemw
 */
public record NewArrayExpr(ClassDesc elementType, IntExpr size) implements Expr {
    @Override
    public void emit(CodeEmitter out) {
        size.emit(out);
        out.anewarray(elementType);
    }
}