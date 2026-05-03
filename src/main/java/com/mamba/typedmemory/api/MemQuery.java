/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.typedmemory.api;

import com.mamba.typedmemory.api.MemQuery.Stage.FilterStage;
import com.mamba.typedmemory.api.MemQuery.Stage.MapStage;
import com.mamba.typedmemory.api.MemQuery.Stage.SkipStage;
import com.mamba.typedmemory.api.MemQuery.Stage.TakeStage;
import com.mamba.typedmemory.api.ops.IntermediateOps;
import com.mamba.typedmemory.api.ops.TerminalOps;

import module java.base;

/**
 * {@summary A lazy query pipeline for processing elements stored in {@link Mem}.}
 *
 * <p>
 * {@code MemQuery} represents a sequence of operations applied to a
 * {@link Mem} memory region. It provides a functional-style pipeline for
 * traversing and transforming structured memory data.
 *
 * <p>
 * Query pipelines are composed of intermediate operations such as
 * {@link #filter(java.util.function.Predicate)} and
 * {@link #map(java.util.function.Function)}. These operations do not
 * immediately process elements. Instead, they build a chain of stages
 * describing how elements should be processed.
 *
 * <p>
 * The pipeline is executed only when a <em>terminal operation</em> is invoked,
 * such as {@link #count()}, {@link #forEach(java.util.function.Consumer)},
 * {@link #findFirst()}, or {@link #reduce(Object, java.util.function.BinaryOperator)}.
 *
 * <h2>Example</h2>
 *
 * {@snippet :
 * record Color(float r, float g, float b) {}
 *
 * try (Arena arena = Arena.ofConfined()) {
 *     Mem<Color> colors = Mem.of(Color.class, arena, 100);
 *
 *     long bright = colors.query()
 *         .filter(c -> c.r() > 0.5f)
 *         .count();
 * }
 * }
 *
 * <h2>Lazy Evaluation</h2>
 *
 * <p>
 * Intermediate operations are evaluated lazily. Instead of producing
 * intermediate collections, each stage forwards elements directly
 * to the next stage in the pipeline.
 *
 * <p>
 * This approach minimizes memory allocation and allows efficient
 * traversal of structured memory regions.
 *
 * <h2>Pipeline Structure</h2>
 *
 * <p>
 * Internally, a {@code MemQuery} pipeline is represented as a chain of
 * processing stages. Each stage performs a specific operation such as
 * filtering, mapping, limiting, or skipping elements.
 *
 * <p>
 * When a terminal operation is invoked, the pipeline is executed by
 * traversing the source memory region and passing each element through
 * the chain of stages.
 *
 * <h2>Transformation</h2>
 *
 * <p>
 * Mapping operations allow transforming the element type flowing through
 * the pipeline.
 *
 * {@snippet :
 * int sum = colors.query()
 *     .map(c -> (int)(c.r() * 255))
 *     .reduce(0, Integer::sum);
 * }
 *
 * <p>
 * This enables pipelines where the output type differs from the source
 * memory element type.
 *
 * <h2>Short-Circuiting Operations</h2>
 *
 * <p>
 * Some terminal operations terminate early when a result is found.
 * For example, {@link #findFirst()} stops traversal as soon as a matching
 * element is encountered.
 *
 * {@snippet :
 * var firstBright = colors.query()
 *     .filter(c -> c.r() > 0.9f)
 *     .findFirst();
 * }
 *
 * <h2>Design Goals</h2>
 *
 * <ul>
 * <li>Provide a functional pipeline for memory traversal</li>
 * <li>Avoid intermediate memory allocations</li>
 * <li>Enable lazy evaluation of query operations</li>
 * <li>Support transformation of element types</li>
 * </ul>
 *
 * <p>
 * {@code MemQuery} is conceptually similar to the Java Stream API,
 * but optimized for structured memory traversal rather than object
 * collections.
 *
 * @param <S> the source element type
 * @param <T> the current pipeline element type
 */
public final class MemQuery<S, T> implements IntermediateOps<S, T>, TerminalOps<T>{
    
    private final Stage<S, T> stage;

    MemQuery(Stage<S, T> stage) {
        this.stage = stage;
    }

    @Override
    public MemQuery<S, T> filter(Predicate<T> predicate) {
        return new MemQuery<>(new FilterStage<>(stage, predicate));
    }

