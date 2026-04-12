package test.op;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


import com.mamba.typedmemory.api.MemLayout;
import com.mamba.typedmemory.api.size;
import com.mamba.typedmemory.internal.emitter.DebugEmitter;
import com.mamba.typedmemory.internal.ir.IRHelper;
import java.lang.constant.ClassDesc;
import test.op.expr.values.LocalExpr;
import test.op.stmt.BlockStmt;

/**
 *
 * @author joemw
 */
public class Test {    
    public record Pixel(int i, int j){}
    public record Point(byte x, @size(3)Pixel[] y, @size(3) int[] z){} 
    
    void main(){
      //  var getL = new RecordGetLowering();
       // var setL = new RecordSetLowering();
        
        //var memL = MemLayout.of(Point.class);
        
        //var stmtGet = getL.emitGet(ClassDesc.ofDescriptor(Test.class.descriptorString()), Screen.class, memL);
        //var stmtSet = setL.emitSet(ClassDesc.ofDescriptor(Test.class.descriptorString()), Screen.class, memL);
        
        //stmtGet.emit(new DebugEmitter());
        //IO.println();
        //stmtSet.emit(new DebugEmitter());
        
        //MemLayoutLowering.lower(memL, ClassDesc.ofDescriptor(Test.class.descriptorString())).emit(new DebugEmitter());
        //VarHandleLowering.lower(memL, ClassDesc.ofDescriptor(Test.class.descriptorString())).emit(new DebugEmitter());
        
        record Deep(String v) {}
        record Inner(Deep d, String s) {}
        record Outer(Inner inner) {}
        
        var stmts = new java.util.ArrayList<Stmt>();
        var root = new LocalExpr(new LocalAllocator.AllocatedLocal(3, IRHelper.JVMType.REFERENCE, "t"));

        SetLowering.emitRecordNullChecks(Outer.class, root, stmts);
        
        var s = new BlockStmt(stmts);
        s.emit(new DebugEmitter());

    }
}