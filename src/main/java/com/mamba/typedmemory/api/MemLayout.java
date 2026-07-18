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

package com.mamba.typedmemory.api;

import module java.base;

import com.mamba.typedmemory.layout.FieldType;
import com.mamba.typedmemory.layout.FieldType.ArrayField;
import com.mamba.typedmemory.layout.FieldType.MemSize;
import com.mamba.typedmemory.layout.FieldType.PrimitiveField;
import com.mamba.typedmemory.layout.FieldType.PtrField;
import com.mamba.typedmemory.layout.FieldType.RawMemField;
import com.mamba.typedmemory.layout.FieldType.RecordField;
import com.mamba.typedmemory.layout.LayoutRules;
import static com.mamba.typedmemory.layout.LayoutRules.computeAlignmentOffset;
import com.mamba.typedmemory.util.MemLayoutString;
import com.mamba.typedmemory.util.MemLayoutString.SummaryStyle;

/**
 * Describes the memory layout derived for a TypedMemory record type.
 *
 * <p>
 * A {@code MemLayout} wraps the primary {@link MemoryLayout} used for an
 * element and, when nested records are present, the group layouts discovered
 * while walking the record structure.
 *
 * @param layout the primary layout for the record or sequence
 * @param groupLayouts nested group layouts discovered while deriving the layout
 */
public record MemLayout(MemoryLayout layout, Optional<List<MemoryLayout>> groupLayouts) implements LayoutRules{
    /**
     * Creates a layout descriptor.
     *
     * @param layout the primary layout for the record or sequence
     * @param groupLayouts nested group layouts discovered while deriving the
     *        layout
     */
    public MemLayout{
        Objects.requireNonNull(layout);
        Objects.requireNonNull(groupLayouts);
    }
    
    /**
     * Creates a layout descriptor without nested group layout metadata.
     *
     * @param layout the primary layout to describe
     */
    public MemLayout(MemoryLayout layout){
        this(layout, Optional.empty());
    }
    
    /**
     * Returns a Java-like source representation of the wrapped layout.
     *
     * @return a formatted memory layout source expression
     */
    public String source() {
        return MemLayoutString.of(this).stringLayout();
    }
        
    /**
     * Reports whether this descriptor contains nested group layouts.
     *
     * @return {@code true} when nested group layouts are present
     */
    public boolean hasInnerLayouts(){
       return (groupLayouts.isPresent() && !groupLayouts.get().isEmpty());        
    }
    
    /**
     * Reports whether the primary layout is a sequence layout.
     *
     * @return {@code true} when {@link #layout()} is a {@link SequenceLayout}
     */
    public boolean isSequence(){
        return layout instanceof SequenceLayout;
    }
    
    /**
     * Reports whether the primary layout is a group layout.
     *
     * @return {@code true} when {@link #layout()} is a {@link GroupLayout}
     */
    public boolean isGroup(){
        return layout instanceof GroupLayout;
    }
    
    /**
     * Returns the group layout represented by this descriptor.
     *
     * <p>
     * If the primary layout is a sequence whose element layout is a group, the
     * sequence element layout is returned.
     *
     * @return the group layout
     * @throws UnsupportedOperationException if the primary layout is not a
     *         group layout or a sequence of group layouts
     */
    public GroupLayout groupLayout() {
        return switch (layout) {
            case SequenceLayout seq when seq.elementLayout() instanceof GroupLayout g -> g;
            case GroupLayout g -> g;
            default -> throw new UnsupportedOperationException();
        };
    }
    
    /**
     * Creates a sequence layout descriptor using this descriptor's group layout.
     *
     * @param size the number of elements in the sequence
     * @return a layout descriptor for a sequence of this descriptor's group
     *         layout
     */
    public MemLayout ofSequenceSize(long size){
        return new MemLayout(MemoryLayout.sequenceLayout(size, groupLayout()), groupLayouts());
    }
    
    /**
     * Returns the nested group layouts as a deque.
     *
     * @return nested group layouts in derivation order
     * @throws java.util.NoSuchElementException if nested group layouts are not
     *         available
     */
    public Deque<MemoryLayout> groupLayoutsDeque(){
        return new ArrayDeque<>(groupLayouts.orElseThrow());
    }
    
