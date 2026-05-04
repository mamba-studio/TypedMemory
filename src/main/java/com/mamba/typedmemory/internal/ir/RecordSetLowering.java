/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.typedmemory.internal.ir;

import com.mamba.typedmemory.api.MemLayout;
import com.mamba.typedmemory.internal.ir.Expr.FieldExpr;
import com.mamba.typedmemory.internal.ir.Expr.ThisExpr;
import static com.mamba.typedmemory.opcode.OpcodeHelper.CD_MemorySegment;
import com.mamba.typedmemory.opcode.stmt.Stmt;
import java.lang.constant.ClassDesc;
import static java.lang.constant.ConstantDescs.CD_VarHandle;
import static java.lang.constant.ConstantDescs.CD_long;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author joemw
 */
public class RecordSetLowering implements RecordAccessEmitter{
    public Stmt emitSet(ClassDesc owner, Class<? extends Record> recordType, MemLayout memLayout) {
        var plans = buildPlans(recordType, memLayout);
        var it = plans.iterator();

        var stmts = new ArrayList<Stmt>();

        buildSet(owner, recordType, it, stmts);

        return new Stmt.Block(stmts);
    }
    
    private void buildSet(ClassDesc owner, Class<? extends Record> type, Iterator<RecordVarHandlePlan> it, List<Stmt> out) {
        for (var component : type.getRecordComponents()) {
            var fieldType = component.getType();
            if (fieldType.isRecord()) {
                // recursion: into nested record
                buildSet(owner,(Class<? extends Record>) fieldType, it, out);
            } else {
                // leaf: emit one VH.set
                var plan = it.next();

                out.add(emitLeafSet(owner, type, component, plan));
            }
        }
    }
    
    private Stmt emitLeafSet(ClassDesc owner, Class<? extends Record> recordType, RecordComponent component, RecordVarHandlePlan plan) {
        return new Stmt.SimpleStmt(out -> {
            // VH
            out.getstatic(owner, plan.varHandleFieldName(), CD_VarHandle);

            // this.segment
            new FieldExpr(new ThisExpr(owner), "segment", CD_MemorySegment).emit(out);

            // index * STRIDE
            out.lload(1);
            out.getstatic(owner, "STRIDE", CD_long);
            out.lmul();

            // load record (from set(long index, Record t)... hence we are loading t)
            out.aload(3); // assuming: this=0, index=1&2, record=3

            // call accessor
            var accessorName = component.getName();
            var accessorDesc = MethodTypeDesc.of(ClassDesc.ofDescriptor(component.getType().descriptorString()));

            out.invokevirtual(ClassDesc.of(recordType.getName()), accessorName, accessorDesc);

            // VH.set
            out.invokevirtual(CD_VarHandle, "set", plan.vhType());
        });
    }
}
