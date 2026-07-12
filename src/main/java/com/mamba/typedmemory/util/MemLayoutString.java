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

package com.mamba.typedmemory.util;

import com.mamba.typedmemory.api.MemLayout;
import com.mamba.typedmemory.layout.LayoutRules;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.PaddingLayout;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.UnionLayout;
import java.lang.foreign.ValueLayout;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Formats memory layouts and generated VarHandle declarations.
 *
 * @param layout the layout being formatted
 * @param stringLayout the formatted layout source text
 */
public record MemLayoutString(MemoryLayout layout, String stringLayout) implements LayoutRules{
    
    /**
     * Formats the primary layout from a {@link MemLayout}.
     *
     * @param memoryLayout the layout descriptor to format
     * @return a formatted layout representation
     */
    public static MemLayoutString of(MemLayout memoryLayout) {
        return of(memoryLayout.layout(), 0);
    }
    
    private static MemLayoutString of(MemoryLayout memoryLayout, int indent) {
        StringBuilder builderGroup = new StringBuilder();
        String indentStr = " ".repeat(indent);

        switch (memoryLayout) {
            case GroupLayout group -> {
                String groupType = switch (group) {
                    case StructLayout _ -> "MemoryLayout.structLayout";
                    case UnionLayout _ -> "MemoryLayout.unionLayout";
                };
                builderGroup.append(indentStr).append(groupType).append("(\n");
                for (MemoryLayout mem : group.memberLayouts()) {
                    builderGroup.append(of(mem, indent + 4).stringLayout()) // Set isNested to true for inner members
                        .append(",\n");
                }
                if (!group.memberLayouts().isEmpty()) 
                    builderGroup.deleteCharAt(builderGroup.length() - 2); // Remove the last comma
                builderGroup.append(indentStr).append(")").append(withNameAppend(group));
            }
            case SequenceLayout seqLayout -> builderGroup
                .append(indentStr)
                .append("MemoryLayout.sequenceLayout(")
                .append(seqLayout.elementCount())
                .append(",\n")
                .append(of(seqLayout.elementLayout(), indent + 4).stringLayout()) // Set isNested to true
                .append("\n")
                .append(indentStr)
                .append(")")
                .append(withNameAppend(seqLayout));
            case ValueLayout valueLayout -> builderGroup
                .append(indentStr)
                .append("ValueLayout.")
                .append(valueLayoutString(valueLayout.carrier()))
                .append(withNameAppend(valueLayout)); // Allow names for ValueLayout
            case PaddingLayout paddingLayout -> builderGroup
                .append(indentStr)
                .append("MemoryLayout.paddingLayout(")
                .append(paddingLayout.byteSize())
                .append(")");
        }
        return new MemLayoutString(memoryLayout, builderGroup.toString());
    }
    
    
    /**
     * Returns generated VarHandle field declarations for this layout.
     *
     * @return VarHandle field declarations
     */
    public List<String> varHandleFields(){
        List<String> varFields = new ArrayList<>();
        varHandleFields(layout, new ArrayDeque<>(varHandleNames()), new ArrayDeque<>(), varFields);
        return varFields;
    }
    
    /**
     * Formats generated VarHandle field declarations separated by newlines.
     *
     * @return formatted VarHandle field declarations
     */
    public String formatVarHandleFields(){
        StringBuilder builder = new StringBuilder();
        for(String s : varHandleFields())
            builder.append(s).append("\n");
        return builder.toString();
    }
    