    /**
     * Returns the name of the primary layout.
     *
     * @return the layout name
     * @throws java.util.NoSuchElementException if the primary layout is unnamed
     */
    public String name(){
        return layout.name().get();
    }
    
    @Override
    public String toString(){
        Objects.requireNonNull(layout);
        return source();
    }
    
    /**
     * Derives a sequence layout for a record type.
     *
     * @param clazz the record class to describe
     * @param name the name to apply to the sequence layout
     * @param size the number of elements in the sequence
     * @return a layout descriptor for the sequence
     * @throws UnsupportedOperationException if {@code size} is negative or the
     *         record contains an unsupported component type
     */
    public static MemLayout ofSequence(Class<? extends Record> clazz, String name, long size){
        if(size < 0)
            throw new UnsupportedOperationException("size should be greater than 0");
        Optional<List<MemoryLayout>> gOptional = Optional.of(new ArrayList<>());
        MemLayout gL = of((RecordField)FieldType.of(clazz, name), gOptional);        
        return new MemLayout(MemoryLayout.sequenceLayout(size, gL.layout()).withName(name), gL.groupLayouts());        
    }
    
    /**
     * Derives the memory layout for a record class.
     *
     * @param clazz the record class to describe
     * @return a layout descriptor for the record
     * @throws UnsupportedOperationException if the record contains an
     *         unsupported component type
     */
    public static MemLayout of(Class<? extends Record> clazz){
        FieldType type = FieldType.of(clazz, clazz.getSimpleName());
        return of((RecordField)type, Optional.of(new ArrayList<>()));
    }
    
    private static MemLayout of(RecordField field, Optional<List<MemoryLayout>> groupLayoutLists){        
        MemSize memSize = field.byteSize();
        
        RecordComponent[] components = field.type().getRecordComponents();
        long offset = 0;        
        ArrayList<MemoryLayout> layouts = new ArrayList<>();    
        
        for (RecordComponent component : components) {
            switch (FieldType.of(component)) {
                case PrimitiveField prim ->{
                    int size = prim.primitiveByteSize();
                    long alignedOffset = computeAlignmentOffset(offset, size);
                                        
                    // Add padding if needed
                    if (alignedOffset > offset) 
                        layouts.add(MemoryLayout.paddingLayout(alignedOffset - offset));
                    
                    // Add field layout
                    layouts.add(prim.valueLayout().withName(prim.name()));
                    
                    // Update the offset
                    offset = alignedOffset + size;
                }
                case RecordField rec -> {
                    // Calculate alignment for the record
                    MemSize rSize = rec.byteSize();
                    long alignedOffset = computeAlignmentOffset(offset, rec.alignByteSize());

                    // Add padding if needed
                    if (alignedOffset > offset) 
                        layouts.add(MemoryLayout.paddingLayout(alignedOffset - offset));
                    
                    // Recursively generate layout for the inner record
                    int indexToInsert = groupLayoutLists.map(List::size).orElse(-1);
                    MemoryLayout groupLayout = of(rec, groupLayoutLists).layout().withName(rec.typeName()); 
                    groupLayoutLists.ifPresent(list-> list.add(indexToInsert, groupLayout));
                    layouts.add(groupLayout.withName(rec.name()));
                    
                    // Update the offset
                    offset = alignedOffset + rSize.endOffset();
                }
                case PtrField _, RawMemField _ -> {
                    var address = ValueLayout.ADDRESS;
                    long alignedOffset = computeAlignmentOffset(offset, address.byteAlignment());

                    if (alignedOffset > offset) {
                        layouts.add(MemoryLayout.paddingLayout(alignedOffset - offset));
                    }

                    layouts.add(address.withName(component.getName()));
                    offset = alignedOffset + address.byteSize();
                }
                case ArrayField(var name, var _, var componentType, var arrSize) -> {
                    long alignedOffsetArr = 0;
                    long elementSize = 0;
                    MemoryLayout elementLayout;
                    
                    switch (FieldType.of(componentType, name)) {
                        case PrimitiveField p ->{
                            elementSize = p.primitiveByteSize();
                            alignedOffsetArr = computeAlignmentOffset(offset, elementSize);

                            // Add padding if needed
                            if (alignedOffsetArr > offset) 
                                layouts.add(MemoryLayout.paddingLayout(alignedOffsetArr - offset));

                            // Generate sequence layout for primitive arrays
                            elementLayout = p.valueLayout();
                        }
                        case RecordField r ->{
                            // Calculate layout for record elements
                            MemSize rSize = r.byteSize();
                            long recordAlignment = r.alignByteSize();
                            alignedOffsetArr = computeAlignmentOffset(offset, recordAlignment);

                            // Add padding if needed
                            if (alignedOffsetArr > offset) 
                                layouts.add(MemoryLayout.paddingLayout(alignedOffsetArr - offset));
                            
                            // Recursively generate layout for the record elements
                            int indexToInsert = groupLayoutLists.map(List::size).orElse(-1);
                            elementLayout = of(r, groupLayoutLists).layout().withName(r.typeName());
                            groupLayoutLists.ifPresent(list-> list.add(indexToInsert, elementLayout));
                            elementSize = rSize.size(); // Use the record's calculated size
                        }
                        case PtrField _, RawMemField _ -> throw new UnsupportedOperationException(
                                "Arrays of pointer fields are not supported: " + name);
                        case ArrayField _ -> throw new UnsupportedOperationException("Array in an array? How did you get here? Please message, because I'm curious how!");
                    }
                    
                    // Add the array layout to the joiner with its name
                    layouts.add(MemoryLayout.sequenceLayout(arrSize, elementLayout).withName(name));
                    
                    // Update the offset
                    if(arrSize == 0) arrSize = 1;
                    offset = alignedOffsetArr + arrSize * elementSize;
                }
            }
        }
        
        // Add final padding if required
        if (memSize.hasPadding()) {
            layouts.add(MemoryLayout.paddingLayout(memSize.padding()));
            
        }
        
        MemoryLayout[] layArray = new MemoryLayout[layouts.size()];
        for(int i = 0; i<layouts.size(); i++)
            layArray[i] = layouts.get(i);
        
        return new MemLayout(MemoryLayout.structLayout(layArray).withName(field.type().getSimpleName()), groupLayoutLists);
    }
    
