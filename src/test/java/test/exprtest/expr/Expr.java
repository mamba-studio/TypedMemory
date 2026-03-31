package test.exprtest.expr;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import com.mamba.typedmemory.internal.ir.IRHelper;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.util.List;
import test.exprtest.LocalAllocator.LocalBinding;

/**
 *
 * @author joemw
 */
public interface Expr {
    void emit(CodeEmitter out);
    
    @Override
    public boolean equals(Object obj);
    
    //Common abstract instructions
    //----------------------------       
    record LocalExpr(LocalBinding binding) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            IRHelper.emitLoad(out, binding.kind(), binding.slot());
        }
    }
    
    record NewArrayExpr(ClassDesc elementType, Expr size) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            size.emit(out);
            out.anewarray(elementType);
        }
    }
    
    record ArrayStoreExpr(Expr array, Expr index, Expr value) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            array.emit(out);
            index.emit(out);
            value.emit(out);
            out.aastore();
        }
    }
    
    record BlockExpr(List<Expr> expressions) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            for (Expr expr : expressions) {
                expr.emit(out);
            }
        }
    }
    
    record LetExpr(LocalBinding binding, Expr init, Expr body) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            init.emit(out);
            IRHelper.emitStore(out, binding.kind(), binding.slot());
            body.emit(out);
        }
    }
    
        
    //Specific abstract instructions
    //----------------------------     
    
    record CastExpr(ClassDesc type, Expr expr) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            expr.emit(out);
            out.checkcast(type);
        }
    }
    
    record ConstantExpr(ConstantDesc value) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            if (value == null) {
                out.aconst_null();
            } else {
                out.ldc(value);
            }
        }
    }
    
    record MulExpr(Expr left, Expr right) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            left.emit(out);
            right.emit(out);
            out.lmul();
        }
    }    
    
    record AddExpr(Expr left, Expr right) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            left.emit(out);
            right.emit(out);
            //out.ladd();
        }
    }
}
