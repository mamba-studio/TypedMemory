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
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import java.util.function.Consumer;

/// {@summary A reference to a native memory address.}
///
/// A {@code Ptr} exposes only the referenced native memory segment. It makes
/// no statement about the type or number of values available at that address.
/// Implementations must never return a heap segment from {@link #segment()}.
///
/// @author joemw
public interface Ptr {

    /// A pointer value representing the native {@code NULL} address.
    Ptr NULL = MemoryRefs.nullPtr();

    /// Creates a pointer for a native memory segment.
    ///
    /// @param segment the native memory segment
    /// @return a pointer to {@code segment}
    /// @throws IllegalArgumentException if {@code segment} is a heap segment
    /// @throws NullPointerException if {@code segment} is null
    static Ptr of(MemorySegment segment) {
        return MemoryRefs.ptr(segment);
    }

    /// Creates a zero-length, globally scoped pointer for a native address.
    ///
    /// This method represents an address only; it does not make the address
    /// dereferenceable or assign spatial bounds to it. Address zero is
    /// canonicalized to {@link #NULL}.
    ///
    /// @param address the native address
    /// @return a pointer representing {@code address}
    static Ptr of(long address) {
        return of(MemorySegment.ofAddress(address));
    }

    /// Reassociates a native pointer with an arena.
    ///
    /// The returned pointer has the same address and spatial bounds as
    /// {@code pointer}, but its lifetime and thread-access properties are
    /// controlled by {@code arena}. No cleanup action is registered. A
    /// native-null pointer is returned as {@link #NULL} because it has no
    /// backing resource or lifetime to reassociate.
    ///
    /// @param pointer the native pointer to reinterpret
    /// @param arena the arena that controls the returned pointer's lifetime
    /// @return a pointer associated with {@code arena}
    /// @throws IllegalCallerException if native access is not enabled for this
    ///         module
    /// @throws IllegalStateException if {@code arena} is not alive
    /// @throws NullPointerException if {@code pointer} or {@code arena} is null
    static Ptr reinterpret(Ptr pointer, Arena arena) {
        Objects.requireNonNull(pointer);
        Objects.requireNonNull(arena);
        if (pointer.isNull()) {
            return NULL;
        }
        return MemoryRefs.ptr(MemoryRefs.reinterpret(pointer.segment(), arena, null));
    }

    /// Reassociates a native pointer with an arena and registers a cleanup
    /// action.
    ///
    /// The cleanup action is invoked when {@code arena} is closed and
    /// receives a globally scoped pointer to the same native address. The
    /// caller is responsible for registering cleanup exactly once for each
    /// owned native reference. A native-null pointer is returned as
    /// {@link #NULL} without registering the cleanup action.
    ///
    /// @param pointer the native pointer to reinterpret
    /// @param arena the arena that controls the returned pointer's lifetime
    /// @param cleanup the action that releases the owned native reference
    /// @return a pointer associated with {@code arena}
    /// @throws IllegalCallerException if native access is not enabled for this
    ///         module
    /// @throws IllegalStateException if {@code arena} is not alive
    /// @throws NullPointerException if any argument is null
    static Ptr reinterpret(
            Ptr pointer, Arena arena, Consumer<? super Ptr> cleanup) {
        Objects.requireNonNull(pointer);
        Objects.requireNonNull(arena);
        Objects.requireNonNull(cleanup);
        if (pointer.isNull()) {
            return NULL;
        }
        return MemoryRefs.ptr(
                MemoryRefs.reinterpret(pointer.segment(), arena, cleanup));
    }

    /// Returns the native memory segment representing this pointer.
    ///
    /// @return the referenced native memory segment
    MemorySegment segment();

    /// Returns the native address represented by this pointer.
    ///
    /// @return the native address
    default long nativeAddress() {
        var segment = segment();
        if (!segment.isNative()) {
            throw new IllegalStateException("Ptr segment must be native");
        }
        return segment.address();
    }

    /// Reports whether this pointer represents the native {@code NULL} address.
    ///
    /// @return {@code true} when the native address is zero
    default boolean isNull() {
        return MemorySegment.NULL.equals(segment());
    }

    /// Compares this pointer with another object for memory-location equality.
    /// Two {@code Ptr} values are equal when their segments refer to the same
    /// memory location, regardless of type, layout, bounds, or lifetime.
    ///
    /// @param other the object to compare with
    /// @return {@code true} if {@code other} is a pointer to the same location
    @Override
    boolean equals(Object other);

    /// Returns a hash code consistent with memory-location equality.
    ///
    /// @return the memory-location hash code
    @Override
    int hashCode();
}
