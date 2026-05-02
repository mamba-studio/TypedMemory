package test.op.expr;

import test.op.Expr;
import test.op.expr.arrays.ArrayLengthExpr;
import test.op.expr.numeric.PrimitiveConversion;
import test.op.expr.numeric.PrimitiveConversionExpr;

public final class Exprs {
    private Exprs() {}

    public static Expr arrayLengthAsLong(Expr arrayExpr) {
        return new PrimitiveConversionExpr(
                PrimitiveConversion.INT_TO_LONG,
                new ArrayLengthExpr(arrayExpr)
        );
    }

    public static Expr longToInt(Expr value) {
        return new PrimitiveConversionExpr(PrimitiveConversion.LONG_TO_INT, value);
    }
}
