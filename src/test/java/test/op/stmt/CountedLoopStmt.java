package test.op.stmt;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import test.op.Expr;
import test.op.LocalAllocator;
import test.op.Stmt;
import test.op.expr.values.LocalExpr;

public record CountedLoopStmt(
        LocalAllocator.LocalBinding index,
        Expr endExclusive,
        Stmt body
) implements Stmt {
    /*
     * Emits:
     *
     *   for (long index = 0; index < endExclusive; index++) {
     *       body;
     *   }
     *
     * as:
     *
     *   index = 0
     *   loopStart:
     *       if (index >= endExclusive) goto loopDone
     *       body
     *       index = index + 1
     *       goto loopStart
     *   loopDone:
     */
    @Override
    public void emit(CodeEmitter out) {
        var loopStart = out.newLabel();
        var loopDone = out.newLabel();

        out.lconst(0L);
        out.lstore(index.slot());

        out.bind(loopStart);

        new IfStmt(
                BranchCondition.IF_GE_ZERO,
                compareLong(new LocalExpr(index), endExclusive),
                null,
                loopDone
        ).emit(out);

        body.emit(out);

        out.lload(index.slot());
        out.lconst(1L);
        out.ladd();
        out.lstore(index.slot());

        out.goto_(loopStart);
        new LabelStmt(loopDone).emit(out);
    }

    private static Expr compareLong(Expr left, Expr right) {
        return out -> {
            left.emit(out);
            right.emit(out);
            out.lcmp();
        };
    }
}
