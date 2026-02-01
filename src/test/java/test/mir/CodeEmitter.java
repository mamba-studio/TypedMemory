/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package test.mir;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

/**
 *
 * @author joemw
 */
public interface CodeEmitter {
    void iconst(int v);    
    void astore(int slot);
    void aastore();
    void iastore();
    void ldc(String s);
    void ldc2(long l);
    void putstatic(ClassDesc owner, String name, ClassDesc type);
    void getstatic(ClassDesc owner, String name, ClassDesc type);
    void invokeinterface(ClassDesc owner, String name, MethodTypeDesc methodDesc);
    void invokestatic(ClassDesc owner, String name, MethodTypeDesc methodDesc);
    void dup();
    void anewarray(ClassDesc className);
    void return_();
}
