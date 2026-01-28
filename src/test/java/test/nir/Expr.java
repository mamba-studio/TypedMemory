/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package test.nir;

import java.util.List;

/**
 *
 * @author jmburu
 */
public interface Expr {
    List<Expr> children();

    int pops();      // how many stack values it consumes
    int pushes();    // how many it produces

    boolean terminalConsumer(); // allowed to destroy value?
    
    boolean consumesOwnResult();
    
    /** Emit the actual JVM instruction(s)
     * @param out */
    void emit(CodeEmitter out);
}

