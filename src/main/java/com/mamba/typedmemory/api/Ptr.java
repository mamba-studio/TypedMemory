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
import java.lang.foreign.MemorySegment;

/**
 * {@summary A reference to a native memory address.}
 *
 * <p>A {@code Ptr} exposes only the referenced native memory segment. It makes
 * no statement about the type or number of values available at that address.
 * Implementations must never return a heap segment from {@link #segment()}.
 *
 * @author joemw
 */
public interface Ptr {

    /**
     * Creates a pointer for a native memory segment.
     *
     * @param segment the native memory segment
     * @return a pointer to {@code segment}
     * @throws IllegalArgumentException if {@code segment} is a heap segment
     * @throws NullPointerException if {@code segment} is null
     */
    static Ptr of(MemorySegment segment) {
        return MemoryRefs.ptr(segment);
    }

    /**
     * Returns the native memory segment representing this pointer.
     *
     * @return the referenced native memory segment
     */
    MemorySegment segment();

    /**
     * Returns the native address represented by this pointer.
     *
     * @return the native address
     */
    default long nativeAddress() {
        var segment = segment();
        if (!segment.isNative()) {
            throw new IllegalStateException("Ptr segment must be native");
        }
        return segment.address();
    }
}
