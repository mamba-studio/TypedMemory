
package com.mamba.typedmemory.opcode.expr.arrays;

import com.mamba.typedmemory.opcode.emitter.CodeEmitter;
import com.mamba.typedmemory.opcode.expr.Expr;
import com.mamba.typedmemory.opcode.expr.NewArrayExpr;

/**
 *
 * @author joemw
 */
public record ArrayExpr(NewArrayExpr alloc, ArrayInitialiserExpr init) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            // new MemoryLayout[elements.size()]
            alloc.emit(out);
            // stack: [array]

            init.emit(out);
            // stack: [array]
        }
    }
