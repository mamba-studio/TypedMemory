/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.exprtest.ops;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import java.lang.classfile.TypeKind;
import test.exprtest.expr.Expr;

/**
 *
 * @author joemw
 */
public record AddExpr(TypeKind type, Expr left, Expr right) implements Expr {
    public AddExpr {
        switch(type){
            case REFERENCE, VOID -> throw new IllegalArgumentException("AddExpr requires numeric JVM type primitive. Currently " +type.name());
        }
    }
    @Override
    public void emit(CodeEmitter out) {
        left.emit(out);
        right.emit(out);
        switch (type) {
            case INT -> throw new UnsupportedOperationException("Type INT not supported yet");
            case LONG -> out.ladd();
            case FLOAT -> throw new UnsupportedOperationException("Type FLOAT not supported yet");
            case DOUBLE -> throw new UnsupportedOperationException("Type DOUBLE not supported yet");
            default -> throw new IllegalArgumentException("Invalid add type: " + type);
        }
    }
}
