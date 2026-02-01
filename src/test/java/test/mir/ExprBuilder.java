/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.mir;

/**
 *
 * @author joemw
 * @param <T>
 */
public interface ExprBuilder<T> {
    public Expr build(T t);
}
