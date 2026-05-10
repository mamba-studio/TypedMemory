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

package com.mamba.typedmemory.api.layout;

import static com.mamba.typedmemory.api.layout.LayoutRules.computeAlignmentOffset;
import java.lang.foreign.ValueLayout;
import java.lang.reflect.RecordComponent;
import java.util.Optional;
import com.mamba.typedmemory.api.size;

/**
 * Describes a record component type during memory layout derivation.
 */
public sealed interface FieldType extends LayoutRules{
    
    /**
     * Size information for a derived field or record layout.
     *
     * @param endOffset the byte offset after the last non-padding byte
     * @param size the total byte size including trailing padding
     */
    public record MemSize(long endOffset, long size) implements LayoutRules{
        /**
         * Creates a zero-size descriptor.
         */
        public MemSize(){this(0, 0);}

        /**
         * Creates a descriptor whose end offset equals its total size.
         *
         * @param size the total byte size
         */
        public MemSize(long size){this(size, size);}

        /**
         * Multiplies both offsets by a count.
         *
         * @param multiplier the multiplier to apply
         * @return the multiplied size descriptor
         */
        public MemSize mul(long multiplier){return new MemSize(endOffset * multiplier, size * multiplier);}

        /**
         * Returns the trailing padding byte count.
         *
         * @return trailing padding bytes
         */
        public long padding(){return size() - endOffset();}

        /**
         * Reports whether this size includes trailing padding.
         *
         * @return {@code true} when trailing padding is present
         */
        public boolean hasPadding(){return padding() > 0;}    
    }
    
    //field types (record, array and primitives)
    
    /**
     * A primitive record component.
     *
     * @param name the component name
     * @param type the primitive component type
     */
    public record PrimitiveField(String name, Class<?> type) implements FieldType {
        /**
         * Returns the byte size of this primitive field.
         *
         * @return the primitive byte size
         */
        public int primitiveByteSize(){
            return primitiveByteSize(type);
        }
        
        /**
         * Returns the FFM value layout for this primitive field.
         *
         * @return the primitive value layout
         */
        public ValueLayout valueLayout(){
            return valueLayout(type);
        }
        
        /**
         * Returns this field with a different component name.
         *
         * @param name the replacement component name
         * @return the renamed primitive field
         */
        public PrimitiveField asName(String name){
            return new PrimitiveField(name, type);
        }
    }

    /**
     * A nested record component.
     *
     * @param name the component name
     * @param type the nested record type
     */
    public record RecordField(String name, Class<? extends Record> type) implements FieldType{    
        
        /**
         * Returns the simple name of the nested record type.
         *
         * @return the record type name
         */
        public String typeName(){
            return type.getSimpleName();
        }
        
        /**
         * Computes the byte alignment required by this record.
         *
         * @return the maximum alignment required by its components
         */
        public long alignByteSize(){                 
            long maxFieldSize = 0;

            // Determine the largest field size, including nested arrays
            for (RecordComponent component : type.getRecordComponents()) {
                FieldType fieldType = FieldType.of(component);
                long fieldSize = maxTypeSize(fieldType); 
                maxFieldSize = Math.max(maxFieldSize, fieldSize);
            }

            return maxFieldSize;
        }
        
        /**
         * Computes the byte size for this record, including trailing padding.
         *
         * @return the record size descriptor
         */
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

    /**
     * A fixed-size array record component.
     *
     * @param name the component name
     * @param type the array type
     * @param componentType the array component type
     * @param size the fixed array length
     */
    public record ArrayField(String name, Class<?> type, Class<?> componentType, long size) implements FieldType {}
    
    /**
     * Returns the component name.
     *
     * @return the component name
     */
    String name();

    /**
     * Returns the component type.
     *
     * @return the component type
     */
    Class<?> type();
    
    /**
     * Returns this field type with its component name starting with lower case.
     *
     * @return the renamed field type
     */
    default FieldType withFirstLetterSmallName(){
        return switch (this) {
            case PrimitiveField(var name, var type)                         -> new PrimitiveField(firstLetterSmall(name), type);
            case RecordField(var name, var type)                            -> new RecordField(firstLetterSmall(name), type);
            case ArrayField(var name, var type, var componentType, var size)-> new ArrayField(firstLetterSmall(name), type, componentType, size);            
        };
    }
   
    /**
     * Computes the byte size for a field type.
     *
     * @param fieldType the field type to measure
     * @return the field size descriptor
     */
    public static MemSize size(FieldType fieldType){
        return switch (fieldType) {
            case PrimitiveField p                                                       -> new MemSize(p.primitiveByteSize()); // Element primitive size × array size
            case RecordField r                                                          -> r.byteSize(); // Element record size × array size
            case ArrayField(    var  name, var    _, var componentType, var arraySize)  -> {
                                                        FieldType elementType = FieldType.of(componentType, name);
                                                        yield size(elementType).mul(arraySize); //multiply with array size
                                                    }            
        };
    }
        
    /**
     * Computes the maximum primitive or record alignment size used by a field.
     *
     * @param fieldType the field type to inspect
     * @return the maximum nested type size
     */
    public static long maxTypeSize(FieldType fieldType){
        return switch (fieldType) {
            case PrimitiveField p                                       -> p.primitiveByteSize();
            case RecordField r                                          -> r.alignByteSize();
            case ArrayField(var name, var _, var componentType, var _)  -> {
                FieldType elementType = FieldType.of(componentType, name);
                long elementSize = maxTypeSize(elementType); 
                yield elementSize;
            }
        };
    }

    /**
     * Creates a field type descriptor from a record component.
     *
     * @param component the record component to describe
     * @return the field type descriptor
     * @throws IllegalStateException if an array component is missing
     *         {@link size} or declares a non-positive length
     * @throws UnsupportedOperationException if the component type is unsupported
     */
    public static FieldType of(RecordComponent component) {
        Class<?> type = component.getType();
        String name = component.getName();
        
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
            case Class<?> record when Record.class.isAssignableFrom(record)         -> new RecordField(name, (Class<? extends Record>) record);
            case Class<?> array when array.isArray() && arrayAnnotation.isPresent() -> new ArrayField(name, array, array.getComponentType(), arrayAnnotation.get().value());
            default                                                                 -> throw new UnsupportedOperationException("Unsupported field type for field '" + name + "': " + type.getName() + ". Only primitives, records, and arrays are supported.");            
        };
    }

    /**
     * Creates a field type descriptor for a non-array type and name.
     *
     * @param type the component type
     * @param name the component name
     * @return the field type descriptor
     * @throws UnsupportedOperationException if the type is unsupported or is an
     *         array outside record-component analysis
     */
    public static FieldType of(Class<?> type, String name) {
        return switch (type) {
            case Class<?> primitive when primitive.isPrimitive()            -> new PrimitiveField(name, primitive);            
            case Class<?> record when record.isRecord()                     -> new RecordField(name, (Class<? extends Record>) record);
            case Class<?> array when array.isArray()                        -> throw new UnsupportedOperationException("Field " +name+ ": " +type.getName()+ " should be called where the parent is a record, and hence array should not be encountered in this method call");
            default                                                         -> throw new UnsupportedOperationException("Unsupported field type for field '" + name + "': " + type.getName() +". Only primitives, records, and arrays are supported.");
        };
    }
}


