/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.mir;

import java.lang.constant.ClassDesc;
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
    public void ldc(String s) {
        IO.println("ldc " + s);
    }

    @Override
    public void ldc2(long l) {
        IO.println("ldc2 " + l);
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
    public void dup() {
        IO.println("dup");
    }

    @Override
    public void anewarray(ClassDesc className) {
        IO.println("anewarray " + className.descriptorString());
    }

    @Override
    public void return_() {
        IO.println("return void");
    }
    
}
