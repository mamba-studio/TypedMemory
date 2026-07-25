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

import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.IdentityHashMap;

///
/// @author joemw
public class DebugEmitter implements CodeEmitter{
    
    private int nextLabelId = 0;
    private final IdentityHashMap<IRLabel, String> labelNames = new IdentityHashMap<>();

    private String labelName(IRLabel label) {
        return labelNames.computeIfAbsent(label, l -> "L" + nextLabelId++);
    }    

    @Override
    public void iconst(int value) {
        IO.println("iconst " + value);}

    @Override
    public void astore(int slot) {
        IO.println("astore " + slot);
    }

    @Override
    public void aastore() {
        IO.println("aastore");
    }

    @Override
    public void bastore() {
        IO.println("bastore");
    }

    @Override
    public void sastore() {
        IO.println("sastore");
    }

    @Override
    public void castore() {
        IO.println("castore");
    }

    @Override
    public void iastore() {
        IO.println("iastore");
    }

    @Override
    public void lastore() {
        IO.println("lastore");
    }

    @Override
    public void fastore() {
        IO.println("fastore");
    }

    @Override
    public void dastore() {
        IO.println("dastore");
    }

    @Override
    public void ldc(ConstantDesc s) {
        IO.println("ldc " + s);
    }

    @Override
    public void putstatic(ClassDesc owner, String name, ClassDesc type) {
        IO.println("putstatic " + owner.descriptorString()+ " " +name+ " " +type.descriptorString());
    }

    @Override
    public void getstatic(ClassDesc owner, String name, ClassDesc type) {
        IO.println("getstatic " + owner.descriptorString()+ " " +name+ " " +type.descriptorString());
    }
    
    @Override
    public void invokeinterface(ClassDesc owner, String name, MethodTypeDesc methodDesc) {
        IO.println("invokeinterface " + owner.descriptorString()+ " " +name+ " " +methodDesc.descriptorString());
    }

    @Override
    public void invokestatic(ClassDesc owner, String name, MethodTypeDesc methodDesc) {
        IO.println("invokestatic " + owner.descriptorString()+ " " +name+ " " +methodDesc.descriptorString());
    }
        
    @Override
    public void invokestatic(ClassDesc owner, String name, MethodTypeDesc methodDesc, boolean isInterface) {
        IO.println("invokestatic " + owner.descriptorString()+ " " +name+ " " +methodDesc.descriptorString()+ " " +isInterface);
    }
    
    @Override
    public void invokespecial(ClassDesc owner, String name, MethodTypeDesc methodDesc) {
        IO.println("invokespecial " + owner.descriptorString()+ " " +name+ " " +methodDesc.descriptorString());
    }
    
    @Override
    public void dup() {
        IO.println("dup");
    }

    @Override
    public void anewarray(ClassDesc className) {
        IO.println("anewarray " + className.arrayType().descriptorString());
    }

    @Override
    public void return_() {
        IO.println("return void");
    }

    @Override
    public void putfield(ClassDesc owner, String name, ClassDesc fieldType) {
        IO.println("putfield " + owner.descriptorString()+ " " +name+ " " +fieldType.descriptorString());
    }

    @Override
    public void new_(ClassDesc className) {
        IO.println("new " +className.descriptorString());
    }

    @Override
    public void iload(int slot) {
        IO.println("iload " +slot);
    }

    @Override
    public void lload(int slot) {
        IO.println("lload " +slot);
    }

    @Override
    public void fload(int slot) {
        IO.println("fload " +slot);
    }

    @Override
    public void dload(int slot) {
        IO.println("dload " +slot);
    }

    @Override
    public void aload(int slot) {
        IO.println("aload " +slot);
    }    

    @Override
    public void getfield(ClassDesc owner, String name, ClassDesc type) {
        IO.println("getfield " + owner.descriptorString()+ " " +name+ " " +type.descriptorString());
    }

    @Override
    public void invokevirtual(ClassDesc owner, String name, MethodTypeDesc methodDesc) {
        IO.println("invokevirtual " + owner.descriptorString()+ " " +name+ " " +methodDesc.descriptorString());
    }

