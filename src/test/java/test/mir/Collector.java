/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package test.mir;

import java.util.function.Consumer;

/**
 *
 * @author joemw
 * @param <T>
 * @param <R>
 */
public interface Collector<T, R> {
    void collect(T t, Consumer<R> out);
}
