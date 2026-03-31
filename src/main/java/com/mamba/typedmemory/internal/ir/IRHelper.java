package com.mamba.typedmemory.internal.ir;

import com.mamba.typedmemory.internal.emitter.CodeEmitter;
import static java.lang.constant.ConstantDescs.INIT_NAME;

import module java.base;



/**
 *
 * @author joemw
 */
public class IRHelper {
    public static final ClassDesc CD_MemoryLayout   = ClassDesc.of(MemoryLayout.class.getName());
    public static final ClassDesc CD_MemorySegment  = ClassDesc.of(MemorySegment.class.getName());
    public static final ClassDesc CD_StructLayout   = ClassDesc.of(StructLayout.class.getName());
    public static final ClassDesc CD_ValueLayout    = ClassDesc.of(ValueLayout.class.getName());
    public static final ClassDesc CD_PathElement    = ClassDesc.of(MemoryLayout.PathElement.class.getName());
    public static final ClassDesc CD_Record         = ClassDesc.ofDescriptor(Record.class.descriptorString());
    public static final ClassDesc CD_PaddingLayout  = ClassDesc.ofDescriptor(PaddingLayout.class.descriptorString());
    
    public enum JVMType {
        INT_LIKE, LONG, FLOAT, DOUBLE, REFERENCE
    }
    
    public enum InvokeKind {
        VIRTUAL, STATIC, INTERFACE, SPECIAL
    }

    public record LocalInfo(int slot, JVMType type) {}
    
    public static JVMType classify(Class<?> classType) {
        Objects.requireNonNull(classType);
        return switch (classType) {
            case Class<?> c when c == long.class   -> JVMType.LONG;
            case Class<?> c when c == double.class -> JVMType.DOUBLE;
            case Class<?> c when c == float.class  -> JVMType.FLOAT;
            case Class<?> c when c.isPrimitive()   -> JVMType.INT_LIKE;
            default -> JVMType.REFERENCE;
        };
    }
    
    public static void emitLoad(CodeEmitter out, JVMType type, int slot) {
        switch (type) {
            case INT_LIKE -> out.iload(slot);
            case LONG     -> out.lload(slot);
            case FLOAT    -> out.fload(slot);
            case DOUBLE   -> out.dload(slot);
            case REFERENCE-> out.aload(slot);
        }
    }
    
    public static void emitStore(CodeEmitter out, JVMType type, int slot) {
        switch (type) {
            case INT_LIKE -> out.storeLocal(TypeKind.INT, slot);
            case LONG     -> out.storeLocal(TypeKind.LONG, slot);
            case FLOAT    -> out.storeLocal(TypeKind.FLOAT, slot);
            case DOUBLE   -> out.storeLocal(TypeKind.DOUBLE, slot);
            case REFERENCE-> out.storeLocal(TypeKind.REFERENCE, slot);
        }
    }
    
    public static String valueLayoutConstant(ValueLayout v) {
        return switch (v) {
            case ValueLayout.OfByte     _ -> "JAVA_BYTE";
            case ValueLayout.OfShort    _ -> "JAVA_SHORT";
            case ValueLayout.OfInt      _ -> "JAVA_INT";
            case ValueLayout.OfLong     _ -> "JAVA_LONG";
            case ValueLayout.OfFloat    _ -> "JAVA_FLOAT";
            case ValueLayout.OfDouble   _ -> "JAVA_DOUBLE";
            case ValueLayout.OfChar     _ -> "JAVA_CHAR";
            case ValueLayout.OfBoolean  _ -> "JAVA_BOOLEAN";
            case AddressLayout _          -> "ADDRESS";
        };
    }
    
    public static ClassDesc valueLayoutClassDesc(ValueLayout v) {
        return switch (v) {
            case ValueLayout.OfByte     _ -> ClassDesc.of("java.lang.foreign.ValueLayout$OfByte");
            case ValueLayout.OfShort    _ -> ClassDesc.of("java.lang.foreign.ValueLayout$OfShort");
            case ValueLayout.OfInt      _ -> ClassDesc.of("java.lang.foreign.ValueLayout$OfInt");
            case ValueLayout.OfLong     _ -> ClassDesc.of("java.lang.foreign.ValueLayout$OfLong");
            case ValueLayout.OfFloat    _ -> ClassDesc.of("java.lang.foreign.ValueLayout$OfFloat");
            case ValueLayout.OfDouble   _ -> ClassDesc.of("java.lang.foreign.ValueLayout$OfDouble");
            case ValueLayout.OfChar     _ -> ClassDesc.of("java.lang.foreign.ValueLayout$OfChar");
            case ValueLayout.OfBoolean  _ -> ClassDesc.of("java.lang.foreign.ValueLayout$OfBoolean");
            case AddressLayout          _ -> ClassDesc.of("java.lang.foreign.AddressLayout");
        };
    }
    
    
    public static int firstFreeSlot(boolean isStatic, Class<?>... parameterTypes) {
        int slot = isStatic ? 0 : 1; // skip 'this' if instance

        for (Class<?> p : parameterTypes) {
            if (p == long.class || p == double.class) {
                slot += 2;
            } else {
                slot += 1;
            }
        }

        return slot;
    }
    
    public static MethodTypeDesc constructorRecordTypeDesc(Class<? extends Record> recordType) {

        var components = recordType.getRecordComponents();
        var paramDescs = new ClassDesc[components.length];

        for (int i = 0; i < components.length; i++) 
            paramDescs[i] = ClassDesc.ofDescriptor((components[i].getType().descriptorString()));

        return MethodTypeDesc.of(ConstantDescs.CD_void, paramDescs);
    }
    
    //opcode for instantiating a record "new RecordType(t1..., tn);"
    public static Stmt emitRecordConstructorCall(Class<? extends Record> type, List<LocalInfo> args) {    
        return new Stmt.SimpleStmt(out -> {
                        var desc = ClassDesc.of(type.getName());
                
                        out.new_(desc);
                        out.dup();
                
                        for (LocalInfo local : args)
                            emitLoad(out, local.type(), local.slot());
                
                        out.invokespecial(desc, INIT_NAME, constructorRecordTypeDesc(type));
        });
    }
    
    public static MethodTypeDesc methodTypeDesc(Class<?> owner, String name, Class<?>... params) {
        try {
            var method = owner.getDeclaredMethod(name, params);
            return MethodTypeDesc.ofDescriptor(
                    MethodType.methodType(
                            method.getReturnType(),
                            method.getParameterTypes()
                    ).descriptorString()
            );
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
