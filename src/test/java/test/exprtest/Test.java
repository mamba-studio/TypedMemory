/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.exprtest;

import com.mamba.typedmemory.api.MemLayout;
import com.mamba.typedmemory.internal.emitter.DebugEmitter;
import java.lang.constant.ClassDesc;

/**
 *
 * @author joemw
 */
public class Test {    
    public record Pixel(int i, int j){}
    public record Screen(Pixel p, int index){}
    
    void main(){
        var getL = new RecordGetLowering();
        var setL = new RecordSetLowering();
        
        var memL = MemLayout.of(Screen.class);
        
        //var stmtGet = getL.emitGet(ClassDesc.ofDescriptor(Test.class.descriptorString()), Screen.class, memL);
        //var stmtSet = setL.emitSet(ClassDesc.ofDescriptor(Test.class.descriptorString()), Screen.class, memL);
        
        //stmtGet.emit(new DebugEmitter());
        //IO.println();
        //stmtSet.emit(new DebugEmitter());
        
        MemLayoutLowering.lower(memL, ClassDesc.ofDescriptor(Test.class.descriptorString())).emit(new DebugEmitter());
    }
}
