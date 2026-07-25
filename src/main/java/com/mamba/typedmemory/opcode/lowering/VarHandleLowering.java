package com.mamba.typedmemory.opcode.lowering;

import com.mamba.typedmemory.api.MemLayout;
import com.mamba.typedmemory.opcode.OpcodeHelper;
import com.mamba.typedmemory.util.MemLayoutString;
import com.mamba.typedmemory.opcode.MemberRef.FieldRef;
import com.mamba.typedmemory.opcode.MemberRef.MethodRef;
import com.mamba.typedmemory.opcode.expr.Expr;
import com.mamba.typedmemory.opcode.expr.arrays.ArrayExpr;
import com.mamba.typedmemory.opcode.expr.arrays.ArrayInitialiserExpr;
import com.mamba.typedmemory.opcode.expr.arrays.NewObjectArrayExpr;
import com.mamba.typedmemory.opcode.expr.values.ConstantExpr;
import com.mamba.typedmemory.opcode.expr.values.IntExpr.IntLiteralExpr;
import com.mamba.typedmemory.opcode.fields.GetStaticFieldExpr;
import com.mamba.typedmemory.opcode.methods.InstanceMethodExpr;
import com.mamba.typedmemory.opcode.methods.StaticMethodExpr;
import com.mamba.typedmemory.opcode.stmt.BlockStmt;
import com.mamba.typedmemory.opcode.stmt.PutStaticStmt;
import com.mamba.typedmemory.opcode.stmt.Stmt;
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

///
/// @author joemw
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

                            var vhExpr = varHandleExpr(new GetStaticFieldExpr(new FieldRef(owner, "layout", OpcodeHelper.CD_MemoryLayout)),
                                    new ArrayExpr(
                                        new NewObjectArrayExpr(
                                            OpcodeHelper.CD_PathElement, new IntLiteralExpr(fullPath.size())), 
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
                        var vhExpr = varHandleExpr(new GetStaticFieldExpr(new FieldRef(owner, "layout", OpcodeHelper.CD_MemoryLayout)), 
                            new ArrayExpr(
                                new NewObjectArrayExpr(
                                    OpcodeHelper.CD_PathElement, new IntLiteralExpr(fullPath.size())), 
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
                new MethodRef(OpcodeHelper.CD_MemoryLayout, "varHandle", MethodTypeDesc.of(CD_VarHandle, OpcodeHelper.CD_PathElement.arrayType())),
                OpcodeHelper.InvokeKind.INTERFACE, pathArrayExpr);
    }
        
    private static Expr groupElement(String name) {
        return new StaticMethodExpr(
                new MethodRef(OpcodeHelper.CD_PathElement, "groupElement", MethodTypeDesc.of(OpcodeHelper.CD_PathElement, CD_String)), 
                true, new ConstantExpr(name));
    }

    private static Expr sequenceElement() {
        return new StaticMethodExpr(
                new MethodRef(OpcodeHelper.CD_PathElement, "sequenceElement", MethodTypeDesc.of(OpcodeHelper.CD_PathElement)), 
                true);
    }
}
