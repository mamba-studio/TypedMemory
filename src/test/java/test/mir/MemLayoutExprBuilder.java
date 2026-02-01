/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.mir;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.PaddingLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import test.mir.Expr.ArrayInitExpr;
import test.mir.Expr.NewArrayExpr;
import static test.mir.Helper.CD_MemoryLayout;
import static test.mir.Helper.valueLayoutConstant;

/**
 *
 * @author joemw
 */
public class MemLayoutExprBuilder implements ExprBuilder<MemoryLayout>{

    @Override
    public Expr build(MemoryLayout layout) {
        return switch (layout) {
            case StructLayout struct -> {
                Expr base =
                    new Expr.StructLayoutExpr(
                        new Expr.ArrayExpr(
                            new NewArrayExpr(CD_MemoryLayout, struct.memberLayouts().size()),
                            new ArrayInitExpr(struct.memberLayouts()
                                  .stream()
                                  .map(this::build)
                                  .toList())
                        )
                    );

                yield struct.name()
                            .<Expr>map(n -> new Expr.WithNameExpr(base, n))
                            .orElse(base);
            }

            case ValueLayout value ->{
                Expr base = new Expr.ValueLayoutExpr(valueLayoutConstant(value));
                
                yield value.name()
                           .<Expr>map(n -> new Expr.WithNameExpr(base, n))
                           .orElse(base);
            }

            case PaddingLayout padding ->
                new Expr.PaddingLayoutExpr(padding.byteSize());

            default ->
                throw new UnsupportedOperationException(
                    "Unsupported layout: " + layout
                );
        };    
    }    
}
