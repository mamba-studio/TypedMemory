package test.op;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;

/**
 *
 * @author joemw
 */
public interface Expr {
    void emit(CodeEmitter out);
    
    @Override
    public boolean equals(Object obj);
    
}
