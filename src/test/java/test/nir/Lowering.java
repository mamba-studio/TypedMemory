/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.nir;

/**
 *
 * @author jmburu
 */
public final class Lowering {

    private final CodeEmitter out;
    private int stackDepth = 0;

    public Lowering(CodeEmitter out) {
        this.out = out;
    }

    public void lower(Expr e) {
        // 1. Bottom-up traversal
        for (Expr c : e.children()) {
            lower(c);
        }

        // 2. Preserve state if required
        if (e.pushes() == 1 && e.consumesOwnResult() && !e.terminalConsumer()) {
            out.emitDup();
            stackDepth++;
        }

        // 3. Emit this node
        e.emit(out);

        // 4. Update virtual stack
        stackDepth += e.pushes() - e.pops();

        // Optional safety check
        if (stackDepth < 0) {
            throw new IllegalStateException("Stack underflow after " + e);
        }
    }
}

