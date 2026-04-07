/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.op.expr.bind;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import com.mamba.typedmemory.internal.ir.IRHelper;
import test.op.Expr;
import test.op.LocalAllocator.AllocatedLocal;

/**
 *
 * @author joemw
 */
public record LetExpr(AllocatedLocal binding, Expr init, Expr body) implements Expr {

    @Override
    public void emit(CodeEmitter out) {
        init.emit(out);
        IRHelper.emitStore(out, binding.kind(), binding.slot());
        body.emit(out);
    }

}
