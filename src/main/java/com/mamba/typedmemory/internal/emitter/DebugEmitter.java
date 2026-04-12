/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.typedmemory.internal.emitter;

import java.lang.classfile.Label;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.MethodTypeDesc;

/**
 *
 * @author joemw
 */
public class DebugEmitter implements CodeEmitter{

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
    public void iastore() {
        IO.println("iastore");
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
    public void newarray(TypeKind tk) {        
        IO.println("newarray " + tk.name());
    }

    @Override
    public void pop() {
        IO.println("pop");
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
