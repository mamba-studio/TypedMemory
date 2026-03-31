/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.typedmemory.internal.ir;

import com.mamba.typedmemory.api.Mem;
import com.mamba.typedmemory.api.MemLayout;
import com.mamba.typedmemory.internal.layout.MemLayoutString;
import static com.mamba.typedmemory.internal.ir.IRHelper.CD_MemoryLayout;
import static com.mamba.typedmemory.internal.ir.IRHelper.CD_MemorySegment;
import com.mamba.typedmemory.internal.emitter.BytecodeEmitter;
import java.lang.classfile.ClassFile;
import static java.lang.classfile.ClassFile.ACC_BRIDGE;
import static java.lang.classfile.ClassFile.ACC_FINAL;
import static java.lang.classfile.ClassFile.ACC_PRIVATE;
import static java.lang.classfile.ClassFile.ACC_PUBLIC;
import static java.lang.classfile.ClassFile.ACC_STATIC;
import static java.lang.classfile.ClassFile.ACC_SYNTHETIC;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import static java.lang.constant.ConstantDescs.CD_VarHandle;
import static java.lang.constant.ConstantDescs.CD_long;
import static java.lang.constant.ConstantDescs.CD_void;
import static java.lang.constant.ConstantDescs.CLASS_INIT_NAME;
import static java.lang.constant.ConstantDescs.INIT_NAME;
import java.lang.constant.MethodTypeDesc;

/**
 *
 * @author joemw
 */
