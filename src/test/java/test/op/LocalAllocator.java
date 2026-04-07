package test.op;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


import com.mamba.typedmemory.internal.ir.IRHelper.JVMType;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

/**
 *
 * @author joemw
 */
public class LocalAllocator {    
    public static final class LocalSymbol {
        private final String name;

        public LocalSymbol(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    
    public record AllocatedLocal(int slot, JVMType kind, LocalSymbol symbol) {
        public AllocatedLocal(int slot, JVMType kind, String name){
            this(slot, kind, new LocalSymbol(name));
        }
        public AllocatedLocal(int slot, JVMType kind) {
            this(slot, kind, new LocalSymbol(""));
        }
        
        public AllocatedLocal named(String name) {
            return new AllocatedLocal(slot, kind, new LocalSymbol(name));
        }
    }
    
    public static final AllocatedLocal THIS = new AllocatedLocal(0, JVMType.REFERENCE, "this");
    private int nextSlot;

    public LocalAllocator(boolean isStatic, MethodTypeDesc methodType) {
        this.nextSlot = computeStartSlot(isStatic, methodType);
    }

    public AllocatedLocal allocate(JVMType kind) {
        return allocate(kind, "");
    }

    public AllocatedLocal allocate(JVMType kind, String name) {
        int slot = nextSlot;
        nextSlot += width(kind);
        return new AllocatedLocal(slot, kind, name);
    }
    
    public AllocatedLocal allocate(JVMType kind, LocalSymbol symbol) {
        int slot = nextSlot;
        nextSlot += width(kind);
        return new AllocatedLocal(slot, kind, symbol);
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