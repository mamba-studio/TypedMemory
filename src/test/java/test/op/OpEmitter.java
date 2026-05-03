/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.op;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;

/**
 *
 * @author joemw
 */
public class OpEmitter {
    public static void emit(CodeBuilder b, ClassDesc currentClass, Op op) {
        switch (op) {
            case Op.PutStatic p -> {
                CodeEmitter.emit(b, currentClass, p.value());   // Expr → value on stack
                b.putstatic(p.owner(), p.name(), p.type());
            }
            case Op.Return _ ->
                b.return_();
        }
    }
}