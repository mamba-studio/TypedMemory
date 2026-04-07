/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.op.expr.ops;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import java.lang.constant.ClassDesc;
import test.op.Expr;

/**
 *
 * @author joemw
 */
public record CastExpr(ClassDesc type, Expr expr) implements Expr {
    @Override
    public void emit(CodeEmitter out) {
        expr.emit(out);
        out.checkcast(type);
    }
}