    private static String formatBytes(long bytes) {
        String[] units = {"B","KiB","MiB","GiB","TiB","PiB"};

        double value = bytes;
        int i = 0;

        while (value >= 1024 && i < units.length - 1) {
            value /= 1024;
            i++;
        }

        return String.format("%.2f %s", value, units[i]);
    }
    
    private static String humanReadableSize(long bytes) {
        return formatBytes(bytes);
    }
    
    /**
     * Formats a short summary of a memory view.
     *
     * @param <T> the element type stored in the memory view
     * @param mem the memory view to summarize
     * @return a human-readable element count, element size, and segment size
     */
    public static<T>  String memorySummary(Mem<T> mem) {
        return "%d elements, element size (%s), segment size (%s)".formatted(
            mem.size(),
            humanReadableSize(mem.layout().byteSize()),
            humanReadableSize(mem.segment().byteSize())
        );
    }
    
    /**
     * Summarizes the layout of a record type.
     *
     * @param type the record type to summarize
     * @return a human-readable type layout summary
     */
    public static String typeSummary(Class<? extends Record> type) {
        return typeSummary(type, SummaryStyle.ASCII);
    }
    
    /**
     * Summarizes the layout of a record type.
     *
     * @param type the record type to summarize
     * @param style the branch style to use
     * @return a human-readable type layout summary
     */
    public static String typeSummary(Class<? extends Record> type, SummaryStyle style) {
        return MemLayoutString.typeSummary(type, style);
    }
    
    /**
     * Prints a Unicode tree summary of the layout of a record type using
     * UTF-8 output.
     *
     * @param type the record type to summarize
     */
    public static void printTypeSummary(Class<? extends Record> type) {
        printTypeSummary(type, SummaryStyle.UNICODE);
    }
    
    /**
     * Prints a summary of the layout of a record type using UTF-8 output.
     *
     * @param type the record type to summarize
     * @param style the branch style to use
     */
    public static void printTypeSummary(Class<? extends Record> type, SummaryStyle style) {
        MemLayoutString.printTypeSummary(type, style);
    }

}
