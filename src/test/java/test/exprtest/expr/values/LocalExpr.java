/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.exprtest.expr.values;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import com.mamba.typedmemory.internal.ir.IRHelper;
import test.exprtest.LocalAllocator.LocalBinding;
import test.exprtest.expr.Expr;

/**
 *
 * @author joemw
 */
public record LocalExpr(LocalBinding binding) implements Expr {
    @Override
    public void emit(CodeEmitter out) {
        IRHelper.emitLoad(out, binding.kind(), binding.slot());
    }
}
