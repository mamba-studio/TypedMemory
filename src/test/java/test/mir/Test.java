/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.mir;

import java.lang.constant.ClassDesc;
import java.util.List;
import test.mir.Expr.ArrayExpr;
import test.mir.Expr.PaddingLayoutExpr;
import test.mir.Expr.StructLayoutExpr;
import test.mir.Expr.ValueLayoutWithNameExpr;
import test.mir.Expr.WithNameExpr;
import static test.mir.Helper.CD_MemoryLayout;
import test.mir.Stmt.PutStatic;

/**
 *
 * @author joemw
 */
public class Test {
    void main(){
        var owner = ClassDesc.of("test.mir.StructType");
        var clinit =  new PutStatic(
                            owner,
                            "layout",
                            CD_MemoryLayout,
                            new WithNameExpr(
                                new StructLayoutExpr(
                                    new ArrayExpr(
                                        CD_MemoryLayout,
                                        List.of(
                                            new ValueLayoutWithNameExpr("JAVA_BYTE", "x"),
                                            new PaddingLayoutExpr(3),
                                            new ValueLayoutWithNameExpr("JAVA_INT", "y"),
                                            new ValueLayoutWithNameExpr("JAVA_LONG", "z")
                                        )
                                    )
                                ),
                                "Point"
                            )
                        );
    }
}
