/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.exprtest.expr.arrays;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import java.lang.classfile.TypeKind;
import test.exprtest.expr.Expr;
import test.exprtest.expr.IntExpr;
import test.exprtest.expr.NewArrayExpr;

/**
 *
 * @author joemw
 */
public record NewPrimitiveArrayExpr(TypeKind elementType, IntExpr size) implements NewArrayExpr{
    
    public NewPrimitiveArrayExpr {
        switch(elementType){
            case REFERENCE, VOID -> throw new IllegalArgumentException("newarray requires primitive component type");
        }
    }

    @Override
    public void emit(CodeEmitter out) {
        size.emit(out);
        out.newarray(elementType);
    }
    
}
