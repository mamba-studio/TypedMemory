/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mamba.typedmemory.opcode.emitter;

import java.lang.classfile.Label;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.MethodTypeDesc;

/**
 *
 * @author joemw
 */
public interface CodeEmitter {
    
    
    
    public static final class IRLabel{}
    
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
    public void iadd();
    public void fadd();
    public void dadd();
    public void lmul();
    public void imul();
    public void fmul();
    public void dmul();
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
    public void newarray(TypeKind tk);
    public void anewarray(ClassDesc className);
    public void return_();
    public void areturn();
    public void new_(ClassDesc className);
    
    void pop();

    void lconst(long v);
    void fconst(float v);
    void dconst(double v);

    void lstore(int slot);

    void arraylength();

    void aaload();
    void iaload();

    void l2i();
    void i2l();
    void i2f();
    void i2d();
    void l2f();
    void l2d();
    void f2d();

    void athrow();

    IRLabel newLabel();
    void bind(IRLabel label);
    void goto_(IRLabel label);

    void ifeq(IRLabel label);
    void ifne(IRLabel label);

    void if_icmpeq(IRLabel label);
    void if_icmpne(IRLabel label);

    void ifge(IRLabel label);
    void iflt(IRLabel label);

    void lcmp();
}
