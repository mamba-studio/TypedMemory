/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.exprtest.expr.bind;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import com.mamba.typedmemory.internal.ir.IRHelper;
import test.exprtest.LocalAllocator.LocalBinding;
import test.exprtest.expr.Expr;

/**
 *
 * @author joemw
 */
public record LetExpr(LocalBinding binding, Expr init, Expr body) implements Expr {

    @Override
    public void emit(CodeEmitter out) {
        init.emit(out);
        IRHelper.emitStore(out, binding.kind(), binding.slot());
        body.emit(out);
    }

}
