
package test.op.expr.arrays;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import test.op.Expr;
import test.op.expr.NewArrayExpr;

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
