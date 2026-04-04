/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.exprtest.expr.arrays;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import java.lang.constant.ClassDesc;
import test.exprtest.expr.Expr;
import test.exprtest.expr.IntExpr;
import test.exprtest.expr.NewArrayExpr;

/**
 *
 * @author joemw
 */
public record NewObjectArrayExpr(ClassDesc elementType, IntExpr size) implements NewArrayExpr {
    @Override
    public void emit(CodeEmitter out) {
        size.emit(out);
        out.anewarray(elementType);
    }
}