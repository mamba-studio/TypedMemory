package test.op;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import com.mamba.typedmemory.internal.ir.IRHelper;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.util.List;

/**
 *
 * @author joemw
 */
public interface Expr {
    void emit(CodeEmitter out);
    
    @Override
    public boolean equals(Object obj);
    
}
