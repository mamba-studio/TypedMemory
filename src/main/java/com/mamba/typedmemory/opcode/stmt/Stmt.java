/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mamba.typedmemory.opcode.stmt;

import com.mamba.typedmemory.opcode.emitter.CodeEmitter;

/**
 *
 * @author joemw
 */
public interface Stmt {
    void emit(CodeEmitter out);
}
