/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.mir;

import com.mamba.typedmemory.core.MemLayout;
import java.lang.constant.ClassDesc;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.List;
import static test.mir.Helper.CD_MemoryLayout;
import test.mir.Stmt.Block;
import test.mir.Stmt.PutStatic;
import test.mir.Stmt.ReturnVoid;

/**
 *
 * @author joemw
 */
public class Test {
    void main(){
        test2();
       
    }
    
    
    void test2(){
        var layout = MemoryLayout.structLayout(ValueLayout.JAVA_BYTE.withName("x"),
                                                MemoryLayout.paddingLayout(3),
                                                ValueLayout.JAVA_INT.withName("y"),
                                                MemoryLayout.structLayout(
                                                    ValueLayout.JAVA_INT.withName("x"),
                                                    ValueLayout.JAVA_INT.withName("y")
                                                ).withName("pixel")
                                            ).withName("Point");
        
        var exprBuild = new MemLayoutExprBuilder();
        var exprLayout = exprBuild.build(layout);
        
        var owner = ClassDesc.of("test.mir.StructType");
                        
        var clinit =  Block.clinit(
                            new PutStatic(owner, "layout", CD_MemoryLayout, exprLayout),
                            VarHandleLowering.lower(new MemLayout(layout), owner)
                        );
                
        
        clinit.emit(new DebugEmitter());        
    }
}
