/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package test.exprtest.expr;

import test.exprtest.expr.values.IntLiteralExpr;

/**
 *
 * @author joemw
 */
public sealed interface IntExpr extends Expr permits IntLiteralExpr{
    
}
