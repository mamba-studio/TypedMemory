/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.mamba.typedmemory.opcode.stmt;

import com.mamba.typedmemory.opcode.emitter.CodeEmitter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

///
/// @author joemw
public record BlockStmt(List<Stmt> statements) implements Stmt {
    public static BlockStmt voidReturn(Stmt... stmts) {
        return new BlockStmt(
            Stream.concat(
                Arrays.stream(stmts),
                Stream.of(new ReturnVoidStmt()) //clinit has returnvoid always
            ).toList()
        );
    }

    public static BlockStmt RefReturn(Stmt... stmts) {
        return new BlockStmt(
            Stream.concat(
                Arrays.stream(stmts),
                Stream.of(new ReturnRefStmt()) //clinit has returnvoid always
            ).toList()
        );
    }

    @Override
    public void emit(CodeEmitter out) {
        for (Stmt s : statements) {
            switch(s){ //flatten (notice it's recursive if it's a mixture of blocks)
                case BlockStmt b -> b.emit(out);
                case Stmt st -> st.emit(out);
                //adding interface makes it exhaustive
            }
        }
    }
}
