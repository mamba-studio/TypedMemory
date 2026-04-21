/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.op.expr.fields;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import test.op.Expr;
import test.op.MemberRef.FieldRef;

/**
 *
 * @author joemw
 */
public record GetStaticFieldExpr(FieldRef field) implements Expr {
    @Override
    public void emit(CodeEmitter out) {
        out.getstatic(field.owner(), field.name(), field.type());
    }
}
