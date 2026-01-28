/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.nir;

import java.util.List;

/**
 *
 * @author jmburu
 */
record StoreLocalExpr(int slot, Expr value) implements Expr {
    @Override
    public List<Expr> children() {
        return List.of(value);
    }

    @Override
    public int pops() {
        return 1;
    }

    @Override
    public int pushes() {
        return 1;
    }

    @Override
    public boolean terminalConsumer() {
        return false;
    }
    
    @Override
    public void emit(CodeEmitter out) {
        out.emitIStore(slot);
    }
    
    @Override
    public boolean consumesOwnResult() {
        return true;
    }
}

