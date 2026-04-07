/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.op.expr.arrays;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import java.lang.constant.ClassDesc;
import test.op.Expr;
import test.op.expr.IntExpr;
import test.op.expr.NewArrayExpr;

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