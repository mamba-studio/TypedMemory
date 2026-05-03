package com.mamba.typedmemory.opcode.expr.numeric;

import com.mamba.typedmemory.opcode.OpcodeHelper;
import com.mamba.typedmemory.opcode.emitter.CodeEmitter;
import com.mamba.typedmemory.opcode.expr.Expr;
import com.mamba.typedmemory.opcode.expr.numeric.PrimitiveConversionExpr.PrimitiveConversion;
import com.mamba.typedmemory.opcode.expr.values.DoubleExpr;
import com.mamba.typedmemory.opcode.expr.values.FloatExpr;
import com.mamba.typedmemory.opcode.expr.values.IntExpr;
import com.mamba.typedmemory.opcode.expr.values.LongExpr;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;

public interface NumericExpr extends Expr {
    TypeKind typeKind();

    static TypeKind from(OpcodeHelper.JVMType type) {
        return switch (type) {
            case INT_LIKE -> TypeKind.INT;
            case LONG -> TypeKind.LONG;
            case FLOAT -> TypeKind.FLOAT;
            case DOUBLE -> TypeKind.DOUBLE;
            case REFERENCE -> throw new IllegalArgumentException("Reference expression is not numeric");
        };
    }

    static TypeKind from(ClassDesc type) {
        return switch (type.descriptorString()) {
            case "Z", "B", "S", "C", "I" -> TypeKind.INT;
            case "J" -> TypeKind.LONG;
            case "F" -> TypeKind.FLOAT;
            case "D" -> TypeKind.DOUBLE;
            default -> throw new IllegalArgumentException("Type is not numeric: " + type.descriptorString());
        };
    }

    static TypeKind normalise(TypeKind type) {
        return switch (type) {
            case BOOLEAN, BYTE, SHORT, CHAR, INT -> TypeKind.INT;
            case LONG, FLOAT, DOUBLE -> type;
            default -> throw new IllegalArgumentException("Type is not numeric: " + type);
        };
    }

    static TypeKind promote(NumericExpr left, NumericExpr right) {
        var l = normalise(left.typeKind());
        var r = normalise(right.typeKind());

        if (l == TypeKind.DOUBLE || r == TypeKind.DOUBLE) return TypeKind.DOUBLE;
        if (l == TypeKind.FLOAT || r == TypeKind.FLOAT) return TypeKind.FLOAT;
        if (l == TypeKind.LONG || r == TypeKind.LONG) return TypeKind.LONG;
        return TypeKind.INT;
    }

    static IntExpr asInt(NumericExpr expr) {
        if (normalise(expr.typeKind()) == TypeKind.INT) {
            return new IntValueExpr(expr);
        }
        throw new IllegalArgumentException("Narrowing numeric conversion is not inserted implicitly: "
                + expr.typeKind() + " to INT");
    }

    static LongExpr asLong(NumericExpr expr) {
        return switch (normalise(expr.typeKind())) {
            case INT -> new LongValueExpr(new PrimitiveConversionExpr(PrimitiveConversion.INT_TO_LONG, expr));
            case LONG -> new LongValueExpr(expr);
            default -> throw new IllegalArgumentException("Cannot promote " + expr.typeKind() + " to LONG");
        };
    }

    static FloatExpr asFloat(NumericExpr expr) {
        return switch (normalise(expr.typeKind())) {
            case INT -> new FloatValueExpr(new PrimitiveConversionExpr(PrimitiveConversion.INT_TO_FLOAT, expr));
            case LONG -> new FloatValueExpr(new PrimitiveConversionExpr(PrimitiveConversion.LONG_TO_FLOAT, expr));
            case FLOAT -> new FloatValueExpr(expr);
            default -> throw new IllegalArgumentException("Cannot promote " + expr.typeKind() + " to FLOAT");
        };
    }

    static DoubleExpr asDouble(NumericExpr expr) {
        return switch (normalise(expr.typeKind())) {
            case INT -> new DoubleValueExpr(new PrimitiveConversionExpr(PrimitiveConversion.INT_TO_DOUBLE, expr));
            case LONG -> new DoubleValueExpr(new PrimitiveConversionExpr(PrimitiveConversion.LONG_TO_DOUBLE, expr));
            case FLOAT -> new DoubleValueExpr(new PrimitiveConversionExpr(PrimitiveConversion.FLOAT_TO_DOUBLE, expr));
            case DOUBLE -> new DoubleValueExpr(expr);
            default -> throw new IllegalArgumentException("Cannot promote " + expr.typeKind() + " to DOUBLE");
        };
    }

    record IntValueExpr(NumericExpr expr) implements IntExpr {
        public IntValueExpr {
            if (NumericExpr.normalise(expr.typeKind()) != TypeKind.INT) {
                throw new IllegalArgumentException("Expected INT expression, got " + expr.typeKind());
            }
        }

        @Override
        public void emit(CodeEmitter out) {
            expr.emit(out);
        }
    }

    record LongValueExpr(NumericExpr expr) implements LongExpr {
        public LongValueExpr {
            if (NumericExpr.normalise(expr.typeKind()) != TypeKind.LONG) {
                throw new IllegalArgumentException("Expected LONG expression, got " + expr.typeKind());
            }
        }

        @Override
        public void emit(CodeEmitter out) {
            expr.emit(out);
        }
    }

    record FloatValueExpr(NumericExpr expr) implements FloatExpr {
        public FloatValueExpr {
            if (NumericExpr.normalise(expr.typeKind()) != TypeKind.FLOAT) {
                throw new IllegalArgumentException("Expected FLOAT expression, got " + expr.typeKind());
            }
        }

        @Override
        public void emit(CodeEmitter out) {
            expr.emit(out);
        }
    }

    record DoubleValueExpr(NumericExpr expr) implements DoubleExpr {
        public DoubleValueExpr {
            if (NumericExpr.normalise(expr.typeKind()) != TypeKind.DOUBLE) {
                throw new IllegalArgumentException("Expected DOUBLE expression, got " + expr.typeKind());
            }
        }

        @Override
        public void emit(CodeEmitter out) {
            expr.emit(out);
        }
    }
}
