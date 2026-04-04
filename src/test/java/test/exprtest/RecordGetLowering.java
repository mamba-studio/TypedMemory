/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package test.exprtest;

//import com.mamba.typedmemory.internal.ir.

import test.exprtest.expr.Expr;
import com.mamba.typedmemory.api.MemLayout;
import com.mamba.typedmemory.internal.ir.IRHelper;
import static com.mamba.typedmemory.internal.ir.IRHelper.CD_MemorySegment;
import com.mamba.typedmemory.internal.ir.IRHelper.InvokeKind;
import com.mamba.typedmemory.internal.ir.RecordAccessEmitter;
import com.mamba.typedmemory.internal.ir.RecordVarHandlePlan;
import com.mamba.typedmemory.internal.ir.Stmt;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import static java.lang.constant.ConstantDescs.CD_VarHandle;
import static java.lang.constant.ConstantDescs.CD_long;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import test.exprtest.expr.methods.ConstructorExpr;
import test.exprtest.expr.fields.GetFieldExpr;
import test.exprtest.expr.methods.InstanceMethodExpr;
import test.exprtest.expr.fields.GetStaticFieldExpr;
import test.exprtest.LocalAllocator.LocalBinding;
import static test.exprtest.LocalAllocator.THIS;
import test.exprtest.expr.values.LocalExpr;
import test.exprtest.ops.MulExpr;


/**
 *
 * @author joemw
 */
public class RecordGetLowering implements RecordAccessEmitter{
    
    public Stmt emitGet(ClassDesc owner, Class<? extends Record> recordType, MemLayout memLayout) {
        var plans = buildPlans(recordType, memLayout);
        var root = buildGetExpr(owner, recordType, plans.iterator());

        return new Stmt.Block(List.of(
            new Stmt.SimpleStmt(root::emit)
        ));
    }
    
    private Expr buildGetExpr(ClassDesc owner, Class<? extends Record> type, Iterator<RecordVarHandlePlan> it) {
        var args = new ArrayList<Expr>();

        for (var component : type.getRecordComponents()) {
            var fieldType = component.getType();

            if (fieldType.isRecord()) {
                args.add(buildGetExpr(owner, (Class<? extends Record>) fieldType, it));
            } else {
                args.add(buildVarHandleGetExpr(owner, it.next()));
            }
        }

        return new ConstructorExpr(
            ClassDesc.ofDescriptor(type.descriptorString()),
            IRHelper.constructorRecordTypeDesc(type),
            args.toArray(Expr[]::new)
        );
    }
    
    private Expr buildVarHandleGetExpr(ClassDesc owner, RecordVarHandlePlan plan) {
        return new InstanceMethodExpr(
                    new GetStaticFieldExpr(owner, plan.varHandleFieldName(), CD_VarHandle),
                    CD_VarHandle,
                    "get",
                    plan.vhType(),
                    InvokeKind.VIRTUAL,
                    new GetFieldExpr(new LocalExpr(THIS), owner, "segment", CD_MemorySegment),
                    new MulExpr(
                        TypeKind.LONG,
                        new LocalExpr(new LocalBinding(1, IRHelper.JVMType.LONG)),
                        new GetStaticFieldExpr(owner, "STRIDE", CD_long)
                    )
                );
    }
}
