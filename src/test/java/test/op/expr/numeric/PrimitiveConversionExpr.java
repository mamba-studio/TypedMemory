/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.op.expr.numeric;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import test.op.Expr;

/**
 *
 * @author joemw
 */
public record PrimitiveConversionExpr(PrimitiveConversion conversion, Expr expr) implements Expr {
    @Override
    public void emit(CodeEmitter out) {
        expr.emit(out);

        switch (conversion) {
            case LONG_TO_INT -> out.l2i();
            case INT_TO_LONG -> out.i2l();
        }
    }
}
