/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import test.emit.CodeEmitter;
import java.lang.classfile.ClassFile;
import static java.lang.classfile.ClassFile.ACC_FINAL;
import static java.lang.classfile.ClassFile.ACC_PRIVATE;
import static java.lang.classfile.ClassFile.ACC_PUBLIC;
import static java.lang.classfile.ClassFile.ACC_STATIC;
import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.CD_void;
import static java.lang.constant.ConstantDescs.CLASS_INIT_NAME;
import static java.lang.constant.ConstantDescs.INIT_NAME;
import java.lang.constant.MethodTypeDesc;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.ValueLayout;
import java.nio.file.Files;
import java.nio.file.Path;
import test.emit.OpEmitter;
import test.ir.Op;
import test.lowering.MemoryLayoutLowering;

/**
 *
 * @author joemw
 */
public class TestByteCode {
    
    private final ClassDesc CD_MemoryLayout         = ClassDesc.of(MemoryLayout.class.getName());
    
    MemoryLayout layout = MemoryLayout.structLayout(
                            ValueLayout.JAVA_BYTE.withName("x")
                        ).withName("Point");
    
    void main() throws Exception{
        
        var classDesc = ClassDesc.of("test.StructType");
        var classBytes = ClassFile.of().build(classDesc, 
                b -> b
                .withFlags(0)
                .withField("layout", CD_MemoryLayout, ACC_PRIVATE | ACC_STATIC | ACC_FINAL)
                
                .withMethodBody(CLASS_INIT_NAME, MethodTypeDesc.of(CD_void), ACC_STATIC, 
                        b0 -> {
                            //generate IR for layout
                            var layoutExpr = MemoryLayoutLowering.ofExpr(layout);  
                            
                            //generate IR for assigning layout to static field and return
                            var put = new Op.PutStatic(classDesc, "layout", CD_MemoryLayout, layoutExpr);
                            var ret = new Op.Return();

                            //generate whole opcode
                            OpEmitter.emit(b0, put);
                            OpEmitter.emit(b0, ret);
                        })
                
                .withMethodBody(INIT_NAME, MethodTypeDesc.of(CD_void), ACC_PUBLIC, 
                        b1 -> b1
                        .aload(0)
                        .invokespecial(CD_Object, INIT_NAME, MethodTypeDesc.of(CD_void))
                        .return_()                        
                    ));
        
        var testClassesRoot = Path.of("target/test-classes");
        writeClass(classDesc, classBytes, testClassesRoot);
           
    }
    
    static void writeClass(ClassDesc classDesc, byte[] classBytes, Path classesRoot)
        throws Exception {
               
        var pkg = classDesc.packageName();
        var dir = pkg.isEmpty()
                ? classesRoot
                : classesRoot.resolve(pkg.replace('.', '/'));

        var path = dir.resolve(classDesc.displayName() + ".class");
        Files.write(path, classBytes);
    }
}
