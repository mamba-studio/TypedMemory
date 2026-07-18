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

import com.mamba.typedmemory.api.Ptr;
import com.mamba.typedmemory.api.RawMem;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

/** Internal implementations for the public memory-reference API. */
public final class MemoryRefs {

    private MemoryRefs() {
    }

    public static Ptr nullPtr() {
        return new NativePtr(MemorySegment.NULL);
    }

    public static Ptr ptr(MemorySegment segment) {
        requireNative(segment);
        return segment.address() == 0 ? Ptr.NULL : new NativePtr(segment);
    }

    public static <T> RawMem<T> rawMem(
            MemorySegment segment, Class<T> type, MemoryLayout layout) {
        return new NativeRawMem<>(segment, type, layout);
    }

    private static MemorySegment requireNative(MemorySegment segment) {
        Objects.requireNonNull(segment);
        if (!segment.isNative()) {
            throw new IllegalArgumentException("Memory reference requires a native memory segment");
        }
        return segment;
    }

    private static int addressHashCode(Ptr pointer) {
        return pointer.segment().hashCode();
    }

    private record NativePtr(MemorySegment segment) implements Ptr {
        private NativePtr {
            requireNative(segment);
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Ptr that
                    && segment.equals(that.segment());
        }

        @Override
        public int hashCode() {
            return addressHashCode(this);
        }
    }

    private record NativeRawMem<T>(
            MemorySegment segment,
            Class<T> type,
            MemoryLayout layout) implements RawMem<T> {

        private NativeRawMem {
            requireNative(segment);
            Objects.requireNonNull(type);
            Objects.requireNonNull(layout);
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Ptr that
                    && segment.equals(that.segment());
        }

        @Override
        public int hashCode() {
            return addressHashCode(this);
        }
    }
}
