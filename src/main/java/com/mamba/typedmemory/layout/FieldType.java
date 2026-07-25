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

import com.mamba.typedmemory.api.Ptr;
import com.mamba.typedmemory.api.RawMem;
import com.mamba.typedmemory.api.align;
import com.mamba.typedmemory.api.size;
import static com.mamba.typedmemory.layout.LayoutRules.computeAlignmentOffset;
import static com.mamba.typedmemory.layout.LayoutRules.isPowerOfTwo;
import java.lang.foreign.ValueLayout;
import java.lang.reflect.GenericSignatureFormatError;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.Optional;

/// Describes a record component type during memory layout derivation.
public sealed interface FieldType extends LayoutRules{
    
    /// Size information for a derived field or record layout.
    ///
    /// @param endOffset the byte offset after the last non-padding byte
    /// @param size the total byte size including trailing padding
    public record MemSize(long endOffset, long size) implements LayoutRules{
        /// Creates a zero-size descriptor.
        public MemSize(){this(0, 0);}

        /// Creates a descriptor whose end offset equals its total size.
        ///
        /// @param size the total byte size
        public MemSize(long size){this(size, size);}

        /// Multiplies both offsets by a count.
        ///
        /// @param multiplier the multiplier to apply
        /// @return the multiplied size descriptor
        public MemSize mul(long multiplier){return new MemSize(endOffset * multiplier, size * multiplier);}

        /// Returns the trailing padding byte count.
        ///
        /// @return trailing padding bytes
        public long padding(){return size() - endOffset();}

        /// Reports whether this size includes trailing padding.
        ///
        /// @return {@code true} when trailing padding is present
        public boolean hasPadding(){return padding() > 0;}    
    }
    
    //field types (record, array and primitives)
    
    /// A primitive record component.
    ///
    /// @param name the component name
    /// @param type the primitive component type
    public record PrimitiveField(String name, Class<?> type) implements FieldType {
        /// Returns the byte size of this primitive field.
        ///
        /// @return the primitive byte size
        public int primitiveByteSize(){
            return primitiveByteSize(type);
        }
        
        /// Returns the FFM value layout for this primitive field.
        ///
        /// @return the primitive value layout
        public ValueLayout valueLayout(){
            return valueLayout(type);
        }
        
        /// Returns this field with a different component name.
        ///
        /// @param name the replacement component name
        /// @return the renamed primitive field
        public PrimitiveField asName(String name){
            return new PrimitiveField(name, type);
        }
    }

    /// A nested record component.
    ///
    /// @param name the component name
    /// @param type the nested record type
    public record RecordField(String name, Class<? extends Record> type) implements FieldType{    
        
        /// Returns the simple name of the nested record type.
        ///
        /// @return the record type name
        public String typeName(){
            return type.getSimpleName();
        }
        
        /// Computes the byte alignment required by this record.
        ///
        /// @return the maximum alignment required by its components
        public long alignByteSize(){                 
            long maxFieldSize = 0;

            // Determine the largest field size, including nested arrays
            for (RecordComponent component : type.getRecordComponents()) {
                FieldType fieldType = FieldType.of(component);
                long fieldSize = maxTypeSize(fieldType); 
                maxFieldSize = Math.max(maxFieldSize, fieldSize);
            }

            align annotation = type.getAnnotation(align.class);
            if (annotation == null) {
                return maxFieldSize;
            }

            long requested = annotation.value();
            if (!isPowerOfTwo(requested)) {
                throw new IllegalArgumentException(
                        "@align value for " + type.getTypeName()
                        + " must be a positive power of two, but was "
                        + requested);
            }
            if (requested < maxFieldSize) {
                throw new IllegalArgumentException(
                        "@align value for " + type.getTypeName()
                        + " must not be smaller than its natural alignment "
                        + maxFieldSize + ", but was " + requested);
            }
            return requested;
        }
        
