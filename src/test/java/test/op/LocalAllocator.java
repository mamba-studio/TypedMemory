package test.op;

import com.mamba.typedmemory.internal.ir.IRHelper.JVMType;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 *
 * @author joemw
 */
public class LocalAllocator {    
    public record AllocatedLocal(int slot, JVMType kind, String name) {}

    private int nextSlot;
    private final Deque<Integer> marks = new ArrayDeque<>();

    public LocalAllocator(boolean isStatic, MethodTypeDesc methodType) {
        this.nextSlot = computeStartSlot(isStatic, methodType);
    }

    public void enterScope() {
        marks.push(nextSlot);
    }

    public void exitScope() {
        nextSlot = marks.pop();
    }
    
    public static final AllocatedLocal THIS = new AllocatedLocal(0, JVMType.REFERENCE, "this");

    public AllocatedLocal allocate(JVMType kind, String name) {
        int slot = nextSlot;
        nextSlot += width(kind);
        return new AllocatedLocal(slot, kind, name);
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