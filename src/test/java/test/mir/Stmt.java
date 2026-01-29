/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package test.mir;

import java.lang.constant.ClassDesc;

/**
 *
 * @author joemw
 */
public interface Stmt {
    record PutStatic(ClassDesc owner, String fieldName, ClassDesc fieldDesc, Expr value) implements Stmt {
        @Override
        public void emit(CodeEmitter out) {
            value.emit(out);              // push value
            out.putstatic(owner, fieldName, fieldDesc); // consume value
        }
    }
    void emit(CodeEmitter out);
}
