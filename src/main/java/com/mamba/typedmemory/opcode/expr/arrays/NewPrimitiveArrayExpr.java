/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.typedmemory.opcode.expr.arrays;

import com.mamba.typedmemory.opcode.emitter.CodeEmitter;
import com.mamba.typedmemory.opcode.expr.values.IntExpr;
import com.mamba.typedmemory.opcode.expr.NewArrayExpr;
import java.lang.classfile.TypeKind;

///
/// @author joemw
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
