

import com.mamba.typedmemory.api.MemLayout;
import com.mamba.typedmemory.api.size;
import com.mamba.typedmemory.opcode.emitter.DebugEmitter;
import com.mamba.typedmemory.opcode.lowering.SetLowering;
import java.lang.constant.ClassDesc;

///
/// @author joemw
public class TestDebugEmitter {    
    public record Pixel(int i, int j){}
    public record Point(byte x, @size(3)Pixel[] y, @size(3) int[] z){} 
    
    void main(){
      //  var getL = new RecordGetLowering();
       // var setL = new RecordSetLowering();
        
        var memL = MemLayout.of(Point.class);
        
        //var stmtGet = getL.emitGet(ClassDesc.ofDescriptor(Test.class.descriptorString()), Screen.class, memL);
        //var stmtSet = setL.emitSet(ClassDesc.ofDescriptor(Test.class.descriptorString()), Screen.class, memL);
        
        //stmtGet.emit(new DebugEmitter());
        //IO.println();
        //stmtSet.emit(new DebugEmitter());
        
        //MemLayoutLowering.lower(memL, ClassDesc.ofDescriptor(Test.class.descriptorString())).emit(new DebugEmitter());
        //VarHandleLowering.lower(memL, ClassDesc.ofDescriptor(Test.class.descriptorString())).emit(new DebugEmitter());
        
        var stmt = SetLowering.lower(ClassDesc.ofDescriptor(TestDebugEmitter.class.descriptorString()), Point.class, memL);
               
        stmt.emit(new DebugEmitter());

    }
}
