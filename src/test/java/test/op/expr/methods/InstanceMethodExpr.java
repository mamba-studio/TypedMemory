/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package test.op.expr.methods;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import com.mamba.typedmemory.internal.ir.IRHelper;
import static com.mamba.typedmemory.internal.ir.IRHelper.InvokeKind.INTERFACE;
import static com.mamba.typedmemory.internal.ir.IRHelper.InvokeKind.SPECIAL;
import static com.mamba.typedmemory.internal.ir.IRHelper.InvokeKind.VIRTUAL;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import test.op.Expr;
import test.op.MemberRef.MethodRef;

/**
 *
 * @author joemw
 */

public record InstanceMethodExpr(Expr receiver, MethodRef method, IRHelper.InvokeKind kind, Expr... args) implements Expr {
    @Override
    public void emit(CodeEmitter out) {
        receiver.emit(out);

        for (Expr arg : args) {
            arg.emit(out);
        }

        switch (kind) {
            case VIRTUAL -> out.invokevirtual(method.owner(), method.name(), method.type());
            case INTERFACE -> out.invokeinterface(method.owner(), method.name(), method.type());
            case SPECIAL -> out.invokespecial(method.owner(), method.name(), method.type());
            default -> throw new IllegalStateException();
        }
    }
}

