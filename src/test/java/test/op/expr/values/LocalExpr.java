/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.op.expr.values;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import com.mamba.typedmemory.internal.ir.IRHelper;
import test.op.Expr;
import test.op.LocalAllocator.AllocatedLocal;

/**
 *
 * @author joemw
 */
public record LocalExpr(AllocatedLocal binding) implements Expr {
    @Override
    public void emit(CodeEmitter out) {
        IRHelper.emitLoad(out, binding.kind(), binding.slot());
    }
}
