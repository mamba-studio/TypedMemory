/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.op;

import com.mamba.typedmemory.api.MemLayout;
import com.mamba.typedmemory.internal.ir.IRHelper;
import com.mamba.typedmemory.internal.layout.MemLayoutString;
import java.lang.constant.ClassDesc;
import static java.lang.constant.ConstantDescs.CD_String;
import static java.lang.constant.ConstantDescs.CD_VarHandle;
import java.lang.constant.MethodTypeDesc;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.ValueLayout;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import test.op.MemberRef.FieldRef;
import test.op.MemberRef.MethodRef;
import test.op.expr.arrays.ArrayExpr;
import test.op.expr.arrays.ArrayInitialiserExpr;
import test.op.expr.arrays.NewObjectArrayExpr;
import test.op.expr.fields.GetStaticFieldExpr;
import test.op.expr.methods.InstanceMethodExpr;
import test.op.expr.methods.StaticMethodExpr;
import test.op.expr.values.ConstantExpr;
import test.op.expr.values.IntLiteralExpr;
import test.op.stmt.BlockStmt;
import test.op.stmt.PutStaticStmt;

/**
 *
 * @author joemw
 */
public class VarHandleLowering {
    public static Stmt lower(MemLayout memLayout, ClassDesc owner) {
        var stmts = new ArrayList<Stmt>();
        var names = new ArrayDeque<>(MemLayoutString.of(memLayout).varHandleNames()); //order same as state description of record
        collect(memLayout.layout(), names, new ArrayDeque<>(), owner, stmts);
        return new BlockStmt(stmts);
    }
    
    private static void collect(
            MemoryLayout layout, Deque<String> names, Deque<Expr> path, ClassDesc owner, List<Stmt> out) {
        switch (layout) {
            case GroupLayout group -> {
                for (MemoryLayout m : group.memberLayouts()) {
                    switch (m) {
                        //value layout is where we trigger assignment to layout
                        case ValueLayout v -> {
                            var fullPath = new ArrayList<>(path);
                            fullPath.add(groupElement(v.name().orElseThrow()));

                            var vhExpr = varHandleExpr(
                                    new GetStaticFieldExpr(new FieldRef(owner, "layout", IRHelper.CD_MemoryLayout)),
                                    new ArrayExpr(
                                        new NewObjectArrayExpr(
                                            IRHelper.CD_PathElement, new IntLiteralExpr(fullPath.size())), 
                                        new ArrayInitialiserExpr(fullPath)));

                            out.add(new PutStaticStmt(new FieldRef(owner, names.removeFirst(), CD_VarHandle), vhExpr));
                        }

                        case GroupLayout g -> {
                            path.addLast(groupElement(g.name().orElseThrow()));
                            collect(g, names, path, owner, out);
                            path.removeLast();
                        }

                        case SequenceLayout s -> {
                            path.addLast(groupElement(s.name().orElseThrow()));
                            collect(s, names, path, owner, out);
                            path.removeLast();
                        }

                        default -> {}
                    }
                }
            }

            case SequenceLayout seq -> {
                path.addLast(sequenceElement());

                switch (seq.elementLayout()) {
                    case ValueLayout _ -> {
                        var fullPath = new ArrayList<>(path);
                        var vhExpr = varHandleExpr(
                            new GetStaticFieldExpr(new FieldRef(owner, "layout", IRHelper.CD_MemoryLayout)), 
                            new ArrayExpr(
                                new NewObjectArrayExpr(
                                    IRHelper.CD_PathElement, new IntLiteralExpr(fullPath.size())), 
                                new ArrayInitialiserExpr(fullPath)));
                        out.add(new PutStaticStmt(new FieldRef(owner, names.removeFirst(), CD_VarHandle), vhExpr));
                    }

                    case GroupLayout g -> collect(g, names, path, owner, out);
                    case SequenceLayout nested ->collect(nested, names, path, owner, out);
                    default -> {}
                }

                path.removeLast();
            }

            default -> {
            }
        }
    }
    
    private static Expr varHandleExpr(Expr layoutExpr, Expr pathArrayExpr) {
        return new InstanceMethodExpr(
                layoutExpr, 
                new MethodRef(IRHelper.CD_MemoryLayout, "varHandle", MethodTypeDesc.of(CD_VarHandle, IRHelper.CD_PathElement.arrayType())),
                IRHelper.InvokeKind.INTERFACE, pathArrayExpr);
    }
        
    private static Expr groupElement(String name) {
        return new StaticMethodExpr(
                new MethodRef(IRHelper.CD_PathElement, "groupElement", MethodTypeDesc.of(IRHelper.CD_PathElement, CD_String)), 
                true, new ConstantExpr(name));
    }

    private static Expr sequenceElement() {
        return new StaticMethodExpr(
                new MethodRef(IRHelper.CD_PathElement, "sequenceElement", MethodTypeDesc.of(IRHelper.CD_PathElement)), 
                true);
    }
}
