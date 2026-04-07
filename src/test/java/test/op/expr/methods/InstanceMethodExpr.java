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

/**
 *
 * @author joemw
 */
public record InstanceMethodExpr(
        Expr receiver, ClassDesc owner, String name, MethodTypeDesc type, IRHelper.InvokeKind kind, Expr... args
        ) implements Expr{
    @Override
    public void emit(CodeEmitter out) {
        receiver.emit(out);

        for (Expr arg : args) {
            arg.emit(out);
        }

        switch (kind) {
            case VIRTUAL -> out.invokevirtual(owner, name, type);
            case INTERFACE -> out.invokeinterface(owner, name, type);
            case SPECIAL -> out.invokespecial(owner, name, type);
            default -> throw new IllegalStateException();
        }
    }
}
