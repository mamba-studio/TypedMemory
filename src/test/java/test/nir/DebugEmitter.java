/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.nir;

/**
 *
 * @author jmburu
 */
public final class DebugEmitter implements CodeEmitter {
    @Override 
    public void emitIConst(int value) {
        IO.println("iconst_" + value);
    }

    @Override 
    public void emitANewArray(String className) {
        IO.println("anewarray " + className);
    }

    @Override 
    public void emitIStore(int slot) {
        IO.println("istore_" + slot);
    }

    @Override 
    public void emitAReturn() {
        IO.println("areturn");
    }

    @Override 
    public void emitDup() {
        IO.println("dup");
    }
}

