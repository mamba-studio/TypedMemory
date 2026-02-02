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
import static test.mir.Helper.CD_PathElement;
import static test.mir.Helper.CD_StructLayout;
import static test.mir.Helper.CD_ValueLayout;
import static test.mir.Helper.CD_VarHandle;

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
    
    record StructLayoutExpr(ArrayExpr layoutsArray) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            layoutsArray.emit(out);
            out.invokestatic(CD_MemoryLayout, "structLayout", MethodTypeDesc.of(CD_MemoryLayout, CD_StructLayout.arrayType()));
        }
    }
    
    record GetStaticExpr(ClassDesc owner, String fieldName,  ClassDesc fieldDesc) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            out.getstatic(owner, fieldName, fieldDesc);
        }
    }
    
    record VarHandleExpr(Expr layoutExpr, ArrayExpr pathElements) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            layoutExpr.emit(out);      // push receiver
            pathElements.emit(out);
            out.invokeinterface(CD_MemoryLayout, "varHandle", MethodTypeDesc.of(CD_VarHandle, CD_PathElement.arrayType()));
        }        
    }
    
    sealed interface PathElementExpr extends Expr{
        record GroupElementExpr(String name) implements PathElementExpr {
            @Override
            public void emit(CodeEmitter out) {
                out.ldc(name);
                out.invokestatic(CD_PathElement, "groupElement", MethodTypeDesc.of(CD_PathElement, CD_String));
            }
        }
        
        record SequenceElementExpr() implements PathElementExpr {
            @Override
            public void emit(CodeEmitter out) {
                out.invokestatic(CD_PathElement, "sequenceElement", MethodTypeDesc.of(CD_PathElement));
            }
        }
    }
    
    
    record ArrayExpr(NewArrayExpr alloc, ArrayInitExpr init) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            // new MemoryLayout[elements.size()]
            alloc.emit(out);
            // stack: [array]

            init.emit(out);
            // stack: [array]
        }
    }
    
    record NewArrayExpr(ClassDesc elementInternalName, int size) implements Expr{
        @Override
        public void emit(CodeEmitter out) {
            out.iconst(size);
            out.anewarray(elementInternalName);
        }        
    }
    
    record ArrayInitExpr(List<Expr> elements) implements Expr{
        @Override
        public void emit(CodeEmitter out) {
            for (int i = 0; i < elements.size(); i++) {
                out.dup();          // preserve array
                out.iconst(i);      // index
                elements.get(i).emit(out); // value
                out.aastore();
            }
            // array remains on stack
        }     
    }
        
    record ValueLayoutExpr(String valueLayoutField) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            out.getstatic(CD_ValueLayout, valueLayoutField, CD_ValueLayout);
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
