/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.op;

import com.mamba.typedmemory.api.Mem;
import com.mamba.typedmemory.api.MemLayout;
import com.mamba.typedmemory.api.layout.MemLayoutString;
import com.mamba.typedmemory.api.size;
import com.mamba.typedmemory.opcode.OpcodeHelper;
import static com.mamba.typedmemory.opcode.OpcodeHelper.CD_MemoryLayout;
import static com.mamba.typedmemory.opcode.OpcodeHelper.CD_MemorySegment;
import com.mamba.typedmemory.opcode.emitter.BytecodeEmitter;
import com.mamba.typedmemory.opcode.lowering.GetLowering;
import com.mamba.typedmemory.opcode.lowering.MemLayoutLowering;
import com.mamba.typedmemory.opcode.lowering.SetLowering;
import com.mamba.typedmemory.opcode.lowering.VarHandleLowering;
import com.mamba.typedmemory.opcode.stmt.Stmt;
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
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author joemw
 */
public class TestByteCode2 {
    public record Pixel(int i, int j){}
    public record Point(byte x, @size(3) Pixel[] y, @size(3) int[] z){}
    
    void main() throws Exception {
        var owner = ClassDesc.of("test.op.GeneratedPointMem");
        var memLayout = MemLayout.of(Point.class);
        var classBytes = generate(owner, Point.class, memLayout);
        
        writeClass(owner, classBytes, Path.of("target/test-classes"));
    }
    
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
                
                b.withMethodBody(INIT_NAME, MethodTypeDesc.of(CD_void, CD_MemorySegment), ACC_PUBLIC, 
                    b0 -> {
                        var init = Stmt.Block.voidReturn(new Stmt.SimpleStmt(cb ->{
                                cb.aload(0);
                                cb.invokespecial(ConstantDescs.CD_Object, INIT_NAME, MethodTypeDesc.of(ConstantDescs.CD_void));
                                cb.aload(0);                            
                                cb.aload(1);
                                cb.putfield(owner, "segment", OpcodeHelper.CD_MemorySegment);
                                
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
                
                b.withMethodBody("get", MethodTypeDesc.of(recordDesc, CD_long), ACC_PUBLIC | ACC_FINAL, 
                    b0 ->{
                        var get = Stmt.Block.RefReturn(
                                GetLowering.lower(owner, record, memLayout)
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
                            SetLowering.lower(owner, record, memLayout)
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
                b.withMethodBody("segment", MethodTypeDesc.of(OpcodeHelper.CD_MemorySegment), ACC_PUBLIC | ACC_FINAL,
                    cb -> {
                        cb.aload(0);
                        cb.getfield(owner, "segment", OpcodeHelper.CD_MemorySegment);
                        cb.areturn();
                    }
                );
                
                b.withMethodBody("address", MethodTypeDesc.of(CD_long), ACC_PUBLIC | ACC_FINAL,
                    cb -> {
                        cb.aload(0);
                        cb.invokevirtual(owner, "segment", MethodTypeDesc.of(OpcodeHelper.CD_MemorySegment));
                        cb.invokeinterface(OpcodeHelper.CD_MemorySegment, "address", MethodTypeDesc.of(CD_long));
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
    
    static void writeClass(ClassDesc classDesc, byte[] classBytes, Path classesRoot)
            throws Exception {
        var pkg = classDesc.packageName();
        var dir = pkg.isEmpty()
                ? classesRoot
                : classesRoot.resolve(pkg.replace('.', '/'));
        
        Files.createDirectories(dir);
        Files.write(dir.resolve(classDesc.displayName() + ".class"), classBytes);
    }
}
