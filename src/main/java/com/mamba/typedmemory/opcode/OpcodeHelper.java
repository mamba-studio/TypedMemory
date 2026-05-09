/*
 * Copyright 2026 joemw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mamba.typedmemory.opcode;

import com.mamba.typedmemory.opcode.emitter.CodeEmitter;

import module java.base;



/**
 *
 * @author joemw
 */
public class OpcodeHelper {
    public static final ClassDesc CD_MemoryLayout   = ClassDesc.of(MemoryLayout.class.getName());
    public static final ClassDesc CD_MemorySegment  = ClassDesc.of(MemorySegment.class.getName());
    public static final ClassDesc CD_StructLayout   = ClassDesc.of(StructLayout.class.getName());
    public static final ClassDesc CD_ValueLayout    = ClassDesc.of(ValueLayout.class.getName());
    public static final ClassDesc CD_SequenceLayout = ClassDesc.ofDescriptor(SequenceLayout.class.descriptorString());
    public static final ClassDesc CD_PathElement    = ClassDesc.of(MemoryLayout.PathElement.class.getName());
    public static final ClassDesc CD_Record         = ClassDesc.ofDescriptor(Record.class.descriptorString());
    public static final ClassDesc CD_PaddingLayout  = ClassDesc.ofDescriptor(PaddingLayout.class.descriptorString());
    public static final ClassDesc CD_Objects_       = ClassDesc.ofDescriptor(Objects.class.descriptorString());
    
    public enum JVMType {
        INT_LIKE, LONG, FLOAT, DOUBLE, REFERENCE
    }
    
    public enum InvokeKind {
        VIRTUAL, STATIC, INTERFACE, SPECIAL
    }
    
    public static JVMType jvmType(Class<?> classType) {
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
    
    public static TypeKind primitiveTypeKind(Class<?> primitiveType) {
        return switch (primitiveType.getName()) {
            case "boolean" -> TypeKind.BOOLEAN;
            case "byte" -> TypeKind.BYTE;
            case "short" -> TypeKind.SHORT;
            case "char" -> TypeKind.CHAR;
            case "int" -> TypeKind.INT;
            case "long" -> TypeKind.LONG;
            case "float" -> TypeKind.FLOAT;
            case "double" -> TypeKind.DOUBLE;
            default -> throw new IllegalArgumentException("Not a primitive type: " + primitiveType);
        };
    }
        
    public static MethodTypeDesc constructorRecordTypeDesc(Class<? extends Record> recordType) {
        var components = recordType.getRecordComponents();
        var paramDescs = new ClassDesc[components.length];

        for (int i = 0; i < components.length; i++) 
            paramDescs[i] = ClassDesc.ofDescriptor((components[i].getType().descriptorString()));

        return MethodTypeDesc.of(ConstantDescs.CD_void, paramDescs);
    }
        
    public static MethodTypeDesc methodTypeDesc(Class<?> owner, String name, Class<?>... params) {        
        try {
            var method = owner.getDeclaredMethod(name, params);
            return MethodTypeDesc.ofDescriptor(
                    MethodType.methodType(
                            method.getReturnType(),
                            method.getParameterTypes()
                    ).descriptorString());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } 
    }
}
