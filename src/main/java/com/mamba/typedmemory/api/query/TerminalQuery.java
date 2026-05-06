/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mamba.typedmemory.api.query;

/**
 *
 * @author joemw
 */

import module java.base;

public interface TerminalQuery<T> {
    void forEach(Consumer<T> action);
    long count();
    boolean any(Predicate<T> p);
    boolean all(Predicate<T> p);
    T reduce(T identity, BinaryOperator<T> op);
}
