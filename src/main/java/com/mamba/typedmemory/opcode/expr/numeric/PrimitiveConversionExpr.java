/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.typedmemory.opcode.expr.numeric;

import com.mamba.typedmemory.opcode.emitter.CodeEmitter;
import java.lang.classfile.TypeKind;

///
/// @author joemw
public record PrimitiveConversionExpr(PrimitiveConversion conversion, NumericExpr expr) implements NumericExpr {
    
    public enum PrimitiveConversion {
        LONG_TO_INT,
        INT_TO_LONG,
        INT_TO_FLOAT,
        INT_TO_DOUBLE,
        LONG_TO_FLOAT,
        LONG_TO_DOUBLE,
        FLOAT_TO_DOUBLE
    }
    
    @Override
    public TypeKind typeKind() {
        return switch (conversion) {
            case LONG_TO_INT -> TypeKind.INT;
            case INT_TO_LONG -> TypeKind.LONG;
            case INT_TO_FLOAT, LONG_TO_FLOAT -> TypeKind.FLOAT;
            case INT_TO_DOUBLE, LONG_TO_DOUBLE, FLOAT_TO_DOUBLE -> TypeKind.DOUBLE;
        };
    }
    
    @Override
    public void emit(CodeEmitter out) {
        expr.emit(out);

        switch (conversion) {
            case LONG_TO_INT -> out.l2i();
            case INT_TO_LONG -> out.i2l();
            case INT_TO_FLOAT -> out.i2f();
            case INT_TO_DOUBLE -> out.i2d();
            case LONG_TO_FLOAT -> out.l2f();
            case LONG_TO_DOUBLE -> out.l2d();
            case FLOAT_TO_DOUBLE -> out.f2d();
        }
    }
    
    public static com.mamba.typedmemory.opcode.expr.values.IntExpr longToIntExpr(NumericExpr value) {
        return NumericExpr.asInt(new PrimitiveConversionExpr(PrimitiveConversion.LONG_TO_INT, value));
    }
}
