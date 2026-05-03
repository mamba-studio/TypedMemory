package com.mamba.typedmemory.opcode.expr;

import com.mamba.typedmemory.opcode.emitter.CodeEmitter;

/**
 *
 * @author joemw
 */
public interface Expr {
    void emit(CodeEmitter out);
    
    @Override
    public boolean equals(Object obj);
    
}
