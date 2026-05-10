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

import com.mamba.typedmemory.api.Mem.MemCache;
import com.mamba.typedmemory.opcode.Generator;
import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.LongFunction;
import java.util.function.ObjLongConsumer;
import java.util.function.Supplier;

/**
 * {@summary A strongly-typed view over contiguous off-heap memory.}
 *
 * <p>
 * {@code Mem<T>} represents a sequence of elements of type {@code T}
 * stored in a contiguous region of memory. It provides a safe and ergonomic
 * abstraction over the Java Foreign Function &amp; Memory (FFM) API while
 * preserving the layout and performance characteristics of low-level memory.
 *
 * <p>
 * Each {@code Mem} instance is backed by a {@link java.lang.foreign.MemorySegment}
 * whose layout is derived from the structure of {@code T}. In the typical case,
 * {@code T} is a {@code record}. The record components are analyzed to produce
 * a deterministic {@link java.lang.foreign.MemoryLayout} describing the binary
 * representation of each element.
 *
 * <p>
 * Unlike traditional Java collections, {@code Mem} does not store Java objects.
 * Instead, elements are stored directly as structured binary data inside a
 * contiguous memory segment. Record instances are reconstructed only when
 * values are read using {@link #get(long)}.
 *
 * <p>
 * This design enables:
 *
 * <ul>
 * <li>zero-copy access to structured memory</li>
 * <li>predictable and deterministic binary layouts</li>
 * <li>compatibility with native memory representations</li>
 * <li>high-performance iteration and traversal</li>
 * </ul>
 *
 * <h2>Allocation</h2>
 *
 * <p>
 * Memory regions are allocated using an {@link Arena}, which defines the
 * lifetime of the underlying memory. When the arena is closed, all memory
 * associated with the {@code Mem} instance is automatically released.
 *
 * {@snippet :
 * record Color(float r, float g, float b) {}
 *
 * try (Arena arena = Arena.ofConfined()) {
 *     Mem<Color> colors = Mem.of(Color.class, arena, 10);
 * }
 * }
 *
 * <h2>Element Access</h2>
 *
 * <p>
 * Elements can be accessed using indexed read and write operations.
 * Writes store structured data directly into the memory segment,
 * while reads reconstruct record instances from the stored bytes.
 *
 * {@snippet :
 * Mem<Color> colors = Mem.of(Color.class, arena, 4);
 *
 * colors.set(0, new Color(1f, 0f, 0f));
 * colors.set(1, new Color(0f, 1f, 0f));
 *
 * Color first = colors.get(0);
 * }
 *
 * <h2>Traversal</h2>
 *
 * <p>
 * Iteration over the memory region can be performed using
 * {@link #forEachIndexed(java.util.function.ObjLongConsumer)}.
 *
 * {@snippet :
 * colors.forEachIndexed((color, index) -> {
 *     System.out.println(index + ": " + color);
 * });
 * }
 *
 * <h2>Relationship to the Foreign Memory API</h2>
 *
 * <p>
 * {@code Mem} builds on top of the {@link java.lang.foreign.MemorySegment}
 * and {@link java.lang.foreign.MemoryLayout} abstractions introduced in
 * the Foreign Function &amp; Memory API. It provides a higher-level,
 * type-safe programming model for structured memory while retaining
 * compatibility with low-level memory operations and native interfaces.
 *
 * <h2>Implementation Strategy</h2>
 *
 * <p>
 * Implementations of {@code Mem} are generated dynamically at runtime.
 * When {@link #of(Class, Arena, long)} is invoked, a specialized hidden
 * class is generated to provide efficient accessors for the memory layout
 * associated with the given type {@code T}. The generated class directly
 * reads and writes fields using the derived memory layout.
 *
 * <p>
 * Generated implementations are cached to avoid repeated class generation
 * for the same element type.
 *
 * <h2>Design Goals</h2>
 *
 * <ul>
 * <li>Provide strongly-typed views over off-heap memory</li>
 * <li>Enable deterministic memory layouts derived from Java types</li>
 * <li>Support high-performance data traversal</li>
 * <li>Allow seamless integration with the Foreign Memory API</li>
 * </ul>
 *
 * <p>
 * {@code Mem} is particularly useful when working with structured binary
 * data, native interoperation, high-performance data processing, or
 * memory layouts that must match external formats.
 *
 * @param <T> the element type stored in this memory region
 * @author joemw
 */

