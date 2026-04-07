/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.op.expr.arrays;

import test.op.ArrayAccessKind;
import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import test.op.Expr;

/**
 *
 * @author joemw
 */
public record ArrayLoadExpr(ArrayAccessKind kind, Expr array, Expr index) implements Expr {
    @Override
    public void emit(CodeEmitter out) {
        array.emit(out);
        index.emit(out);

        switch (kind) {
            case REFERENCE -> out.aaload();
            case INT -> out.iaload();
        }
    }
}