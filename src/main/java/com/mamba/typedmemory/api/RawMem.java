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
import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import java.util.function.Consumer;

/// {@summary A typed reference to native memory with no element count.}
///
/// {@code RawMem<T>} adds an element type and layout to a {@link Ptr}, but
/// does not claim that a particular number of elements is accessible. A
/// counted memory view is represented by {@link Mem}.
///
/// @param <T> the element type represented by this memory reference
/// @author joemw
public interface RawMem<T> extends Ptr {

    /// Creates a typed memory reference representing the native {@code NULL}
    /// address.
    ///
    /// @param <T> the record element type
    /// @param type the record element class
    /// @return a typed native-null reference
    /// @throws NullPointerException if {@code type} is null
    static <T extends Record> RawMem<T> of(Class<T> type) {
        return of(type, Ptr.NULL);
    }

    /// Creates a typed memory reference whose element layout is derived from a
    /// record type.
    ///
    /// @param <T> the record element type
    /// @param type the record element class
    /// @param segment the native memory segment
    /// @return a typed reference to {@code segment}
    /// @throws IllegalArgumentException if {@code segment} is a heap segment
    /// @throws NullPointerException if {@code type} or {@code segment} is null
    static <T extends Record> RawMem<T> of(Class<T> type, MemorySegment segment) {
        var layout = MemTypeCache.get(type).layout().layout();
        return MemoryRefs.rawMem(segment, type, layout);
    }

    /// Creates a typed memory reference whose element layout is derived from a
    /// record type.
    ///
    /// The returned reference preserves the pointer's address, spatial
    /// bounds, and arena scope.
    ///
    /// @param <T> the record element type
    /// @param type the record element class
    /// @param pointer the native pointer
    /// @return a typed reference to {@code pointer}
    /// @throws NullPointerException if {@code type} or {@code pointer} is null
    static <T extends Record> RawMem<T> of(Class<T> type, Ptr pointer) {
        Objects.requireNonNull(pointer);
        return of(type, pointer.segment());
    }

    /// Reassociates a typed native reference with an arena.
    ///
    /// The returned reference preserves the source element type, layout,
    /// address, and spatial bounds. No cleanup action is registered. A typed
    /// native-null reference is returned unchanged because it has no backing
    /// resource or lifetime to reassociate.
    ///
    /// @param <T> the element type represented by the reference
    /// @param memory the typed native reference to reinterpret
    /// @param arena the arena that controls the returned reference's lifetime
    /// @return a typed reference associated with {@code arena}
    /// @throws IllegalCallerException if native access is not enabled for this
    ///         module
    /// @throws IllegalStateException if {@code arena} is not alive
    /// @throws NullPointerException if {@code memory} or {@code arena} is null
    static <T> RawMem<T> reinterpret(RawMem<T> memory, Arena arena) {
        Objects.requireNonNull(memory);
        Objects.requireNonNull(arena);
        if (memory.isNull()) {
            return memory;
        }
        var segment = MemoryRefs.reinterpret(memory.segment(), arena, null);
        return MemoryRefs.rawMem(segment, memory.type(), memory.layout());
    }

    /// Reassociates a typed native reference with an arena and registers a
    /// cleanup action.
    ///
    /// The cleanup action is invoked when {@code arena} is closed and
    /// receives a globally scoped pointer to the same native address. The
    /// caller is responsible for registering cleanup exactly once for each
    /// owned native reference. A typed native-null reference is returned
    /// unchanged without registering the cleanup action.
    ///
    /// @param <T> the element type represented by the reference
    /// @param memory the typed native reference to reinterpret
    /// @param arena the arena that controls the returned reference's lifetime
    /// @param cleanup the action that releases the owned native reference
    /// @return a typed reference associated with {@code arena}
    /// @throws IllegalCallerException if native access is not enabled for this
    ///         module
    /// @throws IllegalStateException if {@code arena} is not alive
    /// @throws NullPointerException if any argument is null
    static <T> RawMem<T> reinterpret(
            RawMem<T> memory,
            Arena arena,
            Consumer<? super Ptr> cleanup) {
        Objects.requireNonNull(memory);
        Objects.requireNonNull(arena);
        Objects.requireNonNull(cleanup);
        if (memory.isNull()) {
            return memory;
        }
        var segment = MemoryRefs.reinterpret(memory.segment(), arena, cleanup);
        return MemoryRefs.rawMem(segment, memory.type(), memory.layout());
    }

    /// Returns the element type represented by this memory reference.
    ///
    /// @return the element type
    Class<T> type();

    /// Reports whether another typed memory reference carries the same element
    /// type.
    ///
    /// @param other the typed memory reference to compare with
    /// @return {@code true} if {@code other} is non-null and carries the same
    ///         element type
    default boolean hasSameType(RawMem<?> other) {
        return other != null && type() == other.type();
    }

    /// Returns the memory layout for one element.
    ///
    /// @return the element memory layout
    MemoryLayout layout();
}
