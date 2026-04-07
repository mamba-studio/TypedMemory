/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.typedmemory.internal.emitter;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.MethodTypeDesc;

/**
 *
 * @author joemw
 */
public class BytecodeEmitter implements CodeEmitter{
    private final CodeBuilder builder;
    
    public BytecodeEmitter(CodeBuilder builder){
        this.builder = builder;
    }

    @Override
    public void iconst(int v) {
        this.iconst(builder, v);
    }

    @Override
    public void astore(int slot) {
        builder.astore(slot);
    }

    @Override
    public void aastore() {
        builder.aastore();
    }

    @Override
    public void iastore() {
        builder.iastore();
    }

    @Override
    public void ldc(ConstantDesc s) {
        builder.ldc(s);
    }

    @Override
    public void putstatic(ClassDesc owner, String name, ClassDesc type) {
        builder.putstatic(owner, name, type);
    }

    @Override
    public void getstatic(ClassDesc owner, String name, ClassDesc type) {
        builder.getstatic(owner, name, type);
    }

    @Override
    public void invokeinterface(ClassDesc owner, String name, MethodTypeDesc methodDesc) {
        builder.invokeinterface(owner, name, methodDesc);
    }

    @Override
    public void invokestatic(ClassDesc owner, String name, MethodTypeDesc methodDesc) {
        builder.invokestatic(owner, name, methodDesc);
    }

    @Override
    public void invokespecial(ClassDesc owner, String name, MethodTypeDesc methodDesc) {
        builder.invokespecial(owner, name, methodDesc);
    }

    @Override
    public void putfield(ClassDesc owner, String name, ClassDesc fieldType) {
        builder.putfield(owner, name, fieldType);
    }

    @Override
    public void dup() {
        builder.dup();
    }

    @Override
    public void anewarray(ClassDesc className) {
        builder.anewarray(className);
    }

    @Override
    public void return_() {
        builder.return_();
    }
    
    
    private void iconst(CodeBuilder cb, int value) {
        switch (value) {
            case -1 -> cb.iconst_m1();
            case 0  -> cb.iconst_0();
            case 1  -> cb.iconst_1();
            case 2  -> cb.iconst_2();
            case 3  -> cb.iconst_3();
            case 4  -> cb.iconst_4();
            case 5  -> cb.iconst_5();
            default -> {
                if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
                    cb.bipush(value);
                } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
                    cb.sipush(value);
                } else {
                    cb.ldc(value);
                }
            }
        }
    }

    @Override
    public void new_(ClassDesc className) {
        builder.new_(className);
    }

    @Override
    public void iload(int slot) {
        builder.iload(slot);
    }

    @Override
    public void lload(int slot) {
        builder.lload(slot);
    }

    @Override
    public void fload(int slot) {
        builder.fload(slot);
    }

    @Override
    public void dload(int slot) {
        builder.dload(slot);
    }

    @Override
    public void aload(int slot) {
        builder.aload(slot);
    }    

    @Override
    public void getfield(ClassDesc owner, String name, ClassDesc type) {
        builder.getfield(owner, name, type);
    }

    @Override
    public void invokevirtual(ClassDesc owner, String name, MethodTypeDesc methodDesc) {
        builder.invokevirtual(owner, name, methodDesc);
    }

    @Override
    public void storeLocal(TypeKind tk, int slot) {
        builder.storeLocal(tk, slot);
    }

    @Override
    public void areturn() {
        builder.areturn();
    }

    @Override
    public void invokestatic(ClassDesc owner, String name, MethodTypeDesc methodDesc, boolean isInterface) {
        builder.invokestatic(owner, name, methodDesc, isInterface);
    }
    
    @Override
    public void lmul(){
        builder.lmul();
    }

    @Override
    public void ldiv() {
        builder.ldiv();
    }

    @Override
    public void aconst_null() {
        builder.aconst_null();
    }

    @Override
    public void checkcast(ClassDesc type) {
        builder.checkcast(type);
    }

    @Override
    public void ladd() {
        builder.ladd();
    }

    @Override
    public void newarray(TypeKind tk) {
        builder.newarray(tk);
    }

    @Override
    public void pop() {
        builder.pop();
    }

    @Override
    public void lconst(long v) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void lstore(int slot) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void arraylength() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void aaload() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void iaload() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void l2i() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void i2l() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void athrow() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Label newLabel() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void bind(Label label) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void goto_(Label label) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void if_icmpne(Label label) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void ifge(Label label) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void ifeq(Label label) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void ifne(Label label) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void if_icmpeq(Label label) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void iflt(Label label) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void lcmp() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
