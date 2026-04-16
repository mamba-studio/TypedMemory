/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.op.stmt;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import com.mamba.typedmemory.internal.emitter.CodeEmitter.IRLabel;
import test.op.Stmt;

/**
 *
 * @author joemw
 */
public record LabelStmt(IRLabel label) implements Stmt {
    @Override
    public void emit(CodeEmitter out) {
        out.bind(label);
    }
}