        /// Computes the byte size for this record, including trailing padding.
        ///
        /// @return the record size descriptor
        public MemSize byteSize(){        
            long offset = 0;
            long maxSize = alignByteSize();

            for (RecordComponent component : this.type.getRecordComponents()) {
                FieldType fieldType = FieldType.of(component);

                long fieldSize = size(fieldType).size(); // Array multiplier is for components            
                long alignSize = FieldType.maxTypeSize(fieldType);
                offset = computeAlignmentOffset(offset, alignSize);
                offset += fieldSize; // Add the field size            
            }

            // End offset
            long endOffset = offset; 
            // Apply preferred alignment for the whole record
            long size = computeAlignmentOffset(offset, maxSize); 
            return new MemSize(endOffset, size);
        }
    }    

    /// A fixed-size array record component.
    ///
    /// @param name the component name
    /// @param type the array type
    /// @param componentType the array component type
    /// @param size the fixed array length
    public record ArrayField(String name, Class<?> type, Class<?> componentType, long size) implements FieldType {}

    /// An untyped native pointer component.
    public record PtrField(String name) implements FieldType {
        @Override
        public Class<?> type() {
            return Ptr.class;
        }
    }

    /// A typed native pointer component.
    ///
    /// @param name the component name
    /// @param targetType the concrete record type referenced by the pointer
    public record RawMemField(String name, Class<? extends Record> targetType) implements FieldType {
        @Override
        public Class<?> type() {
            return RawMem.class;
        }
    }
    
    /// Returns the component name.
    ///
    /// @return the component name
    String name();

    /// Returns the component type.
    ///
    /// @return the component type
    Class<?> type();
    
    /// Returns this field type with its component name starting with lower case.
    ///
    /// @return the renamed field type
    default FieldType withFirstLetterSmallName(){
        return switch (this) {
            case PrimitiveField(var name, var type)                         -> new PrimitiveField(firstLetterSmall(name), type);
            case RecordField(var name, var type)                            -> new RecordField(firstLetterSmall(name), type);
            case ArrayField(var name, var type, var componentType, var size)-> new ArrayField(firstLetterSmall(name), type, componentType, size);
            case PtrField(var name)                                         -> new PtrField(firstLetterSmall(name));
            case RawMemField(var name, var targetType)                      -> new RawMemField(firstLetterSmall(name), targetType);
        };
    }
   
    /// Computes the byte size for a field type.
    ///
    /// @param fieldType the field type to measure
    /// @return the field size descriptor
    public static MemSize size(FieldType fieldType){
        return switch (fieldType) {
            case PrimitiveField p                                                       -> new MemSize(p.primitiveByteSize()); // Element primitive size × array size
            case RecordField r                                                          -> r.byteSize(); // Element record size × array size
            case PtrField _, RawMemField _                                              -> new MemSize(ValueLayout.ADDRESS.byteSize());
            case ArrayField(    var  name, var    _, var componentType, var arraySize)  -> {
                                                        FieldType elementType = FieldType.of(componentType, name);
                                                        yield size(elementType).mul(arraySize); //multiply with array size
                                                    }            
        };
    }
        
    /// Computes the maximum primitive or record alignment size used by a field.
    ///
    /// @param fieldType the field type to inspect
    /// @return the maximum nested type size
    public static long maxTypeSize(FieldType fieldType){
        return switch (fieldType) {
            case PrimitiveField p                                       -> p.primitiveByteSize();
            case RecordField r                                          -> r.alignByteSize();
            case PtrField _, RawMemField _                              -> ValueLayout.ADDRESS.byteAlignment();
            case ArrayField(var name, var _, var componentType, var _)  -> {
                FieldType elementType = FieldType.of(componentType, name);
                long elementSize = maxTypeSize(elementType); 
                yield elementSize;
            }
        };
    }

