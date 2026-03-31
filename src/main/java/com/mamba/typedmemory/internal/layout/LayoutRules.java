package com.mamba.typedmemory.internal.layout;

import java.util.Objects;
import java.lang.foreign.*;

/**
 *
 * @author user
 */
public interface LayoutRules {
    
    public static long computeAlignmentOffset(long offset, long align) {
        if (align == 0L) 
            return offset;        
        if (align > 0) 
            return (offset + align - 1) & -align;
        throw new IllegalArgumentException(
            "Alignment must be non-negative: " + align
        );       
    }
    
    public static boolean isPowerOfTwo(long value) {
        return value > 0 && (value & (value - 1)) == 0;
    }  
        
    
    default ValueLayout valueLayout(Class<?> componentType) {
        return switch (componentType.getSimpleName()) {
            case "char" -> ValueLayout.JAVA_CHAR;
            case "boolean" -> ValueLayout.JAVA_BOOLEAN;
            case "byte" -> ValueLayout.JAVA_BYTE;
            case "short" -> ValueLayout.JAVA_SHORT;
            case "int" -> ValueLayout.JAVA_INT;
            case "float" -> ValueLayout.JAVA_FLOAT;
            case "long" -> ValueLayout.JAVA_LONG;
            case "double" -> ValueLayout.JAVA_DOUBLE;                  
            default -> throw new IllegalArgumentException("Unknown primitive type");
        };
    }
    
    default int primitiveByteSize(Class<?> componentType) { 
        Objects.requireNonNull(componentType);
        
        return switch (componentType.getTypeName()) {
            case "boolean", "byte" -> 1;
            case "short", "char" -> 2;
            case "int", "float" -> 4;
            case "long", "double" -> 8;
            default -> throw new IllegalArgumentException("Unknown primitive type: " +componentType);
        };
    }
    
    // Helper method to capitalise the first letter of a string
    default String firstLetterCapital(String str) {      
        return switch (str) {
            case null   -> throw new NullPointerException("string is null"); // Handle null explicitly
            case ""     -> "";     // Handle empty string
            default     -> str.substring(0, 1).toUpperCase() + str.substring(1);
        };      
    }
    
    default String firstLetterSmall(String str) {
        return switch (str) {
            case null -> throw new NullPointerException("String is null");
            case "" -> "";
            default -> str.substring(0, 1).toLowerCase() + str.substring(1);
        };
    }        
}