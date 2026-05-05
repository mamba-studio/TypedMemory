/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mamba.typedmemory.api;

import com.mamba.typedmemory.api.Mem.MemCache;
import com.mamba.typedmemory.opcode.Generator;
import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
 * Efficient iteration over the memory region can be performed using
 * {@link #traverse(java.util.function.ObjLongConsumer)}.
 *
 * {@snippet :
 * colors.traverse((color, index) -> {
 *     System.out.println(index + ": " + color);
 * });
 * }
 *
 * <h2>Query Pipelines</h2>
 *
 * <p>
 * {@code Mem} integrates with {@link MemQuery} to provide a lazy
 * data-processing pipeline similar to the Java Stream API but optimized
 * for structured memory traversal.
 *
 * <p>
 * Query operations are evaluated lazily and executed only when a terminal
 * operation such as {@link MemQuery#count()} or
 * {@link MemQuery#forEach(java.util.function.Consumer)} is invoked.
 *
 * {@snippet :
 * long count = colors.query()
 *     .filter(c -> c.r() > 0.5f)
 *     .count();
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
    public void set(long index, T t);
    public T get(long index);
    public MemorySegment segment();
    public long size();
    public Class<T> type();
    public MemoryLayout layout();
           
    default long address(){
        return segment().address();
    }
    
    default Mem<T> init(Supplier<T> supplier){
        for (long i = 0; i < size(); i++)
            set(i, supplier.get());
        return this;
    }
    
    default MemQuery<T, T> query() {
        return new MemQuery<>(new MemQuery.Stage.SourceStage<>(this));
    }
    
    default void traverse(ObjLongConsumer<T> consumer) {
        long n = size();
        for (long i = 0; i < n; i++)
            consumer.accept(get(i), i);
    }
    
    @SuppressWarnings("unchecked")
    default Mem<T> allocateLike(Arena arena, long size) {
        if (type().isRecord())
            return Mem.record((Class) type(), arena, size);
        else
            return Mem.union(type(), arena, size);
    }
            
    public static <T extends Record> Mem<T> of(Class<T> clazz,  MethodHandles.Lookup lookup, Arena arena, long size) {
        try {
            if (!clazz.isRecord()) {
                throw new IllegalArgumentException("Must be record");
            }

            var cache = MemCache.of();
            var key = new CacheKey(clazz, lookup.lookupClass());

            var ctor = cache.get(key);

            if (ctor == null) {

                var memLayout = MemLayout.of(clazz);
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

                ctor = hiddenLookup.findConstructor(
                        hiddenClass,
                        MethodType.methodType(void.class, MemorySegment.class)
                ).asType(MethodType.methodType(Mem.class, MemorySegment.class));

                cache.put(key, ctor);
            }

            var segment = arena.allocate(
                    MemLayout.of(clazz).layout(), size
            );

            return (Mem<T>) ctor.invoke(segment);

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    
    public static <T extends Record> Mem<T> of(Class<T> clazz, Arena arena, long size) {
        return of(clazz, MethodHandles.lookup(), arena, size);
    }
    
    static <T extends Record> Mem<T> record(Class<T> clazz, Arena arena, long size) {
        return of(clazz, arena, size);
    }
    
    static <T> Mem<T> union(Class<T> clazz, Arena arena, long size){
        throw new UnsupportedOperationException("Not implemented yet");
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
}
