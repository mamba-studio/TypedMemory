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
import com.mamba.typedmemory.layout.FieldType;
import com.mamba.typedmemory.layout.FieldType.ArrayField;
import com.mamba.typedmemory.layout.FieldType.PtrField;
import com.mamba.typedmemory.layout.FieldType.RawMemField;
import com.mamba.typedmemory.layout.FieldType.RecordField;
import com.mamba.typedmemory.layout.LayoutRules;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.PaddingLayout;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.UnionLayout;
import java.lang.foreign.ValueLayout;
import java.lang.reflect.RecordComponent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/// Formats memory layouts and generated VarHandle declarations.
///
/// @param layout the layout being formatted
/// @param stringLayout the formatted layout source text
public record MemLayoutString(MemoryLayout layout, String stringLayout) implements LayoutRules{
    /// Selects the branch glyphs used when rendering a type layout summary.
    public enum SummaryStyle {
        /// Uses plain ASCII branch glyphs for terminals or logs without Unicode
        /// tree support.
        ASCII("+-- ", "`-- ", "|   ", "    "),
        /// Uses Unicode box-drawing glyphs for compact tree summaries.
        UNICODE("\u251c\u2500\u2500 ", "\u2514\u2500\u2500 ", "\u2502   ", "    ");
        
        private final String branch;
        private final String lastBranch;
        private final String vertical;
        private final String indent;
        
        SummaryStyle(String branch, String lastBranch, String vertical, String indent) {
            this.branch = branch;
            this.lastBranch = lastBranch;
            this.vertical = vertical;
            this.indent = indent;
        }
        
        public String branch() {
            return branch;
        }
        
        public String lastBranch() {
            return lastBranch;
        }
        
        public String vertical() {
            return vertical;
        }
        
        public String indent() {
            return indent;
        }
    }
    
    
    /// Formats the primary layout from a {@link MemLayout}.
    ///
    /// @param memoryLayout the layout descriptor to format
    /// @return a formatted layout representation
    public static MemLayoutString of(MemLayout memoryLayout) {
        return of(memoryLayout.layout(), 0);
    }
    
    /// Formats a tree summary for the memory layout of a record type.
    ///
    /// @param type the record type to summarize
    /// @param style the branch style to use
    /// @return a human-readable type layout summary
    public static String typeSummary(Class<? extends Record> type, SummaryStyle style) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(style);
        var memLayout = MemLayout.of(type);
        var layout = memLayout.layout();
        var groupTypes = new HashMap<String, String>();
        collectGroupTypes(type, "", groupTypes);
        var sb = new StringBuilder();

        long total = layout.byteSize();
        sb.append(type.getSimpleName()).append(" [0..").append(total).append(") - ").append(formatLayoutBytes(total)).append("\n");
        appendLayoutTreeChildren(layout, sb, 0, "", groupTypes, "", style);

