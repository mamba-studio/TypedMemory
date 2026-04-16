package test.op.stmt;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import com.mamba.typedmemory.internal.emitter.CodeEmitter.IRLabel;
import test.op.Expr;
import test.op.Stmt;

public record IfStmt(BranchCondition condition, Expr left, Expr right, IRLabel target) implements Stmt {
    @Override
    public void emit(CodeEmitter out) {        
        switch (condition) {
            case IF_EQ_ZERO -> {
                left.emit(out);
                out.ifeq(target);
            }
            case IF_NE_ZERO -> {
                left.emit(out);
                out.ifne(target);
            }
            case IF_GE_ZERO -> {
                left.emit(out);
                out.ifge(target);
            }
            case IF_LT_ZERO -> {
                left.emit(out);
                out.iflt(target);
            }
            case IF_ICMP_EQ -> {
                left.emit(out);
                right.emit(out);
                out.if_icmpeq(target);
            }
            case IF_ICMP_NE -> {
                left.emit(out);
                right.emit(out);
                out.if_icmpne(target);
            }
        }
    }
}