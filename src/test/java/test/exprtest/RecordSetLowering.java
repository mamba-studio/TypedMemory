/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.exprtest;

import test.exprtest.expr.Expr;
import com.mamba.typedmemory.api.MemLayout;
import com.mamba.typedmemory.internal.ir.IRHelper;
import static com.mamba.typedmemory.internal.ir.IRHelper.CD_MemorySegment;
import com.mamba.typedmemory.internal.ir.IRHelper.InvokeKind;
import com.mamba.typedmemory.internal.ir.RecordAccessEmitter;
import com.mamba.typedmemory.internal.ir.Stmt;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import static java.lang.constant.ConstantDescs.CD_VarHandle;
import static java.lang.constant.ConstantDescs.CD_long;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;
import test.exprtest.expr.Expr.*;
import test.exprtest.LocalAllocator.LocalBinding;
import static test.exprtest.LocalAllocator.THIS;
import test.exprtest.expr.fields.GetFieldExpr;
import test.exprtest.expr.fields.GetStaticFieldExpr;
import test.exprtest.expr.methods.InstanceMethodExpr;
import test.exprtest.expr.values.LocalExpr;
import test.exprtest.ops.MulExpr;

/**
 *
 * @author joemw
 */
public class RecordSetLowering implements RecordAccessEmitter{
    
    public Stmt emitSet(ClassDesc owner,
                    Class<? extends Record> recordType,
                    MemLayout memLayout) {

        var vhPlans = buildPlans(recordType, memLayout);
        var paths = buildPaths(recordType);

        var stmts = new ArrayList<Stmt>();

        for (int i = 0; i < vhPlans.size(); i++) {
            var vhPlan = vhPlans.get(i);
            var path = paths.get(i);

            Expr valueExpr = buildValueExpr(recordType, path);

            Expr setExpr = new InstanceMethodExpr(
                new GetStaticFieldExpr(owner, vhPlan.varHandleFieldName(), CD_VarHandle),
                CD_VarHandle,
                "set",
                vhPlan.vhType(),
                InvokeKind.VIRTUAL,
                new GetFieldExpr(new LocalExpr(THIS), owner, "segment", CD_MemorySegment),
                new MulExpr(
                    TypeKind.LONG,
                    new LocalExpr(new LocalBinding(1, IRHelper.JVMType.LONG)),
                    new GetStaticFieldExpr(owner, "STRIDE", CD_long)
                ),
                valueExpr
            );

            stmts.add(new Stmt.SimpleStmt(setExpr::emit));
        }

        return new Stmt.Block(stmts);
    }
    
    private Expr buildValueExpr(Class<? extends Record> rootType, String path) {
        Expr current = new LocalExpr(new LocalBinding(3, IRHelper.JVMType.REFERENCE));
        Class<? extends Record> currentType = rootType;

        var parts = path.split("\\.");

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            var component = findComponent(currentType, part);
            Class<?> partType = component.getType();

            if (i < parts.length - 1) {
                current = new GetFieldExpr(
                    current,
                    ClassDesc.ofDescriptor(currentType.descriptorString()),
                    part,
                    ClassDesc.ofDescriptor(partType.descriptorString())
                );
                currentType = (Class<? extends Record>) partType;
            } else {
                current = new InstanceMethodExpr(
                    current,
                    ClassDesc.ofDescriptor(currentType.descriptorString()),
                    part,
                    MethodTypeDesc.of(
                        ClassDesc.ofDescriptor(partType.descriptorString())
                    ),
                    InvokeKind.VIRTUAL
                );
            }
        }

        return current;
    }
    
    private RecordComponent findComponent(Class<? extends Record> type, String name) {
        for (var c : type.getRecordComponents()) {
            if (c.getName().equals(name)) return c;
        }
        throw new IllegalArgumentException("No component " + name + " in " + type.getName());
    }
    
    private List<String> buildPaths(Class<? extends Record> type) {
        var out = new ArrayList<String>();
        buildPathsRecursive(type, "", out);
        return out;
    }

    private void buildPathsRecursive(Class<? extends Record> type,
                                     String prefix,
                                     List<String> out) {
        for (var component : type.getRecordComponents()) {
            var path = prefix.isEmpty()
                    ? component.getName()
                    : prefix + "." + component.getName();

            if (component.getType().isRecord()) {
                buildPathsRecursive((Class<? extends Record>) component.getType(), path, out);
            } else {
                out.add(path);
            }
        }
    }
}