public interface Mem<T> {
    /**
     * Stores an element at the given index.
     *
     * @param index the zero-based element index
     * @param t the element value to store
     * @throws IndexOutOfBoundsException if {@code index} is outside this memory
     *         view
     */
    public void set(long index, T t);

    /**
     * Reads an element from the given index.
     *
     * @param index the zero-based element index
     * @return the reconstructed element value
     * @throws IndexOutOfBoundsException if {@code index} is outside this memory
     *         view
     */
    public T get(long index);

    /**
     * Returns the backing memory segment for this view.
     *
     * @return the backing memory segment
     */
    public MemorySegment segment();

    /**
     * Returns the number of elements in this memory view.
     *
     * @return the element count
     */
    public long size();

    /**
     * Returns the element type represented by this memory view.
     *
     * @return the element type
     */
    public Class<T> type();

    /**
     * Returns the memory layout for one element.
     *
     * @return the element memory layout
     */
    public MemoryLayout layout();
    
    /**
     * Fills every element with the same value.
     *
     * @param value the value to store in each element
     * @return this memory view
     */
    default Mem<T> fill(T value) {
        for (long i = 0; i < size(); i++) set(i, value);
        return this;
    }
           
    /**
     * Returns the native address of the backing memory segment.
     *
     * @return the segment address
     */
    default long nativeAddress(){
        return segment().address();
    }
    
    /**
     * Initializes every element with values supplied on demand.
     *
     * @param supplier supplies each element value
     * @return this memory view
     */
    default Mem<T> init(Supplier<T> supplier){
        for (long i = 0; i < size(); i++)
            set(i, supplier.get());
        return this;
    }
    
    /**
     * Initializes every element with values derived from its index.
     *
     * @param factory creates an element value for each zero-based index
     * @return this memory view
     */
    default Mem<T> initIndexed(LongFunction<? extends T> factory) {
        for (long i = 0; i < size(); i++) set(i, factory.apply(i));
        return this;
    }

    /**
     * Visits each element together with its index.
     *
     * @param consumer receives each element and its zero-based index
     */
    default void forEachIndexed(ObjLongConsumer<T> consumer) {
        long n = size();
        for (long i = 0; i < n; i++)
            consumer.accept(get(i), i);
    }
    
    /**
     * Visits each element in index order.
     *
     * @param consumer receives each element
     */
    default void forEach(Consumer<? super T> consumer) {
        long n = size();
        for (long i = 0; i < n; i++) consumer.accept(get(i));
    }
    
