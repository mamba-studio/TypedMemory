/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mamba.typedmemory.internal.ir;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import static com.mamba.typedmemory.internal.ir.IRHelper.*;
import java.lang.constant.ClassDesc;
import static java.lang.constant.ConstantDescs.CD_String;
import static java.lang.constant.ConstantDescs.CD_long;
import static java.lang.constant.ConstantDescs.CD_VarHandle;
import static java.lang.constant.ConstantDescs.INIT_NAME;
import java.lang.constant.MethodTypeDesc;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.ValueLayout;
import java.util.List;

/**
 *
 * @author joemw
 */
public interface Expr {
    
    record ThisExpr(ClassDesc type) implements Expr{
        public ThisExpr(Class<?> type){
            this(ClassDesc.of(type.descriptorString()));
        }
        @Override
        public void emit(CodeEmitter out) {
            out.aload(0);
        }        
        
    }
    
    record LocalExpr(int slot, IRHelper.JVMType kind) implements Expr{
        @Override
        public void emit(CodeEmitter out) {
            IRHelper.emitLoad(out, kind, slot);
        }        
    }
    
    record FieldExpr(ThisExpr base, String fieldName, ClassDesc fieldType) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            base.emit(out); // aload_0

            out.getfield(
                base.type(),
                fieldName,
                fieldType
            );
        }
    }
    
    record MethodExpr(Expr target, ClassDesc owner, String name, MethodTypeDesc type, Expr... args) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            target.emit(out);
            for (Expr arg : args) {
                arg.emit(out);
            }
            out.invokevirtual(owner, name, type);
        }        
    }
           
    record WithNameExpr(Expr target, String name, ClassDesc receiverType) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            target.emit(out);     // receiver first
            out.ldc(name);        // argument after
            out.invokeinterface(receiverType, "withName", MethodTypeDesc.of(receiverType, CD_String));
        }
    }
    
    record StructLayoutExpr(ArrayExpr layoutsArray) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            layoutsArray.emit(out);
            out.invokestatic(CD_MemoryLayout, "structLayout", MethodTypeDesc.of(CD_StructLayout, CD_MemoryLayout.arrayType()), true);
        }
    }
    
    record GetStaticExpr(ClassDesc owner, String fieldName,  ClassDesc fieldDesc) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            out.getstatic(owner, fieldName, fieldDesc);
        }
    }
    
    record VarHandleExpr(Expr layoutExpr, ArrayExpr pathElements) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            layoutExpr.emit(out);      // push receiver
            pathElements.emit(out);
            out.invokeinterface(CD_MemoryLayout, "varHandle", MethodTypeDesc.of(CD_VarHandle, CD_PathElement.arrayType()));
        }        
    }
    
    sealed interface PathElementExpr extends Expr{
        record GroupElementExpr(String name) implements PathElementExpr {
            @Override
            public void emit(CodeEmitter out) {
                out.ldc(name);
                out.invokestatic(CD_PathElement, "groupElement", MethodTypeDesc.of(CD_PathElement, CD_String), true);
            }
        }
        
        record SequenceElementExpr() implements PathElementExpr {
            @Override
            public void emit(CodeEmitter out) {
                out.invokestatic(CD_PathElement, "sequenceElement", MethodTypeDesc.of(CD_PathElement));
            }
        }
    }
    
    
    record ArrayExpr(NewArrayExpr alloc, ArrayInitExpr init) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            // new MemoryLayout[elements.size()]
            alloc.emit(out);
            // stack: [array]

            init.emit(out);
            // stack: [array]
        }
    }
    
    record NewArrayExpr(ClassDesc elementInternalName, int size) implements Expr{
        @Override
        public void emit(CodeEmitter out) {
            out.iconst(size);
            out.anewarray(elementInternalName);
        }        
    }
    
    record ArrayInitExpr(List<Expr> elements) implements Expr{
        @Override
        public void emit(CodeEmitter out) {
            for (int i = 0; i < elements.size(); i++) {
                out.dup();          // preserve array
                out.iconst(i);      // index
                elements.get(i).emit(out); // value
                out.aastore();
            }
            // array remains on stack
        }     
    }
        
    record ValueLayoutExpr(ValueLayout layout) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            String fieldName = IRHelper.valueLayoutConstant(layout);
            ClassDesc fieldDesc = IRHelper.valueLayoutClassDesc(layout);

            out.getstatic(
                IRHelper.CD_ValueLayout,
                fieldName,
                fieldDesc
            );
        }
    }
    
    record PaddingLayoutExpr(long size) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            out.ldc(size);
            out.invokestatic(CD_MemoryLayout, "paddingLayout", IRHelper.methodTypeDesc(MemoryLayout.class, "paddingLayout", long.class), true);
        }
    }
    
    record RecordConstructorExpr(Class<? extends Record> recordType, ClassDesc type, List<Expr> args) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            out.new_(type);
            out.dup();

            for (Expr arg : args) {
                arg.emit(out); //  THIS is the key
            }

            out.invokespecial(type, INIT_NAME, IRHelper.constructorRecordTypeDesc(recordType));
        }
    }
    
    record VarHandleGetExpr(ClassDesc owner, RecordVarHandlePlan plan) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            // VH
            out.getstatic(owner, plan.varHandleFieldName(), CD_VarHandle);
            
            // this.segment
            new FieldExpr(new ThisExpr(owner), "segment",CD_MemorySegment).emit(out);
            
            // index * STRIDE
            out.lload(1); // index
            out.getstatic(owner, "STRIDE", CD_long);
            out.lmul();

            // call
            out.invokevirtual(CD_VarHandle, "get", plan.vhType());
        }
    }


    void emit(CodeEmitter out);
}