    /// Creates a field type descriptor from a record component.
    ///
    /// @param component the record component to describe
    /// @return the field type descriptor
    /// @throws IllegalStateException if an array component is missing
    ///         {@link size} or declares a non-positive length
    /// @throws UnsupportedOperationException if the component type is unsupported
    public static FieldType of(RecordComponent component) {
        Class<?> type = component.getType();
        String name = component.getName();

        if (type.isArray()
                && (type.getComponentType() == Ptr.class
                || type.getComponentType() == RawMem.class)) {
            throw new UnsupportedOperationException(
                    "Arrays of pointers are not supported for field '" + name
                    + "' in " + component.getDeclaringRecord().getTypeName()
                    + ". This includes Ptr[] and RawMem<T>[]; "
                    + "declare pointer fields individually.");
        }
        
        Optional<size> arrayAnnotation;

        if (type.isArray()) {
            size a = component.getAnnotation(size.class);
            if (a == null) {
                throw new IllegalStateException(
                    "@array annotation is not defined for field " + name +
                    " of type " + type.getTypeName() +
                    ", in " + component.getDeclaringRecord().getSimpleName() + " record"
                );
            }

            if (a.value() <= 0) {
                throw new IllegalStateException(
                    "Array size is required for field: " + name +
                    " of type " + type.getTypeName() +
                    ". Value provided is " + a.value()
                );
            }

            arrayAnnotation = Optional.of(a);
        } else {
            arrayAnnotation = Optional.empty();
        }
              
        return switch (type) {
            case Class<?> primitive when primitive.isPrimitive()                    -> new PrimitiveField(name, primitive);            
            case Class<?> record when Record.class.isAssignableFrom(record)         -> new RecordField(name, record.asSubclass(Record.class));
            case Class<?> array when array.isArray() && arrayAnnotation.isPresent() -> new ArrayField(name, array, array.getComponentType(), arrayAnnotation.get().value());
            case Class<?> pointer when pointer == Ptr.class                          -> new PtrField(name);
            case Class<?> rawMem when rawMem == RawMem.class                        -> new RawMemField(name, rawMemTarget(component));
            default                                                                 -> throw new UnsupportedOperationException("Unsupported field type for field '" + name + "': " + type.getName() + ". Only primitives, records, arrays, Ptr, and RawMem<Record> are supported.");            
        };
    }

    private static Class<? extends Record> rawMemTarget(RecordComponent component) {
        final Type genericType;
        try {
            genericType = component.getGenericType();
        } catch (GenericSignatureFormatError | TypeNotPresentException
                | MalformedParameterizedTypeException ex) {
            throw new IllegalArgumentException(
                    "Invalid generic signature for RawMem component '"
                    + component.getName() + "' in "
                    + component.getDeclaringRecord().getTypeName(), ex);
        }

        if (!(genericType instanceof ParameterizedType parameterized)
                || parameterized.getRawType() != RawMem.class) {
            throw invalidRawMemComponent(component, genericType);
        }

        final Type[] arguments;
        try {
            arguments = parameterized.getActualTypeArguments();
        } catch (TypeNotPresentException | MalformedParameterizedTypeException ex) {
            throw new IllegalArgumentException(
                    "Invalid type argument for RawMem component '"
                    + component.getName() + "' in "
                    + component.getDeclaringRecord().getTypeName(), ex);
        }

        if (arguments.length != 1
                || !(arguments[0] instanceof Class<?> target)
                || !target.isRecord()) {
            throw invalidRawMemComponent(component, genericType);
        }

        return target.asSubclass(Record.class);
    }

    private static IllegalArgumentException invalidRawMemComponent(
            RecordComponent component, Type genericType) {
        return new IllegalArgumentException(
                "RawMem component '" + component.getName() + "' in "
                + component.getDeclaringRecord().getTypeName()
                + " must declare one concrete record type, but was "
                + genericType.getTypeName());
    }

    /// Creates a field type descriptor for a non-array type and name.
    ///
    /// @param type the component type
    /// @param name the component name
    /// @return the field type descriptor
    /// @throws UnsupportedOperationException if the type is unsupported or is an
    ///         array outside record-component analysis
    public static FieldType of(Class<?> type, String name) {
        return switch (type) {
            case Class<?> primitive when primitive.isPrimitive()            -> new PrimitiveField(name, primitive);            
            case Class<?> record when record.isRecord()                     -> new RecordField(name, record.asSubclass(Record.class));
            case Class<?> array when array.isArray()                        -> throw new UnsupportedOperationException("Field " +name+ ": " +type.getName()+ " should be called where the parent is a record, and hence array should not be encountered in this method call");
            default                                                         -> throw new UnsupportedOperationException("Unsupported field type for field '" + name + "': " + type.getName() +". Only primitives, records, and arrays are supported.");
        };
    }
}


