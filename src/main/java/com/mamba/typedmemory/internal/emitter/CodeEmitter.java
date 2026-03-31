/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mamba.typedmemory.internal.emitter;

import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.MethodTypeDesc;

/**
 *
 * @author joemw
 */
public interface CodeEmitter {
    public void aconst_null();
    public void checkcast(ClassDesc type);
    public void iconst(int v); 
    public void iload(int slot);
    public void lload(int slot);
    public void fload(int slot);
    public void dload(int slot);
    public void aload(int slot);
    public void astore(int slot);
    public void aastore();
    public void iastore();
    public void ldc(ConstantDesc c);
    public void ladd();
    public void lmul();
    public void ldiv();
    public void getfield(ClassDesc owner, String name, ClassDesc type);
    public void putstatic(ClassDesc owner, String name, ClassDesc type);
    public void getstatic(ClassDesc owner, String name, ClassDesc type);
    public void invokeinterface(ClassDesc owner, String name, MethodTypeDesc methodDesc);
    public void invokestatic(ClassDesc owner, String name, MethodTypeDesc methodDesc);
    public void invokestatic(ClassDesc owner, String name, MethodTypeDesc methodDesc, boolean isInterface);
    public void invokespecial(ClassDesc owner, String name, MethodTypeDesc methodDesc);
    public void invokevirtual(ClassDesc owner, String name, MethodTypeDesc methodDesc);
    public void putfield(ClassDesc owner, String name, ClassDesc fieldType);
    public void storeLocal(TypeKind tk, int slot);
    public void dup();
    public void anewarray(ClassDesc className);
    public void return_();
    public void areturn();
    public void new_(ClassDesc className);
}
