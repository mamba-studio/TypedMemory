/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.nir;

/**
 *
 * @author jmburu
 */
public interface CodeEmitter {
    void emitIConst(int value);
    void emitANewArray(String className);
    void emitIStore(int slot);
    void emitAReturn();
    void emitDup();
}