    private void varHandleFields(MemoryLayout memoryLayout, Deque<String> varNames, Deque<String> vhFieldsStack, List<String> fields){       
        switch (memoryLayout) {
            case GroupLayout group -> { 
                for (MemoryLayout mem : group.memberLayouts()) {
                    switch(mem){
                        case ValueLayout v ->{        
                            StringJoiner joiner = new StringJoiner(",");
                            for(String st : vhFieldsStack)
                                joiner.add(st);                            
                            fields.add("public static final VarHandle " +varNames.removeFirst()+ " = layout.varHandle(" +joiner+ ",PathElement.groupElement(" +v.name().get()+ "));");                              
                        }
                        case SequenceLayout s ->{
                            vhFieldsStack.add("PathElement.groupElement(" +s.name().get()+ ")");
                            varHandleFields(s, varNames, vhFieldsStack, fields);
                            vhFieldsStack.removeLast();
                        }
                        case GroupLayout g -> {
                            vhFieldsStack.add("PathElement.groupElement(" +g.name().get()+ ")");
                            varHandleFields(g, varNames, vhFieldsStack, fields);
                            vhFieldsStack.removeLast();
                        }
                        default ->{}
                    }                     
                }
            }
            case SequenceLayout seqLayout -> {
                vhFieldsStack.add("PathElement.sequenceElement()");
                switch(seqLayout.elementLayout()){                    
                    case ValueLayout _ ->{
                        StringJoiner joiner = new StringJoiner(",");
                            for(String st : vhFieldsStack)
                                joiner.add(st);                            
                        fields.add("public static final VarHandle " +varNames.removeFirst()+ " = layout.varHandle(" +joiner+ ");");                        
                    }
                    case GroupLayout g -> varHandleFields(g, varNames, vhFieldsStack, fields);
                    default ->{}
                }  
                vhFieldsStack.removeLast();
            }           
            default -> {}
        }
    }
    
    /**
     * Returns generated VarHandle names as a deque.
     *
     * @return generated VarHandle names
     */
    public Deque<String> varHandleNamesDeque(){
        return new ArrayDeque<>(varHandleNames());
    }
    
    /**
     * Returns generated VarHandle names for fields in this layout.
     *
     * @return generated VarHandle names
     */
    public List<String> varHandleNames() {
        List<String> handleNames = new LinkedList<>();
        Deque<String> currentHandleName = new LinkedList<>();
        currentHandleName.push("Handle");
        currentHandleName.push(layout.getClass().getSimpleName());
        varHandleNames(layout, handleNames, currentHandleName);
        return handleNames;
    }
    
    private void varHandleNames(MemoryLayout memoryLayout, List<String> handleNames, Deque<String> currentHandleName) {        
        switch (memoryLayout) {
            case GroupLayout group -> {   
                boolean pushed = group.name().isPresent();
                if(pushed)
                    currentHandleName.push(firstLetterCapital(group.name().get()));
                for (MemoryLayout mem : group.memberLayouts()) 
                    varHandleNames(mem, handleNames, currentHandleName);         
                if(pushed)
                    currentHandleName.pop();
            }
            case SequenceLayout seqLayout -> {
                if(seqLayout.name().isPresent() && seqLayout.elementLayout().name().isPresent())
                    currentHandleName.push(firstLetterCapital(seqLayout.name().get())); //maybe group
                else
                    currentHandleName.push(seqLayout.name().get()); //maybe is @array(value = ...)int/primitive[] var                              
                varHandleNames(seqLayout.elementLayout(), handleNames, currentHandleName);   
                currentHandleName.pop();
            }
            case ValueLayout valueLayout -> {
                boolean pushed = valueLayout.name().isPresent();
                if(pushed)
                    currentHandleName.push(valueLayout.name().get());
                handleNames.add(String.join("", currentHandleName));
                if(pushed)
                    currentHandleName.pop();
            }
            case PaddingLayout _ -> {}
        }
    }
    
    private static String withNameAppend(MemoryLayout memoryLayout) {
        if(memoryLayout.name().isPresent()) //might be a value layout in a sequence which don't have names (name is in sequence only)
            return ".withName(\"" + memoryLayout.name().get() + "\")";
        else 
            return  "";
             
    }
    
    private static String valueLayoutString(Class<?> componentType) {
        Objects.requireNonNull(componentType);
        
        return switch (componentType.getSimpleName()) {
            case "char" -> "JAVA_CHAR";
            case "boolean" -> "JAVA_BOOLEAN";
            case "byte" -> "JAVA_BYTE";
            case "short" -> "JAVA_SHORT";
            case "int" -> "JAVA_INT";
            case "float" -> "JAVA_FLOAT";
            case "long" -> "JAVA_LONG";
            case "double" -> "JAVA_DOUBLE";
            default -> throw new IllegalArgumentException("Unknown primitive type");
        };
    }
}
