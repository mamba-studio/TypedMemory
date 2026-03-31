/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.exprtest.expr.fields;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import java.lang.constant.ClassDesc;
import test.exprtest.expr.Expr;

/**
 *
 * @author joemw
 */

public record GetFieldExpr(Expr target, ClassDesc owner, String fieldName, ClassDesc fieldType) implements Expr {
    @Override
    public void emit(CodeEmitter out) {
        target.emit(out);
        out.getfield(owner, fieldName, fieldType);
    }
}
