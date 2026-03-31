package com.mamba.typedmemory.internal.ir;

import com.mamba.typedmemory.api.MemLayout;
import com.mamba.typedmemory.internal.ir.Expr.RecordConstructorExpr;
import com.mamba.typedmemory.internal.ir.Expr.VarHandleGetExpr;

import module java.base;

/**
 *
 * @author joemw
 */
public class RecordGetLowering implements RecordAccessEmitter {    
    public Stmt emitGet(ClassDesc owner, Class<? extends Record> recordType, MemLayout memLayout){
        var plans = buildPlans(recordType, memLayout);        
        Iterator<RecordVarHandlePlan> it = plans.iterator();

        Expr expr = buildExpr(owner, recordType, it);

        return new Stmt.Block(List.of(
            new Stmt.SimpleStmt(out -> expr.emit(out))
        ));
    }
    
    private Expr buildExpr(ClassDesc owner, Class<? extends Record> type, Iterator<RecordVarHandlePlan> it) {
        var args = new ArrayList<Expr>();

        for (var component : type.getRecordComponents()) {
            var fieldType = component.getType();
            
            if (fieldType.isRecord()) {
                //nested record (recursive)
                args.add(buildExpr(owner, (Class<? extends Record>) fieldType, it));
            }
            else{
                var plan = it.next();
                args.add(new VarHandleGetExpr(owner, plan));
            }
        }

        return new RecordConstructorExpr(type, ClassDesc.of(type.getName()), args);
    }
    
}
