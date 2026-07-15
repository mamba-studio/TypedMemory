/*
 * Copyright 2026 joemw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mamba.typedmemory.opcode.emitter;
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
    public void bastore() {
        builder.bastore();
    }

    @Override
    public void sastore() {
        builder.sastore();
    }

    @Override
    public void castore() {
        builder.castore();
    }

    @Override
    public void iastore() {
        builder.iastore();
    }

    @Override
    public void lastore() {
        builder.lastore();
    }

    @Override
    public void fastore() {
        builder.fastore();
    }

    @Override
    public void dastore() {
        builder.dastore();
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
    public void imul(){
        builder.imul();
    }
    
    @Override
    public void fmul(){
        builder.fmul();
    }
    
    @Override
    public void dmul(){
        builder.dmul();
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
    public void iadd() {
        builder.iadd();
    }
    
    @Override
    public void fadd() {
        builder.fadd();
    }
    
    @Override
    public void dadd() {
        builder.dadd();
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
    public void fconst(float v) {
        if (v == 0.0f) {
            builder.fconst_0();
        } else if (v == 1.0f) {
            builder.fconst_1();
        } else if (v == 2.0f) {
            builder.fconst_2();
        } else {
            builder.ldc(v);
        }
    }
    
    @Override
    public void dconst(double v) {
        if (v == 0.0d) {
            builder.dconst_0();
        } else if (v == 1.0d) {
            builder.dconst_1();
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
    public void baload() {
        builder.baload();
    }

    @Override
    public void saload() {
        builder.saload();
    }

    @Override
    public void caload() {
        builder.caload();
    }

    @Override
    public void iaload() {
        builder.iaload();
    }

    @Override
    public void laload() {
        builder.laload();
    }

    @Override
    public void faload() {
        builder.faload();
    }

    @Override
    public void daload() {
        builder.daload();
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
    public void i2f() {
        builder.i2f();
    }
    
    @Override
    public void i2d() {
        builder.i2d();
    }
    
    @Override
    public void l2f() {
        builder.l2f();
    }
    
    @Override
    public void l2d() {
        builder.l2d();
    }
    
    @Override
    public void f2d() {
        builder.f2d();
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
    public void if_acmpeq(IRLabel label) {
        builder.if_acmpeq(backendLabel(label));
    }

    @Override
    public void if_acmpne(IRLabel label) {
        builder.if_acmpne(backendLabel(label));
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
