/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.mamba.typedmemory.opcode.fields;

import com.mamba.typedmemory.opcode.emitter.CodeEmitter;
import com.mamba.typedmemory.opcode.MemberRef.FieldRef;
import com.mamba.typedmemory.opcode.expr.Expr;

///
/// @author joemw
public record GetStaticFieldExpr(FieldRef field) implements Expr {
    @Override
    public void emit(CodeEmitter out) {
        out.getstatic(field.owner(), field.name(), field.type());
    }
}
