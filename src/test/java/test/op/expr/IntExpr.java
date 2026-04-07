/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package test.op.expr;

import test.op.Expr;
import test.op.expr.values.IntLiteralExpr;

/**
 *
 * @author joemw
 */
public sealed interface IntExpr extends Expr permits IntLiteralExpr{
    
}
