/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.mamba.typedmemory.opcode.stmt;

import com.mamba.typedmemory.opcode.emitter.CodeEmitter;

/**
 *
 * @author joemw
 */
public record ReturnVoidStmt() implements Stmt{
    @Override
    public void emit(CodeEmitter out) {
        out.return_();
    }

}
