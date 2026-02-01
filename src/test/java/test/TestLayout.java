/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import com.mamba.typedmemory.core.MemLayout;
import com.mamba.typedmemory.core.MemLayoutString;
import com.mamba.typedmemory.annotation.size;

/**
 *
 * @author joemw
 */
public class TestLayout {
    void main(){
        test1();
    }
    
    public void test1(){
        record Pixel(int x, int y){}
        record Point(byte x, int y, Pixel pixel){}        
        
        MemLayout mL = MemLayout.of(Point.class);       
        MemLayoutString mLS = MemLayoutString.of(mL);
        IO.println(mL);
        IO.println(mLS.varNames());
        mLS.varHandleFields().forEach(string -> IO.println(string));
    }
    
    public void test2(){
        record Point(byte x, @size(3)int[] y){}        
        
        MemLayout mL = MemLayout.of(Point.class);       
        MemLayoutString mLS = MemLayoutString.of(mL);
        IO.println(mL);
        IO.println(mLS.varNames());
    }
}