    @Override
    public void storeLocal(TypeKind tk, int slot) {
        IO.println("store local " +tk+ " slot " +slot);
    }

    @Override
    public void areturn() {
        IO.println("areturn");
    }

    @Override
    public void lmul() {
        IO.println("lmul");
    }
    
    @Override
    public void imul() {
        IO.println("imul");
    }
    
    @Override
    public void fmul() {
        IO.println("fmul");
    }
    
    @Override
    public void dmul() {
        IO.println("dmul");
    }

    @Override
    public void ldiv() {
        IO.println("ldiv"); 
    }

    @Override
    public void aconst_null() {
        IO.println("aconst_null");
    }

    @Override
    public void checkcast(ClassDesc type) {
        IO.println("checkcast " +type);
    }

    @Override
    public void ladd() {
        IO.println("ladd");
    }
    
    @Override
    public void iadd() {
        IO.println("iadd");
    }
    
    @Override
    public void fadd() {
        IO.println("fadd");
    }
    
    @Override
    public void dadd() {
        IO.println("dadd");
    }

    @Override
    public void newarray(TypeKind tk) {        
        IO.println("newarray " + tk.name());
    }

    @Override
    public void pop() {
        IO.println("pop");
    }

    @Override
    public void lconst(long v) { IO.println("lconst " + v); }
    
    @Override
    public void fconst(float v) { IO.println("fconst " + v); }
    
    @Override
    public void dconst(double v) { IO.println("dconst " + v); }

    @Override
    public void lstore(int slot) { IO.println("lstore " + slot); }

    @Override
    public void arraylength() { IO.println("arraylength"); }

    @Override
    public void aaload() { IO.println("aaload"); }

    @Override
    public void baload() { IO.println("baload"); }

    @Override
    public void saload() { IO.println("saload"); }

    @Override
    public void caload() { IO.println("caload"); }

    @Override
    public void iaload() { IO.println("iaload"); }

    @Override
    public void laload() { IO.println("laload"); }

    @Override
    public void faload() { IO.println("faload"); }

    @Override
    public void daload() { IO.println("daload"); }

    @Override
    public void l2i() { IO.println("l2i"); }

    @Override
    public void i2l() { IO.println("i2l"); }
    
    @Override
    public void i2f() { IO.println("i2f"); }
    
    @Override
    public void i2d() { IO.println("i2d"); }
    
    @Override
    public void l2f() { IO.println("l2f"); }
    
    @Override
    public void l2d() { IO.println("l2d"); }
    
    @Override
    public void f2d() { IO.println("f2d"); }

    @Override
    public void athrow() { IO.println("athrow"); }

    @Override
    public void lcmp() { IO.println("lcmp"); }

    @Override
    public IRLabel newLabel() {
        return new IRLabel();
    }

    @Override
    public void bind(IRLabel label) {
        IO.println(labelName(label) + ":");
    }

    @Override
    public void goto_(IRLabel label) {
        IO.println("goto " + labelName(label));
    }

    @Override
    public void if_icmpeq(IRLabel label) {
        IO.println("if_icmpeq " + labelName(label));
    }

    @Override
    public void if_icmpne(IRLabel label) {
        IO.println("if_icmpne " + labelName(label));
    }

    @Override
    public void if_acmpeq(IRLabel label) {
        IO.println("if_acmpeq " + labelName(label));
    }

    @Override
    public void if_acmpne(IRLabel label) {
        IO.println("if_acmpne " + labelName(label));
    }

    @Override
    public void ifeq(IRLabel label) {
        IO.println("ifeq " + labelName(label));
    }

    @Override
    public void ifne(IRLabel label) {
        IO.println("ifne " + labelName(label));
    }

    @Override
    public void ifge(IRLabel label) {
        IO.println("ifge " + labelName(label));
    }

    @Override
    public void iflt(IRLabel label) {
        IO.println("iflt " + labelName(label));
    }

}