        return sb.toString();
    }
    
    /// Prints a tree summary for the memory layout of a record type.
    ///
    /// @param type the record type to summarize
    /// @param style the branch style to use
    public static void printTypeSummary(Class<? extends Record> type, SummaryStyle style) {
        IO.print(typeSummary(type, style));
    }
    
    private static void collectGroupTypes(Class<? extends Record> type, String path, Map<String, String> groupTypes) {
        for (RecordComponent component : type.getRecordComponents()) {
            String componentPath = path.isEmpty() ? component.getName() : path + "." + component.getName();
            switch (FieldType.of(component)) {
                case RecordField record -> {
                    groupTypes.put(componentPath, record.typeName());
                    collectGroupTypes(record.type(), componentPath, groupTypes);
                }
                case ArrayField(var _, var _, var componentType, var _) when Record.class.isAssignableFrom(componentType) -> {
                    Class<? extends Record> recordType = componentType.asSubclass(Record.class);
                    groupTypes.put(componentPath, recordType.getSimpleName());
                    collectGroupTypes(recordType, componentPath, groupTypes);
                }
                case PtrField _ -> groupTypes.put(componentPath, "Ptr");
                case RawMemField(var _, var targetType) -> groupTypes.put(
                        componentPath,
                        "RawMem<" + targetType.getSimpleName() + ">");
                default -> {}
            }
        }
    }
    
    private static void appendLayoutTreeChildren(
            MemoryLayout layout,
            StringBuilder sb,
            long baseOffset,
            String path,
            Map<String, String> groupTypes,
            String prefix,
            SummaryStyle style) {
        if (!(layout instanceof GroupLayout group)) {
            return;
        }

        long offset = 0;
        var members = group.memberLayouts();
        for (int i = 0; i < members.size(); i++) {
            MemoryLayout member = members.get(i);
            long memberOffset = baseOffset + offset;
            String memberPath = member.name()
                    .map(name -> path.isEmpty() ? name : path + "." + name)
                    .orElse(path);
            appendLayoutTreeNode(member, sb, memberOffset, memberPath, groupTypes, prefix, i == members.size() - 1, style);
            offset += member.byteSize();
        }
    }
    
    private static void appendLayoutTreeNode(
            MemoryLayout layout,
            StringBuilder sb,
            long offset,
            String path,
            Map<String, String> groupTypes,
            String prefix,
            boolean last,
            SummaryStyle style) {
        sb.append(prefix)
                .append(last ? style.lastBranch() : style.branch())
                .append(summaryLabel(layout, path, groupTypes))
                .append(" [").append(offset).append("..").append(offset + layout.byteSize()).append(") - ")
                .append(formatLayoutBytes(layout.byteSize())).append("\n");

        String childPrefix = prefix + (last ? style.indent() : style.vertical());
        if (layout instanceof GroupLayout group) {
            appendLayoutTreeChildren(group, sb, offset, path, groupTypes, childPrefix, style);
        } else if (layout instanceof SequenceLayout sequence && sequence.elementLayout() instanceof GroupLayout group) {
            appendLayoutTreeElement(sequence, group, sb, offset, path, groupTypes, childPrefix, style);
        }
    }
    
    private static void appendLayoutTreeElement(
            SequenceLayout sequence,
            GroupLayout group,
            StringBuilder sb,
            long offset,
            String path,
            Map<String, String> groupTypes,
            String prefix,
            SummaryStyle style) {
        long elementSize = group.byteSize();
        sb.append(prefix)
                .append(style.lastBranch())
                .append("element: ")
                .append(groupTypes.getOrDefault(path, group.name().orElse("struct")))
                .append(" [").append(offset).append("..").append(offset + elementSize).append(") - ")
                .append(formatLayoutBytes(elementSize))
                .append(" x ").append(sequence.elementCount())
                .append("\n");
        appendLayoutTreeChildren(group, sb, offset, path, groupTypes, prefix + style.indent(), style);
    }
    
    private static String summaryLabel(MemoryLayout layout, String path, Map<String, String> groupTypes) {
        return switch (layout) {
            case PaddingLayout _ -> "padding";
            default -> layout.name()
                    .map(name -> name + ": " + summaryType(layout, path, groupTypes))
                    .orElse(summaryType(layout, path, groupTypes));
        };
    }
    
    private static String summaryType(MemoryLayout layout, String path, Map<String, String> groupTypes) {
        return switch (layout) {
            case ValueLayout value -> groupTypes.getOrDefault(
                    path, primitiveName(value.carrier()));
            case SequenceLayout sequence -> groupTypes.getOrDefault(path, summaryType(sequence.elementLayout(), path, groupTypes))
                    + "[" + sequence.elementCount() + "]";
            case GroupLayout group -> groupTypes.getOrDefault(path, group.name().orElse("struct"));
            case PaddingLayout padding -> padding.byteSize() + " bytes";
        };
    }
    
    private static String formatLayoutBytes(long bytes) {
        return bytes < 1024 ? bytes + " B" : humanReadableSize(bytes);
    }
    
    private static String humanReadableSize(long bytes) {
        String[] units = {"B","KiB","MiB","GiB","TiB","PiB"};

        double value = bytes;
        int i = 0;

        while (value >= 1024 && i < units.length - 1) {
            value /= 1024;
            i++;
        }

        return String.format("%.2f %s", value, units[i]);
    }
    
    private static String primitiveName(Class<?> type) {
        return switch (type.getSimpleName()) {
            case "char" -> "char";
            case "boolean" -> "boolean";
            case "byte" -> "byte";
            case "short" -> "short";
            case "int" -> "int";
            case "float" -> "float";
            case "long" -> "long";
            case "double" -> "double";
            default -> type.getSimpleName();
        };
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
    
    
    /// Returns generated VarHandle field declarations for this layout.
    ///
    /// @return VarHandle field declarations
    public List<String> varHandleFields(){
        List<String> varFields = new ArrayList<>();
        varHandleFields(layout, new ArrayDeque<>(varHandleNames()), new ArrayDeque<>(), varFields);
        return varFields;
    }
    
    /// Formats generated VarHandle field declarations separated by newlines.
    ///
    /// @return formatted VarHandle field declarations
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
    
    /// Returns generated VarHandle names as a deque.
    ///
    /// @return generated VarHandle names
    public Deque<String> varHandleNamesDeque(){
        return new ArrayDeque<>(varHandleNames());
    }
    
    /// Returns generated VarHandle names for fields in this layout.
    ///
    /// @return generated VarHandle names
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
            case "MemorySegment" -> "ADDRESS";
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
