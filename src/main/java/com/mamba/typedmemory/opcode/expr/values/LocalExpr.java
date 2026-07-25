/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.mamba.typedmemory.opcode.expr.values;

import com.mamba.typedmemory.opcode.LocalAllocator.LocalBinding;
import com.mamba.typedmemory.opcode.emitter.CodeEmitter;
import com.mamba.typedmemory.opcode.OpcodeHelper;
import com.mamba.typedmemory.opcode.expr.numeric.NumericExpr;
import java.lang.classfile.TypeKind;


///
/// @author joemw
public record LocalExpr(LocalBinding binding) implements NumericExpr {
    @Override
    public TypeKind typeKind() {
        return NumericExpr.from(binding.kind());
    }
    
    @Override
    public void emit(CodeEmitter out) {
        OpcodeHelper.emitLoad(out, binding.kind(), binding.slot());
    }
}
