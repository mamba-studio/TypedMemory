/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.exprtest;

import com.mamba.typedmemory.internal.ir.IRHelper.JVMType;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

/**
 *
 * @author joemw
 */
public class LocalAllocator {    
    public record LocalBinding(int slot, JVMType kind, String name) {
        public LocalBinding(int slot, JVMType kind) {
            this(slot, kind, "");
        }
        
        public LocalBinding named(String name) {
            return new LocalBinding(slot, kind, name);
        }
    }
    
    public static final LocalBinding THIS = new LocalBinding(0, JVMType.REFERENCE, "this");
    private int nextSlot;

    public LocalAllocator(boolean isStatic, MethodTypeDesc methodType) {
        this.nextSlot = computeStartSlot(isStatic, methodType);
    }

    public LocalBinding allocate(JVMType kind) {
        return allocate(kind, "");
    }

    public LocalBinding allocate(JVMType kind, String name) {
        int slot = nextSlot;
        nextSlot += width(kind);
        return new LocalBinding(slot, kind, name);
    }
    
    public int nextSlot() {
        return nextSlot;
    }
    
    private static int computeStartSlot(boolean isStatic, MethodTypeDesc methodType) {
        int slot = isStatic ? 0 : 1;

        for (int i = 0; i < methodType.parameterCount(); i++) {
            slot += width(methodType.parameterType(i));
        }
        return slot;
    }

    private static int width(JVMType kind) {
        return switch (kind) {
            case LONG, DOUBLE -> 2;
            default -> 1;
        };
    }

    private static int width(ClassDesc type) {
        return switch (type.descriptorString()) {
            case "J", "D" -> 2;
            default -> 1;
        };
    }
}
