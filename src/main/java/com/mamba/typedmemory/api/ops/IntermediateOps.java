/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mamba.typedmemory.api.ops;

import module java.base;
import com.mamba.typedmemory.api.MemQuery;

/**
 *
 * @author joemw
 */
public interface IntermediateOps<S, T> {
    MemQuery<S, T> filter(Predicate<T> predicate);
    <U> MemQuery<S, U> map(Function<T, U> mapper);
    MemQuery<S,T> take(long n);    
    public MemQuery<S,T> skip(long n);
}
