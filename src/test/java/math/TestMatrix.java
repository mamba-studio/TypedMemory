/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package math;

import com.mamba.typedmemory.api.Mem;
import com.mamba.typedmemory.api.MemLayout;
import java.lang.foreign.Arena;

///
/// @author joemw
public class TestMatrix {
    void main(){
        try(var arena = Arena.ofConfined()){
            Mem<Matrix4> mem = Mem.of(Matrix4.class, arena, 10000);
            mem.set(0, Matrix4.identity());
            
            IO.println(mem.get(0));
            IO.println(MemLayout.memorySummary(mem));
        }
    }
}
