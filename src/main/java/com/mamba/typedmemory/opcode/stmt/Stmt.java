/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mamba.typedmemory.opcode.stmt;

import com.mamba.typedmemory.opcode.emitter.CodeEmitter;
import com.mamba.typedmemory.opcode.expr.Expr;
import java.lang.constant.ClassDesc;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 *
 * @author joemw
 */
public interface Stmt {
    public record Block(List<Stmt> statements) implements Stmt {
        public static Block voidReturn(Stmt... stmts) {
            return new Block(
                Stream.concat(
                    Arrays.stream(stmts),
                    Stream.of(new ReturnVoid()) //clinit has returnvoid always
                ).toList()
            );
        }
        
        public static Block RefReturn(Stmt... stmts) {
            return new Block(
                Stream.concat(
                    Arrays.stream(stmts),
                    Stream.of(new ReturnRef()) //clinit has returnvoid always
                ).toList()
            );
        }
        
        @Override
        public void emit(CodeEmitter out) {
            for (Stmt s : statements) {
                switch(s){ //flatten (notice it's recursive if it's a mixture of blocks)
                    case Block b -> b.emit(out);
                    case Stmt st -> st.emit(out); //adding interface makes it exhaustive
                }
            }
        }
    }
    
    public record SimpleStmt(Consumer<CodeEmitter> body) implements Stmt {
        @Override
        public void emit(CodeEmitter out) {
            body.accept(out);
        }
    }
    
   public record PutStatic(ClassDesc owner, String fieldName, ClassDesc fieldDesc, Expr value) implements Stmt {
        @Override
        public void emit(CodeEmitter out) {
            value.emit(out);              // push value
            out.putstatic(owner, fieldName, fieldDesc); // consume value
        }
    }
    
   public record ReturnVoid() implements Stmt{

        @Override
        public void emit(CodeEmitter out) {
            out.return_();
        }
        
    }
   
   public record ReturnRef() implements Stmt{

        @Override
        public void emit(CodeEmitter out) {
            out.areturn();
        }
        
    }
    void emit(CodeEmitter out);
}
