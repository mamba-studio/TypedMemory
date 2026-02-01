/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package test.mir;

import java.lang.constant.ClassDesc;
import java.util.List;

/**
 *
 * @author joemw
 */
public interface Stmt {
    record Block(List<Stmt> statements) implements Stmt {
        @Override
        public void emit(CodeEmitter out) {
            for (Stmt s : statements) {
                s.emit(out);
            }
        }
    }
    
    record PutStatic(ClassDesc owner, String fieldName, ClassDesc fieldDesc, Expr value) implements Stmt {
        @Override
        public void emit(CodeEmitter out) {
            value.emit(out);              // push value
            out.putstatic(owner, fieldName, fieldDesc); // consume value
        }
    }
    
    record ReturnVoid() implements Stmt{

        @Override
        public void emit(CodeEmitter out) {
            out.return_();
        }
        
    }
    void emit(CodeEmitter out);
}
