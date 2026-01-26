/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.emit;

import test.ir.Expr;
import java.lang.classfile.CodeBuilder;
import test.ir.Expr.BaseExpr.GetStatic;
import test.ir.Expr.BaseExpr.LongLiteral;
import test.ir.Expr.BaseExpr.StringLiteral;
import test.ir.Expr.CompositeExpr.ArrayLiteral;
import test.ir.Expr.CompositeExpr.Call;

/**
 *
 * @author joemw
 */
public class CodeEmitter {
    public static void emit(CodeBuilder b, Expr e) {
        switch (e) {
            case GetStatic f -> b.getstatic(f.owner(), f.name(), f.type());
            case StringLiteral s -> b.ldc(s.value());
            case ArrayLiteral a -> {
                loadConst(b, a.elements().size());
                b.anewarray(a.component());
                for (int i = 0; i < a.elements().size(); i++) {
                    b.dup();
                    loadConst(b, i);
                    emit(b, a.elements().get(i));
                    b.aastore();
                }
            }
            case Call c -> {
                for (Expr arg : c.args())
                    emit(b, arg);                
                switch (c.kind()) {
                    case STATIC -> b.invokestatic(c.owner(), c.name(), c.type());
                    case VIRTUAL -> b.invokevirtual(c.owner(), c.name(), c.type());
                    case INTERFACE -> b.invokeinterface(c.owner(), c.name(), c.type());
                    case SPECIAL -> b.invokespecial(c.owner(), c.name(), c.type());
                }
            }
            case LongLiteral l -> b.ldc(l.value());
        }
    }
    
    public static void loadConst(CodeBuilder cb, int value) {
        switch (value) {
            case -1 -> cb.iconst_m1();
            case 0  -> cb.iconst_0();
            case 1  -> cb.iconst_1();
            case 2  -> cb.iconst_2();
            case 3  -> cb.iconst_3();
            case 4  -> cb.iconst_4();
            case 5  -> cb.iconst_5();
            default -> {
                if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
                    cb.bipush(value);
                } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
                    cb.sipush(value);
                } else {
                    cb.ldc(value);
                }
            }
        }
    }
    
    
}
