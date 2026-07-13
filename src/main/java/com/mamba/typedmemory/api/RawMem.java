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

import com.mamba.typedmemory.util.MemoryRefs;
import com.mamba.typedmemory.util.MemTypeCache;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

/**
 * {@summary A typed reference to native memory with no element count.}
 *
 * <p>{@code RawMem<T>} adds an element type and layout to a {@link Ptr}, but
 * does not claim that a particular number of elements is accessible. A
 * counted memory view is represented by {@link Mem}.
 *
 * @param <T> the element type represented by this memory reference
 * @author joemw
 */
public interface RawMem<T> extends Ptr {

    /**
     * Creates a typed memory reference whose element layout is derived from a
     * record type.
     *
     * @param <T> the record element type
     * @param type the record element class
     * @param segment the native memory segment
     * @return a typed reference to {@code segment}
     * @throws IllegalArgumentException if {@code segment} is a heap segment
     * @throws NullPointerException if {@code type} or {@code segment} is null
     */
    static <T extends Record> RawMem<T> of(Class<T> type, MemorySegment segment) {
        var layout = MemTypeCache.get(type).layout().layout();
        return MemoryRefs.rawMem(segment, type, layout);
    }

    /**
     * Returns the element type represented by this memory reference.
     *
     * @return the element type
     */
    Class<T> type();

    /**
     * Returns the memory layout for one element.
     *
     * @return the element memory layout
     */
    MemoryLayout layout();
}
