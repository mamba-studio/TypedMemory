/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.ir;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;

/**
 *
 * @author joemw
 */
public sealed interface Expr {
    
    enum InvokeKind {STATIC, VIRTUAL, INTERFACE, SPECIAL}
    
    sealed interface BaseExpr extends Expr { // no children
        record GetStatic(ClassDesc owner, String name, ClassDesc type) implements BaseExpr {}
        record StringLiteral(String value) implements BaseExpr {}
        record LongLiteral(long value) implements BaseExpr {}
    }      
    
    sealed interface CompositeExpr extends Expr { // has children  
        record Call(InvokeKind kind, ClassDesc owner, String name, MethodTypeDesc type, Expr... args) implements CompositeExpr {}
        record ArrayLiteral(ClassDesc component, List<Expr> elements) implements CompositeExpr {}
    }   
}