public class TypedMemoryClassGenerator {
    public static byte[] generate(ClassDesc owner, Class<? extends Record> record, MemLayout memLayout){
        var recordDesc = ClassDesc.ofDescriptor(record.descriptorString());
        var memLayoutString = MemLayoutString.of(memLayout);
        return ClassFile.of().build(owner, 
            b -> {
                b.withFlags(0);
                b.withInterfaceSymbols(ClassDesc.ofDescriptor(Mem.class.descriptorString()));
                b.withField("segment", CD_MemorySegment, ACC_PRIVATE | ACC_FINAL);      
                b.withField("layout", CD_MemoryLayout, ACC_PRIVATE | ACC_STATIC | ACC_FINAL);   
                b.withField("STRIDE", CD_long, ACC_PRIVATE | ACC_STATIC | ACC_FINAL);   
                b.withField("size", CD_long, ACC_PRIVATE | ACC_FINAL);
                for(var name : memLayoutString.varHandleNames())
                    b.withField(name, CD_VarHandle, ACC_PRIVATE | ACC_STATIC | ACC_FINAL); //initialise static fields
                    
                b.withMethodBody(INIT_NAME, MethodTypeDesc.of(CD_void, CD_MemorySegment), ACC_PUBLIC, 
                    b0 -> {
                        var init = Stmt.Block.voidReturn(
                            new Stmt.SimpleStmt(cb ->{
                                cb.aload(0);
                                cb.invokespecial(ConstantDescs.CD_Object, INIT_NAME, MethodTypeDesc.of(ConstantDescs.CD_void));
                                cb.aload(0);                            
                                cb.aload(1);
                                cb.putfield(owner, "segment", IRHelper.CD_MemorySegment);
                                
                                // compute size once
                                cb.aload(0);
                                cb.aload(1);
                                cb.invokeinterface(
                                    CD_MemorySegment,
                                    "byteSize",
                                    MethodTypeDesc.of(CD_long)
                                );
                                cb.getstatic(owner, "STRIDE", CD_long);
                                cb.ldiv();
                                cb.putfield(owner, "size", CD_long);
                            })
                        );
                        
                        init.emit(new BytecodeEmitter(b0));
                    }
                );
                
                b.withMethodBody(CLASS_INIT_NAME, MethodTypeDesc.of(CD_void), ACC_STATIC, 
                    b0 -> {
                        var STRIDE_ASSIGN = new Stmt.SimpleStmt(cb ->{
                            cb.getstatic(owner, "layout", CD_MemoryLayout);
                            cb.invokeinterface(CD_MemoryLayout, "byteSize", MethodTypeDesc.of(ConstantDescs.CD_long));
                            cb.putstatic(owner, "STRIDE", ConstantDescs.CD_long);
                        });
                        var clinit = Stmt.Block.voidReturn(
                            MemLayoutLowering.lower(memLayout, owner),
                            VarHandleLowering.lower(memLayout, owner),
                            STRIDE_ASSIGN
                        );
                        clinit.emit(new BytecodeEmitter(b0));
                    }
                );
                
                b.withMethodBody("get", MethodTypeDesc.of(recordDesc, CD_long), ACC_PUBLIC | ACC_FINAL, 
                    b0 ->{
                        var get = Stmt.Block.RefReturn(
                                new RecordGetLowering().emitGet(owner, record, memLayout)
                        );
                        get.emit(new BytecodeEmitter(b0));
                    }
                );
                
                b.withMethodBody("get", MethodTypeDesc.of(ConstantDescs.CD_Object, CD_long), ACC_PUBLIC | ACC_BRIDGE | ACC_SYNTHETIC,
                    cb -> {
                        cb.aload(0);
                        cb.lload(1);
                        cb.invokevirtual(owner, "get",
                            MethodTypeDesc.of(recordDesc, CD_long));
                        cb.areturn();
                    }
                );
                
                b.withMethodBody("set", MethodTypeDesc.of(CD_void, CD_long, recordDesc), ACC_PUBLIC | ACC_FINAL, 
                    b0 ->{
                        var set = Stmt.Block.voidReturn(
                                new RecordSetLowering().emitSet(owner, record, memLayout)
                        );
                        set.emit(new BytecodeEmitter(b0));
                    }
                );
                
                b.withMethodBody("set", MethodTypeDesc.of(CD_void, CD_long, ConstantDescs.CD_Object), ACC_PUBLIC | ACC_BRIDGE | ACC_SYNTHETIC,
                    cb -> {
                        cb.aload(0);                // this
                        cb.lload(1);                // long index
                        cb.aload(3);                // Object obj
                        cb.checkcast(recordDesc);   // cast to record type
                        cb.invokevirtual(owner, "set",
                            MethodTypeDesc.of(CD_void, CD_long, recordDesc));
                        cb.return_();
                    }
                );
                
                b.withMethodBody("segment", MethodTypeDesc.of(IRHelper.CD_MemorySegment), ACC_PUBLIC | ACC_FINAL,
                    cb -> {
                        cb.aload(0);
                        cb.getfield(owner, "segment", IRHelper.CD_MemorySegment);
                        cb.areturn();
                    }
                );
                
                b.withMethodBody("address", MethodTypeDesc.of(CD_long), ACC_PUBLIC | ACC_FINAL,
                    cb -> {
                        cb.aload(0);
                        cb.invokevirtual(owner, "segment", MethodTypeDesc.of(IRHelper.CD_MemorySegment));
                        cb.invokeinterface(IRHelper.CD_MemorySegment, "address", MethodTypeDesc.of(CD_long));
                        cb.lreturn();
                    }
                );
                
                b.withMethodBody("size", MethodTypeDesc.of(CD_long), ACC_PUBLIC | ACC_FINAL,
                    cb -> {
                        cb.aload(0);
                        cb.getfield(owner, "size", CD_long);
                        cb.lreturn();
                    }
                );
                
                b.withMethodBody("type", MethodTypeDesc.of(ClassDesc.of("java.lang.Class")), ACC_PUBLIC | ACC_FINAL,
                    cb -> {
                        cb.ldc(recordDesc); // pushes Color.class
                        cb.areturn();
                    }
                );
                
                b.withMethodBody("layout", MethodTypeDesc.of(CD_MemoryLayout), ACC_PUBLIC | ACC_FINAL,
                    cb -> {
                        cb.getstatic(owner, "layout", CD_MemoryLayout);
                        cb.areturn();
                    }
                );
            }
        );
    }
    
    public static ClassDesc generateUserImplName(Class<?> valueType) {
        String pkg = valueType.getPackageName();
        String simple = "Mem$" + valueType.getSimpleName()
                + "_Impl_" + Long.toHexString(System.nanoTime());

        if (pkg == null || pkg.isEmpty()) {
            // default package → NO slash prefix
            return ClassDesc.of(simple);
        }

        return ClassDesc.ofInternalName(pkg.replace('.', '/') + "/" + simple);
    }
    
}