    /**
     * Checks that an index is within this memory view.
     *
     * @param index the zero-based element index to check
     * @throws IndexOutOfBoundsException if {@code index} is outside this memory
     *         view
     */
    default void checkIndex(long index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
        }
    }
    
    /**
     * Copies all elements from another compatible memory view into this view.
     *
     * @param src the source memory view
     * @throws IllegalArgumentException if the views have different element
     *         types, element sizes, or element counts
     * @throws NullPointerException if {@code src} is null
     */
    default void copyFrom(Mem<T> src) {
        Objects.requireNonNull(src);
        checkCopyCompatible(src);
        if (src.size() != size()) {
            throw new IllegalArgumentException(
                    "Source size " + src.size() + " != destination size " + size());
        }
        MemorySegment.copy(src.segment(), 0, segment(), 0, segment().byteSize());
    }
    
    /**
     * Copies a range of elements from another compatible memory view.
     *
     * @param src the source memory view
     * @param srcIndex the first source index
     * @param dstIndex the first destination index in this view
     * @param count the number of elements to copy
     * @throws IllegalArgumentException if the views have different element
     *         types or element sizes
     * @throws IndexOutOfBoundsException if either range is outside its memory
     *         view
     * @throws NullPointerException if {@code src} is null
     */
    default void copyFrom(Mem<T> src, long srcIndex, long dstIndex, long count) {
        Objects.requireNonNull(src);
        checkCopyCompatible(src);
        checkCopyRange(srcIndex, count, src.size(), "Source");
        checkCopyRange(dstIndex, count, size(), "Destination");
        
        var stride = layout().byteSize();
        var bytes = Math.multiplyExact(stride, count);
        var srcOffset = Math.multiplyExact(stride, srcIndex);
        var dstOffset = Math.multiplyExact(stride, dstIndex);
        MemorySegment.copy(src.segment(), srcOffset, segment(), dstOffset, bytes);
    }
    
    /**
     * Copies all elements from this view into another compatible memory view.
     *
     * @param dst the destination memory view
     * @throws IllegalArgumentException if the views have different element
     *         types, element sizes, or element counts
     * @throws NullPointerException if {@code dst} is null
     */
    default void copyTo(Mem<T> dst) {
        Objects.requireNonNull(dst);
        dst.copyFrom(this);
    }
    
    /**
     * Copies a range of elements from this view into another compatible view.
     *
     * @param dst the destination memory view
     * @param srcIndex the first source index in this view
     * @param dstIndex the first destination index
     * @param count the number of elements to copy
     * @throws IllegalArgumentException if the views have different element
     *         types or element sizes
     * @throws IndexOutOfBoundsException if either range is outside its memory
     *         view
     * @throws NullPointerException if {@code dst} is null
     */
    default void copyTo(Mem<T> dst, long srcIndex, long dstIndex, long count) {
        Objects.requireNonNull(dst);
        dst.copyFrom(this, srcIndex, dstIndex, count);
    }
    
    /**
     * Swaps two elements in this memory view.
     *
     * @param i the first zero-based element index
     * @param j the second zero-based element index
     * @throws IndexOutOfBoundsException if either index is outside this memory
     *         view
     */
    default void swap(long i, long j) {
        checkIndex(i);
        checkIndex(j);
        if (i == j) {
            return;
        }
        
        var stride = layout().byteSize();
        if (stride == 0) {
            return;
        }

        var iOffset = Math.multiplyExact(stride, i);
        var jOffset = Math.multiplyExact(stride, j);
        try (var arena = Arena.ofConfined()) {
            var tmp = arena.allocate(stride);
            MemorySegment.copy(segment(), iOffset, tmp, 0, stride);
            MemorySegment.copy(segment(), jOffset, segment(), iOffset, stride);
            MemorySegment.copy(tmp, 0, segment(), jOffset, stride);
        }
    }
    
    private void checkCopyCompatible(Mem<T> src) {
        if (src.type() != type()) {
            throw new IllegalArgumentException(
                    "Source type " + src.type().getName()
                    + " != destination type " + type().getName());
        }
        if (src.layout().byteSize() != layout().byteSize()) {
            throw new IllegalArgumentException(
                    "Source element byte size " + src.layout().byteSize()
                    + " != destination element byte size " + layout().byteSize());
        }
    }
    
    private static void checkCopyRange(long index, long count, long size, String label) {
        if (count < 0) {
            throw new IndexOutOfBoundsException(label + " count: " + count);
        }
        if (index < 0 || index > size || count > size - index) {
            throw new IndexOutOfBoundsException(
                    label + " index: " + index + ", Count: " + count + ", Size: " + size);
        }
    }
    
    /**
     * Allocates a typed memory view for a record type.
     *
     * <p>
     * The supplied lookup is used to define the generated implementation class
     * with access to the record constructor.
     *
     * @param <T> the record element type
     * @param clazz the record class to store
     * @param lookup the lookup used to access the record type
     * @param arena the arena that owns the allocated memory
     * @param size the number of elements to allocate
     * @return a typed memory view backed by newly allocated memory
     * @throws IllegalArgumentException if {@code clazz} is not a record or
     *         {@code size} is negative
     * @throws NullPointerException if {@code clazz}, {@code lookup}, or
     *         {@code arena} is null
     */
    public static <T extends Record> Mem<T> of(Class<T> clazz, Lookup lookup, Arena arena, long size) {
        Objects.requireNonNull(arena);
        var memLayout = MemLayout.of(clazz);
        var segment = arena.allocate(memLayout.layout(), size);
        return instantiate(clazz, lookup, memLayout, segment);
    }
    
    /**
     * Allocates a typed memory view for a record type.
     *
     * @param <T> the record element type
     * @param clazz the record class to store
     * @param arena the arena that owns the allocated memory
     * @param size the number of elements to allocate
     * @return a typed memory view backed by newly allocated memory
     * @throws IllegalArgumentException if {@code clazz} is not a record or
     *         {@code size} is negative
     * @throws NullPointerException if {@code clazz} or {@code arena} is null
     */
    public static <T extends Record> Mem<T> of(Class<T> clazz, Arena arena, long size) {
        return of(clazz, MethodHandles.lookup(), arena, size);
    }
    
    /**
     * Creates a typed {@code Mem} view over an existing memory segment.
     *
     * <p>
     * This is the preferred API when the caller already owns a
     * {@link MemorySegment}. The returned {@code Mem} is backed by a slice of
     * {@code segment} containing exactly {@code size} elements.
     *
     * @param <T> the record element type
     * @param clazz the record class to store
     * @param lookup the lookup used to access the record type
     * @param segment the memory segment to wrap
     * @param size the number of elements to expose
     * @return a typed memory view backed by {@code segment}
     * @throws IllegalArgumentException if {@code clazz} is not a record,
     *         {@code size} is negative, or {@code segment} is too small
     * @throws NullPointerException if {@code clazz}, {@code lookup}, or
     *         {@code segment} is null
     */
    public static <T extends Record> Mem<T> wrap(Class<T> clazz, Lookup lookup, MemorySegment segment, long size) {
        Objects.requireNonNull(segment);

        var memLayout = MemLayout.of(clazz);
        var byteSize = byteSizeFor(memLayout, size);
        if (segment.byteSize() < byteSize) {
            throw new IllegalArgumentException(
                    "Segment byte size " + segment.byteSize()
                    + " is smaller than required byte size " + byteSize);
        }

        return instantiate(clazz, lookup, memLayout, segment.asSlice(0, byteSize));
    }
    
    /**
     * Creates a typed {@code Mem} view over an existing memory segment.
     *
     * @param <T> the record element type
     * @param clazz the record class to store
     * @param segment the memory segment to wrap
     * @param size the number of elements to expose
     * @return a typed memory view backed by {@code segment}
     * @throws IllegalArgumentException if {@code clazz} is not a record,
     *         {@code size} is negative, or {@code segment} is too small
     * @throws NullPointerException if {@code clazz} or {@code segment} is null
     */
    public static <T extends Record> Mem<T> wrap(Class<T> clazz, MemorySegment segment, long size) {
        return wrap(clazz, MethodHandles.lookup(), segment, size);
    }
    
    /**
     * Reinterprets a raw native address as a typed {@code Mem} view.
     *
     * <p>
     * This is advanced usage. The caller is responsible for ensuring the
     * address is valid, sufficiently large for {@code size} elements, correctly
     * aligned for the layout of {@code clazz}, and that the memory lifetime is
     * compatible with {@code arena}.
     *
     * @param <T> the record element type
     * @param clazz the record class to store
     * @param lookup the lookup used to access the record type
     * @param address the native address to reinterpret
     * @param arena the arena that controls the reinterpreted segment lifetime
     * @param size the number of elements to expose
     * @return a typed memory view backed by the reinterpreted address
     * @throws IllegalArgumentException if {@code clazz} is not a record or
     *         {@code size} is negative
     * @throws NullPointerException if {@code clazz}, {@code lookup}, or
     *         {@code arena} is null
     */
    public static <T extends Record> Mem<T> reinterpret(
            Class<T> clazz, Lookup lookup, long address, Arena arena, long size) {
        Objects.requireNonNull(arena);

        var memLayout = MemLayout.of(clazz);
        var byteSize = byteSizeFor(memLayout, size);
        var segment = MemorySegment.ofAddress(address).reinterpret(byteSize, arena, null);
        return instantiate(clazz, lookup, memLayout, segment);
    }
    
    /**
     * Reinterprets a raw native address as a typed {@code Mem} view.
     *
     * @param <T> the record element type
     * @param clazz the record class to store
     * @param address the native address to reinterpret
     * @param arena the arena that controls the reinterpreted segment lifetime
     * @param size the number of elements to expose
     * @return a typed memory view backed by the reinterpreted address
     * @throws IllegalArgumentException if {@code clazz} is not a record or
     *         {@code size} is negative
     * @throws NullPointerException if {@code clazz} or {@code arena} is null
     */
    public static <T extends Record> Mem<T> reinterpret(Class<T> clazz, long address, Arena arena, long size) {
        return reinterpret(clazz, MethodHandles.lookup(), address, arena, size);
    }
    
    private static <T extends Record> Mem<T> instantiate(Class<T> clazz, Lookup lookup, MemLayout memLayout, MemorySegment segment) {
        try {
            Objects.requireNonNull(clazz);
            Objects.requireNonNull(lookup);
            Objects.requireNonNull(memLayout);
            Objects.requireNonNull(segment);

            if (!clazz.isRecord()) {
                throw new IllegalArgumentException("Must be record");
            }

            MethodHandle ctor;

            if (isEphemeral(clazz)) {
                ctor = constructorFor(clazz, lookup, memLayout);
            } else {
                var cache = MemCache.of();
                var key = new CacheKey(clazz, lookup.lookupClass());

                ctor = cache.get(key);
                if (ctor == null) {
                    ctor = constructorFor(clazz, lookup, memLayout);
                    cache.put(key, ctor);
                }
            }

            return (Mem<T>) ctor.invoke(segment);

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    
    private static long byteSizeFor(MemLayout memLayout, long size) {
        if (size < 0) {
            throw new IllegalArgumentException("Size must be non-negative: " + size);
        }
        return Math.multiplyExact(memLayout.layout().byteSize(), size);
    }
        
    record CacheKey(Class<?> clazz, Class<?> lookupClass) {}
              
    final class MemCache {
        private MemCache() {}

        private static final Map<CacheKey, MethodHandle> CACHE =
                new ConcurrentHashMap<>();
        
        private static Map<CacheKey, MethodHandle> of(){
            return CACHE;
        }
    }    
    
    private static boolean isEphemeral(Class<?> clazz) {
        return clazz.isLocalClass() || clazz.isAnonymousClass();
    }

    private static <T extends Record> MethodHandle constructorFor(
            Class<T> clazz,
            MethodHandles.Lookup lookup,
            MemLayout memLayout) throws Throwable {
        var privateLookup = MethodHandles.privateLookupIn(clazz, lookup);

        var owner = Generator.generateUserImplName(clazz);

        byte[] bytes = Generator.generate(owner, clazz, memLayout);

        MethodHandles.Lookup hiddenLookup =
                privateLookup.defineHiddenClass(
                        bytes,
                        true,
                        MethodHandles.Lookup.ClassOption.NESTMATE
                );

        var hiddenClass = hiddenLookup.lookupClass();

        return hiddenLookup.findConstructor(
                hiddenClass,
                MethodType.methodType(void.class, MemorySegment.class)
        ).asType(MethodType.methodType(Mem.class, MemorySegment.class));
    }
}
