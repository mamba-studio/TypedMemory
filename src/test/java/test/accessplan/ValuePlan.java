/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package test.accessplan;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;

/**
 *
 * @author joemw
 */
public sealed interface ValuePlan {
    record FieldPlan(ValuePlan valuePlan) {
        long offset() { return valuePlan.offset(); }
        String name() { return valuePlan.name(); }
    }
    
    record PrimitivePlan(String name, Class<?> javaType, 
        long offset, long byteSize, VarHandle varHandle) implements ValuePlan {}
    
    record RecordValuePlan(String name, Class<?> javaType, 
        long offset, long byteSize, MethodHandle constructor, FieldPlan[] fields) implements ValuePlan {}
    
    record ArrayPlan(String name, Class<?> javaType, 
        long offset, long byteSize, int length, long elementStride, ValuePlan elementPlan) implements ValuePlan {}
    
    String name();
    Class<?> javaType();
    long offset();
    long byteSize();
}
