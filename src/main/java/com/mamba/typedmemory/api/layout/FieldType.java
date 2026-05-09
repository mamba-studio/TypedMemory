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

/**
 *
 * @author user
 */
import static com.mamba.typedmemory.api.layout.LayoutRules.computeAlignmentOffset;
import java.lang.foreign.ValueLayout;
import java.lang.reflect.RecordComponent;
import java.util.Optional;
import com.mamba.typedmemory.api.size;

public sealed interface FieldType extends LayoutRules{
    
    public record MemSize(long endOffset, long size) implements LayoutRules{
        public MemSize(){this(0, 0);}
        public MemSize(long size){this(size, size);}

        public MemSize mul(long multiplier){return new MemSize(endOffset * multiplier, size * multiplier);}
        public long padding(){return size() - endOffset();}
        public boolean hasPadding(){return padding() > 0;}    
    }
    
    //field types (record, array and primitives)
    
    public record PrimitiveField(String name, Class<?> type) implements FieldType {
        public int primitiveByteSize(){
            return primitiveByteSize(type);
        }
        
        public ValueLayout valueLayout(){
            return valueLayout(type);
        }
        
        public PrimitiveField asName(String name){
            return new PrimitiveField(name, type);
        }
    }
    public record RecordField(String name, Class<? extends Record> type) implements FieldType{    
        
        public String typeName(){
            return type.getSimpleName();
        }
        
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
    public record ArrayField(String name, Class<?> type, Class<?> componentType, long size) implements FieldType {}
    
    String name();
    Class<?> type();
    
    default FieldType withFirstLetterSmallName(){
        return switch (this) {
            case PrimitiveField(var name, var type)                         -> new PrimitiveField(firstLetterSmall(name), type);
            case RecordField(var name, var type)                            -> new RecordField(firstLetterSmall(name), type);
            case ArrayField(var name, var type, var componentType, var size)-> new ArrayField(firstLetterSmall(name), type, componentType, size);            
        };
    }
   
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
        
    // We don't check the array size, but all components of types including of types in array (an array is a homogenous aggregate - one type)
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

    public static FieldType of(Class<?> type, String name) {
        return switch (type) {
            case Class<?> primitive when primitive.isPrimitive()            -> new PrimitiveField(name, primitive);            
            case Class<?> record when record.isRecord()                     -> new RecordField(name, (Class<? extends Record>) record);
            case Class<?> array when array.isArray()                        -> throw new UnsupportedOperationException("Field " +name+ ": " +type.getName()+ " should be called where the parent is a record, and hence array should not be encountered in this method call");
            default                                                         -> throw new UnsupportedOperationException("Unsupported field type for field '" + name + "': " + type.getName() +". Only primitives, records, and arrays are supported.");
        };
    }
}


