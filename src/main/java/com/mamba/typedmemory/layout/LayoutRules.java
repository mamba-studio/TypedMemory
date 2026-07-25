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

package com.mamba.typedmemory.layout;

import java.util.Objects;
import java.lang.foreign.*;

/// Shared layout helper operations used while deriving record memory layouts.
public interface LayoutRules {
    
    /// Computes the next offset aligned to the requested byte boundary.
    ///
    /// @param offset the current byte offset
    /// @param align the byte alignment; zero leaves {@code offset} unchanged
    /// @return the aligned byte offset
    /// @throws IllegalArgumentException if {@code align} is negative
    public static long computeAlignmentOffset(long offset, long align) {
        if (align == 0L) 
            return offset;        
        if (align > 0) 
            return (offset + align - 1) & -align;
        throw new IllegalArgumentException(
            "Alignment must be non-negative: " + align
        );       
    }
    
    /// Reports whether a value is a positive power of two.
    ///
    /// @param value the value to test
    /// @return {@code true} when {@code value} is a power of two
    public static boolean isPowerOfTwo(long value) {
        return value > 0 && (value & (value - 1)) == 0;
    }  
        
    
    /// Returns the FFM value layout for a primitive component type.
    ///
    /// @param componentType the primitive component type
    /// @return the matching value layout
    /// @throws IllegalArgumentException if {@code componentType} is not a
    ///         supported primitive type
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
    
    /// Returns the byte size for a primitive component type.
    ///
    /// @param componentType the primitive component type
    /// @return the primitive byte size
    /// @throws IllegalArgumentException if {@code componentType} is not a
    ///         supported primitive type
    /// @throws NullPointerException if {@code componentType} is null
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
    
    /// Returns a string with its first character converted to upper case.
    ///
    /// @param str the string to convert
    /// @return {@code str} with an upper-case first character
    /// @throws NullPointerException if {@code str} is null
    default String firstLetterCapital(String str) {      
        return switch (str) {
            case null   -> throw new NullPointerException("string is null"); // Handle null explicitly
            case ""     -> "";     // Handle empty string
            default     -> str.substring(0, 1).toUpperCase() + str.substring(1);
        };      
    }
    
    /// Returns a string with its first character converted to lower case.
    ///
    /// @param str the string to convert
    /// @return {@code str} with a lower-case first character
    /// @throws NullPointerException if {@code str} is null
    default String firstLetterSmall(String str) {
        return switch (str) {
            case null -> throw new NullPointerException("String is null");
            case "" -> "";
            default -> str.substring(0, 1).toLowerCase() + str.substring(1);
        };
    }        
}
