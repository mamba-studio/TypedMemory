/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.typedmemory.internal.ir;

import module java.base;
import com.mamba.typedmemory.api.MemLayout;
import static com.mamba.typedmemory.internal.ir.IRHelper.CD_MemoryLayout;

/**
 *
 * @author joemw
 */
public class MemLayoutLowering {
    public static Stmt lower(MemLayout layout, ClassDesc owner) {
        return  new Stmt.PutStatic(
                    owner,
                    "layout",
                    CD_MemoryLayout,
                    build(layout.layout()));
    }
    
    public static Expr build(MemoryLayout layout) {
        return switch (layout) {
            case StructLayout struct -> {
                Expr base =
                    new Expr.StructLayoutExpr(
                        new Expr.ArrayExpr(
                            new Expr.NewArrayExpr(CD_MemoryLayout, struct.memberLayouts().size()),
                            new Expr.ArrayInitExpr(struct.memberLayouts()
                                  .stream()
                                  .map(MemLayoutLowering::build)
                                  .toList())
                        )
                    );

                yield struct.name()
                            .<Expr>map(n -> new Expr.WithNameExpr(base, n, ClassDesc.ofDescriptor(StructLayout.class.descriptorString())))
                            .orElse(base);
            }

            case ValueLayout value ->{
                Expr base = new Expr.ValueLayoutExpr(value);
                
                yield value.name()
                           .<Expr>map(n -> new Expr.WithNameExpr(base, n, IRHelper.valueLayoutClassDesc(value)))
                           .orElse(base);
            }

            case PaddingLayout padding ->
                new Expr.PaddingLayoutExpr(padding.byteSize());
                
            //TODO: No sequence implemented in switch
            /*
            case SequenceLayout sequence ->
                new Expr.SequenceLayoutExpr...
            */

            default -> 
                throw new UnsupportedOperationException(
                    "Unsupported layout: " + layout
                );
        };    
    }    
}
