package test.op.expr.arrays;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import java.util.List;
import test.op.Expr;
import test.op.expr.values.IntLiteralExpr;

/**
 * Initializes the array currently on top of the stack with the given elements.
 * Leaves the array reference on the stack.
 */
public record ArrayInitialiserExpr(List<Expr> elements) implements Expr {
    public ArrayInitialiserExpr{
        elements = List.copyOf(elements);
    }
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
