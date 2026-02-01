/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.mir;

import com.mamba.typedmemory.core.MemLayout;
import com.mamba.typedmemory.core.MemLayoutString;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author joemw
 */
public class VarHandlesExprBuilder implements ExprBuilder<MemoryLayout>{

    @Override
    public Expr build(MemoryLayout t) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    public void ofExpr(MemLayout memLayout){
        var memLayoutString = MemLayoutString.of(memLayout);
        var varNames = memLayoutString.varNames();
        var exprs = ofExpr(memLayout.layout(), new LinkedList<>(varNames), new ArrayDeque<>());
    }
    
    private static List<Expr> ofExpr(MemoryLayout memLayout, Deque<String> varNames, Deque<Expr> pathStack) {
        var result = new ArrayList<Expr>();
        switch (memLayout) {
            case GroupLayout group -> {
                for (MemoryLayout m : group.memberLayouts()) {
                    // Build path = current path + this field
                    List<Expr> path = new ArrayList<>(pathStack);
                }
            }
            default -> {}
        }
        
        return result;
    }
    
}
