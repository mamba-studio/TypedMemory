/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.mir;

import com.mamba.typedmemory.core.MemLayout;
import com.mamba.typedmemory.core.MemLayoutString;
import java.lang.constant.ClassDesc;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.ValueLayout;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import test.mir.Stmt.Block;

/**
 *
 * @author joemw
 */
public class VarHandleLowering {
    public static Stmt lower(MemLayout memLayout, ClassDesc owner) {
        var stmts = new ArrayList<Stmt>();
        var names = new ArrayDeque<>(MemLayoutString.of(memLayout).varNames());
        collect(memLayout.layout(), names, new ArrayDeque<>(), owner, stmts);
        return new Block(stmts);
    }
    
    private static void collect(MemoryLayout layout, Deque<String> names, Deque<Expr> path, ClassDesc owner, List<Stmt> out) {
        switch (layout) {
            case GroupLayout group -> {
                for (MemoryLayout m : group.memberLayouts()) {
                    switch (m) {
                        case ValueLayout v -> {
                            var fullPath = new ArrayList<>(path);
                            fullPath.add(
                                new Expr.PathElementExpr.GroupElementExpr(
                                    v.name().get()
                                )
                            );

                            Expr vhExpr = new Expr.VarHandleExpr(
                                new Expr.GetStaticExpr(
                                    owner,
                                    "layout",
                                    Helper.CD_MemoryLayout
                                ),
                                new Expr.ArrayExpr(
                                    new Expr.NewArrayExpr(
                                        Helper.CD_PathElement,
                                        fullPath.size()
                                    ),
                                    new Expr.ArrayInitExpr(fullPath)
                                )
                            );

                            String fieldName = names.removeFirst();

                            out.add(new Stmt.PutStatic(
                                owner,
                                fieldName,
                                Helper.CD_VarHandle,
                                vhExpr
                            ));
                        }

                        case GroupLayout g -> {
                            path.addLast(
                                new Expr.PathElementExpr.GroupElementExpr(
                                    g.name().get()
                                )
                            );
                            collect(g, names, path, owner, out);
                            path.removeLast();
                        }

                        case SequenceLayout s -> {
                            path.addLast(
                                new Expr.PathElementExpr.GroupElementExpr(
                                    s.name().get()
                                )
                            );
                            collect(s, names, path, owner, out);
                            path.removeLast();
                        }

                        default -> {}
                    }
                }
            }
            case SequenceLayout seq -> {
                path.addLast(new Expr.PathElementExpr.SequenceElementExpr());
                collect(seq.elementLayout(), names, path, owner, out);
                path.removeLast();
            }
            default -> {}
        }
    }
}
