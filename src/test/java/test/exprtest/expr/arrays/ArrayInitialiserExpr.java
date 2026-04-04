package test.exprtest.expr.arrays;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import java.util.List;
import test.exprtest.expr.Expr;
import test.exprtest.expr.values.IntLiteralExpr;

/**
 * Initializes the array currently on top of the stack with the given elements.
 * Leaves the array reference on the stack.
 */
public record ArrayInitialiserExpr(List<Expr> elements) implements Expr {
    @Override
    public void emit(CodeEmitter out) {
        for (int i = 0; i < elements.size(); i++) {
            out.dup();
            new IntLiteralExpr(i).emit(out);
            elements.get(i).emit(out);
            out.aastore();
        }
    }
}
