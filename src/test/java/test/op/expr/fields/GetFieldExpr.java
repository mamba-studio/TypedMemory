/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.op.expr.fields;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import java.lang.constant.ClassDesc;
import test.op.Expr;
import test.op.MemberRef.FieldRef;

/**
 *
 * @author joemw
 */

public record GetFieldExpr(Expr receiver, FieldRef field) implements Expr {
    @Override
    public void emit(CodeEmitter out) {
        receiver.emit(out);
        out.getfield(field.owner(), field.name(), field.type());
    }
}
