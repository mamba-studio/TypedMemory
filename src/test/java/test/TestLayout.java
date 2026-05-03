/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import com.mamba.typedmemory.api.Mem;
import com.mamba.typedmemory.api.MemLayout;
import com.mamba.typedmemory.api.size;
import com.mamba.typedmemory.opcode.emitter.DebugEmitter;
import com.mamba.typedmemory.internal.ir.RecordGetLowering;
import com.mamba.typedmemory.internal.ir.RecordSetLowering;
import com.mamba.typedmemory.api.layout.MemLayoutString;
import java.lang.constant.ClassDesc;
import java.lang.foreign.Arena;

/**
 *
 * @author joemw
 */
public class TestLayout {
    void main(){
        test2();
    }
    
    public void test1(){
        record Point(int x, int y){}        
        
        MemLayout mL = MemLayout.of(Point.class);       
        MemLayoutString mLS = MemLayoutString.of(mL);
        IO.println(mL);
        IO.println(mLS.varHandleNames());
        mLS.varHandleFields().forEach(string -> IO.println(string));
    }
    
    public void test2(){
        record Pixel(int i, int j){}
        record Point(byte x, @size(3)Pixel[] y, @size(3) int[] z){}        
        
        MemLayout mL = MemLayout.of(Point.class);       
        MemLayoutString mLS = MemLayoutString.of(mL);
        IO.println(mL);
        for(String field : mLS.varHandleFields())
            IO.println(field);
    }
    
    public void test3(){
        /*
        record Point(int x, int y){}
        MemLayout mL = MemLayout.of(Point.class);
        var recordLowerSet = new RecordSetLowering();
        var stmtEmitSet = recordLowerSet.emitSet(ClassDesc.ofDescriptor(TestLayout.class.descriptorString()),Point.class, mL);
        stmtEmitSet.emit(new DebugEmitter());
        
        var recordLowerGet = new RecordGetLowering();
        var stmtEmitGet = recordLowerGet.emitGet(ClassDesc.ofDescriptor(TestLayout.class.descriptorString()),Point.class, mL);
        stmtEmitGet.emit(new DebugEmitter());
        */
        
        record Pixel(int i, int j){}
        record Screen(Pixel p, int index){}
        
        var mL = MemLayout.of(Screen.class);
        IO.println(mL.toString());
        
        for(var s : MemLayoutString.of(mL).varHandleFields()){
            IO.println(s);
        }
    }
    
    
    public void test4(){
        record Student(int id, int score, boolean active){}
        try (Arena arena = Arena.ofConfined()) {
            var students = Mem.of(Student.class, arena, 10);
            IO.println(MemLayout.memorySummary(students));
            IO.println();
            IO.println(MemLayout.describe(Student.class));
        }
    }
    
    public void test5(){
        record Student(int id, int score, boolean active){}
        try (Arena arena = Arena.ofConfined()) {
            var students = Mem.of(Student.class, arena, 10);
            IO.println(students.layout());
        }
    }
    
}