    @Override
    public <U> MemQuery<S, U> map(Function<T, U> mapper) {
        return new MemQuery<>(new MapStage<>(stage, mapper));
    }

    @Override
    public void forEach(Consumer<T> action) {
        stage.forEach(v -> {
            action.accept(v);
            return true; // always continue
        });
    }

    @Override
    public long count() {
        final long[] c = {0};
        stage.forEach(v -> {
            c[0]++;
            return true;
        });
        return c[0];
    }

    @Override
    public boolean any(Predicate<T> p) {
        final boolean[] found = {false};
        stage.forEach(v -> {
            if (p.test(v)) {
                found[0] = true;
                return false;
            }
            return true;
        });
        return found[0];
    }

    @Override
    public boolean all(Predicate<T> predicate) {
        final boolean[] ok = {true};

        stage.forEach(v -> {
            if (!predicate.test(v)) {
                ok[0] = false;
                return false;
            }
            return true;
        });

        return ok[0];
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> op) {
        final Object[] acc = {identity};
        stage.forEach(v -> {
            acc[0] = op.apply((T) acc[0], v);
            return true;
        });
        return (T) acc[0];
    }
    
    @Override
    public MemQuery<S,T> take(long n) {
        return new MemQuery<>(new TakeStage<>(stage, n));
    }
    
    @Override
    public MemQuery<S,T> skip(long n) {
        return new MemQuery<>(new SkipStage<>(stage, n));
    }
    
    public Optional<T> find(Predicate<T> predicate) {
        final Object[] result = {null};
        stage.forEach(v -> {
            if (predicate.test(v)) {
                result[0] = v;
                return false;
            }
            return true;
        });
        return Optional.ofNullable((T) result[0]);
    }
    
    public Optional<T> findFirst() {
        final Object[] result = {null};

        stage.forEach(v -> {
            result[0] = v;
            return false;
        });

        return Optional.ofNullable((T) result[0]);
    }
    
    sealed interface Stage<S, T>{
        boolean forEach(Sink<T> sink);
        Mem<S> source();
                
        record SourceStage<S>(Mem<S> mem) implements Stage<S, S> {
           
            @Override
            public boolean forEach(Sink<S> sink) {
                long n = mem.size();
                for (long i = 0; i < n; i++) {
                    if (!sink.accept(mem.get(i)))
                        return false;
                }
                return true;
            }
            
            @Override
            public Mem<S> source() {
                return mem;
            }            
        }
        
        record FilterStage<S, T>(Stage<S, T> prev, Predicate<T> predicate) implements Stage<S, T> {           
            
            @Override
            public Mem<S> source() {
                return prev.source();
            }

            @Override
            public boolean forEach(Sink<T> sink) {
                return prev.forEach(v -> {
                    if (predicate.test(v))
                        return sink.accept(v);
                    return true;
                });
            }
        }
        
        record MapStage<S, T, U>(Stage<S, T> prev, Function<T, U> mapper) implements Stage<S, U> {           
            
            @Override
            public Mem<S> source() {
                return prev.source();
            }

            @Override
            public boolean forEach(Sink<U> sink) {
                return prev.forEach(v -> sink.accept(mapper.apply(v)));
            }
        }
        
        record TakeStage<S,T>(Stage<S,T> prev, long limit) implements Stage<S,T> {
            @Override
            public boolean forEach(Sink<T> sink) {

                final long[] count = {0};

                return prev.forEach(v -> {
                    if (count[0] >= limit)
                        return false;

                    count[0]++;

                    if (!sink.accept(v))
                        return false;

                    return count[0] < limit;
                });
            }
            
            @Override
            public Mem<S> source() {
                return prev.source();
            }
        }
        
        record SkipStage<S,T>(Stage<S,T> prev, long skip) implements Stage<S,T> {

            @Override
            public boolean forEach(Sink<T> sink) {

                final long[] count = {0};

                return prev.forEach(v -> {

                    if (count[0] < skip) {
                        count[0]++;
                        return true;
                    }

                    return sink.accept(v);
                });
            }
            
            @Override
            public Mem<S> source() {
                return prev.source();
            }
        }
    }
    
    @FunctionalInterface
    interface Sink<T> {
        boolean accept(T value);
    }
}
