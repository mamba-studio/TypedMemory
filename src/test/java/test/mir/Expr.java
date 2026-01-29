/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package test.mir;

import java.lang.constant.ClassDesc;
import static java.lang.constant.ConstantDescs.CD_String;
import static java.lang.constant.ConstantDescs.CD_long;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import static test.mir.Helper.CD_MemoryLayout;
import static test.mir.Helper.CD_StructLayout;
import static test.mir.Helper.CD_ValueLayout;

/**
 *
 * @author joemw
 */
public interface Expr {
           
    record WithNameExpr(Expr target, String name) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            target.emit(out);     // receiver first
            out.ldc(name);        // argument after
            out.invokeinterface(CD_MemoryLayout, "withName", MethodTypeDesc.of(CD_String, CD_MemoryLayout));
        }
    }
    
    record StructLayoutExpr(Expr layoutsArray) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            layoutsArray.emit(out);
            out.invokestatic(CD_MemoryLayout, "structLayout", MethodTypeDesc.of(CD_MemoryLayout, CD_StructLayout));
        }
    }
    
    record ArrayExpr(ClassDesc elementInternalName, List<Expr> elements) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            // new MemoryLayout[elements.size()]
            out.iconst(elements.size());
            out.anewarray(elementInternalName);
            // stack: [array]

            for (int i = 0; i < elements.size(); i++) {
                out.dup();          // preserve array
                out.iconst(i);      // index
                elements.get(i).emit(out); // element value
                out.aastore();      // consumes one array ref
            }
            // stack: [array]
        }
    }
    
    record ValueLayoutWithNameExpr(String valueLayoutField, String name) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            out.getstatic(CD_ValueLayout, valueLayoutField, CD_ValueLayout);
            out.ldc(name);
            out.invokeinterface(CD_ValueLayout, "withName", MethodTypeDesc.of(CD_String, CD_MemoryLayout));
        }
    }
    
    record PaddingLayoutExpr(long size) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            out.ldc2(size);
            out.invokestatic(CD_MemoryLayout, "paddingLayout", MethodTypeDesc.of(CD_MemoryLayout, CD_long));
        }
    }


    void emit(CodeEmitter out);
}
