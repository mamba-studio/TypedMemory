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
    private final java.util.IdentityHashMap<IRLabel, Label> labels = new java.util.IdentityHashMap<>();
    
    private final CodeBuilder builder;
    
    public BytecodeEmitter(CodeBuilder builder){
        this.builder = builder;
    }
    
    private Label backendLabel(IRLabel label) {
        return labels.computeIfAbsent(label, k -> builder.newLabel());
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
        if (v == 0L) {
            builder.lconst_0();
        } else if (v == 1L) {
            builder.lconst_1();
        } else {
            builder.ldc(v);
        }
    }

    @Override
    public void lstore(int slot) {
        builder.lstore(slot);
    }

    @Override
    public void arraylength() {
        builder.arraylength();
    }

    @Override
    public void aaload() {
        builder.aaload();
    }

    @Override
    public void iaload() {
        builder.iaload();
    }

    @Override
    public void l2i() {
        builder.l2i();
    }

    @Override
    public void i2l() {
        builder.i2l();
    }

    @Override
    public void athrow() {
        builder.athrow();
    }

    @Override
    public IRLabel newLabel() {
        return new IRLabel();
    }

    @Override
    public void bind(IRLabel label) { 
        builder.labelBinding(backendLabel(label));
    }

    @Override
    public void goto_(IRLabel label) {
        builder.goto_(backendLabel(label));
    }

    @Override
    public void if_icmpeq(IRLabel label) {
        builder.if_icmpeq(backendLabel(label));
    }

    @Override
    public void if_icmpne(IRLabel label) {
        builder.if_icmpne(backendLabel(label));
    }

    @Override
    public void ifeq(IRLabel label) {
        builder.ifeq(backendLabel(label));
    }

    @Override
    public void ifne(IRLabel label) {
        builder.ifne(backendLabel(label));
    }

    @Override
    public void ifge(IRLabel label) {
        builder.ifge(backendLabel(label));
    }

    @Override
    public void iflt(IRLabel label) {
        builder.iflt(backendLabel(label));
    }

    @Override
    public void lcmp() {
        builder.lcmp();
    }
}
