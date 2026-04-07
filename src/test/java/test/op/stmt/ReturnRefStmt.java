/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.op.stmt;

import test.op.Stmt;
import com.mamba.typedmemory.internal.emitter.CodeEmitter;

/**
 *
 * @author joemw
 */
public record ReturnRefStmt() implements Stmt{
    @Override
    public void emit(CodeEmitter out) {
        out.areturn();
    }
}
