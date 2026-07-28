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

import java.util.Objects;
import java.util.function.DoubleFunction;
import java.util.function.IntFunction;
import java.util.function.LongFunction;

/// Primitive-specialized transformations for bulk-initializing memory views.
///
/// A transformation converts each primitive input to the record value stored
/// by a {@link Mem}. Primitive array parameters avoid boxing the supplied
/// values. The number of values must exactly match the size of the destination
/// memory view.
///
/// {@snippet :
/// record IntValue(int value) {}
///
/// Mem<IntValue> values = Mem.of(IntValue.class, arena, 4);
/// MemTransforms.transform(values, IntValue::new, 1, 4, 3, 2);
/// }
public final class MemTransforms {

    private MemTransforms() {}

    /// Transforms boolean values into records and stores them in {@code mem}.
    ///
    /// @param <R> the destination record type
    /// @param mem the destination memory view
    /// @param transform converts each primitive value to a record
    /// @param values the values to transform; their count must equal {@code mem.size()}
    /// @return {@code mem}
    /// @throws IllegalArgumentException if the value count differs from {@code mem.size()}
    /// @throws NullPointerException if {@code mem} or {@code transform} is null
    public static <R extends Record> Mem<R> transform(
            Mem<R> mem,
            BooleanFunction<? extends R> transform,
            boolean... values) {
        Objects.requireNonNull(transform, "transform");
        checkSize(mem, values.length);
        for (int i = 0; i < values.length; i++) {
            mem.set(i, transform.apply(values[i]));
        }
        return mem;
    }

    /// Transforms boolean values into records starting at {@code index}.
    ///
    /// @param <R> the destination record type
    /// @param mem the destination memory view
    /// @param index the destination index of the first transformed value
    /// @param transform converts each primitive value to a record
    /// @param values the values to transform
    /// @return {@code mem}
    /// @throws IndexOutOfBoundsException if the destination range is outside {@code mem}
    /// @throws NullPointerException if {@code mem} or {@code transform} is null
    public static <R extends Record> Mem<R> transformAt(
            Mem<R> mem,
            long index,
            BooleanFunction<? extends R> transform,
            boolean... values) {
        Objects.requireNonNull(transform, "transform");
        checkRange(mem, index, values.length);
        for (int i = 0; i < values.length; i++) {
            mem.set(index + i, transform.apply(values[i]));
        }
        return mem;
    }

    /// Transforms byte values into records and stores them in {@code mem}.
    ///
    /// @param <R> the destination record type
    /// @param mem the destination memory view
    /// @param transform converts each primitive value to a record
    /// @param values the values to transform; their count must equal {@code mem.size()}
    /// @return {@code mem}
    /// @throws IllegalArgumentException if the value count differs from {@code mem.size()}
    /// @throws NullPointerException if {@code mem} or {@code transform} is null
    public static <R extends Record> Mem<R> transform(
            Mem<R> mem,
            ByteFunction<? extends R> transform,
            byte... values) {
        Objects.requireNonNull(transform, "transform");
        checkSize(mem, values.length);
        for (int i = 0; i < values.length; i++) {
            mem.set(i, transform.apply(values[i]));
        }
        return mem;
    }

    /// Transforms byte values into records starting at {@code index}.
    ///
    /// @param <R> the destination record type
    /// @param mem the destination memory view
    /// @param index the destination index of the first transformed value
    /// @param transform converts each primitive value to a record
    /// @param values the values to transform
    /// @return {@code mem}
    /// @throws IndexOutOfBoundsException if the destination range is outside {@code mem}
    /// @throws NullPointerException if {@code mem} or {@code transform} is null
    public static <R extends Record> Mem<R> transformAt(
            Mem<R> mem,
            long index,
            ByteFunction<? extends R> transform,
            byte... values) {
        Objects.requireNonNull(transform, "transform");
        checkRange(mem, index, values.length);
        for (int i = 0; i < values.length; i++) {
            mem.set(index + i, transform.apply(values[i]));
        }
        return mem;
    }

    /// Transforms short values into records and stores them in {@code mem}.
    ///
    /// @param <R> the destination record type
    /// @param mem the destination memory view
    /// @param transform converts each primitive value to a record
    /// @param values the values to transform; their count must equal {@code mem.size()}
    /// @return {@code mem}
    /// @throws IllegalArgumentException if the value count differs from {@code mem.size()}
    /// @throws NullPointerException if {@code mem} or {@code transform} is null
    public static <R extends Record> Mem<R> transform(
            Mem<R> mem,
            ShortFunction<? extends R> transform,
            short... values) {
        Objects.requireNonNull(transform, "transform");
        checkSize(mem, values.length);
        for (int i = 0; i < values.length; i++) {
            mem.set(i, transform.apply(values[i]));
        }
        return mem;
    }

    /// Transforms short values into records starting at {@code index}.
    ///
    /// @param <R> the destination record type
    /// @param mem the destination memory view
    /// @param index the destination index of the first transformed value
    /// @param transform converts each primitive value to a record
    /// @param values the values to transform
    /// @return {@code mem}
    /// @throws IndexOutOfBoundsException if the destination range is outside {@code mem}
    /// @throws NullPointerException if {@code mem} or {@code transform} is null
    public static <R extends Record> Mem<R> transformAt(
            Mem<R> mem,
            long index,
            ShortFunction<? extends R> transform,
            short... values) {
        Objects.requireNonNull(transform, "transform");
        checkRange(mem, index, values.length);
        for (int i = 0; i < values.length; i++) {
            mem.set(index + i, transform.apply(values[i]));
        }
        return mem;
    }

    /// Transforms char values into records and stores them in {@code mem}.
    ///
    /// @param <R> the destination record type
    /// @param mem the destination memory view
    /// @param transform converts each primitive value to a record
    /// @param values the values to transform; their count must equal {@code mem.size()}
    /// @return {@code mem}
    /// @throws IllegalArgumentException if the value count differs from {@code mem.size()}
    /// @throws NullPointerException if {@code mem} or {@code transform} is null
    public static <R extends Record> Mem<R> transform(
            Mem<R> mem,
            CharFunction<? extends R> transform,
            char... values) {
        Objects.requireNonNull(transform, "transform");
        checkSize(mem, values.length);
        for (int i = 0; i < values.length; i++) {
            mem.set(i, transform.apply(values[i]));
        }
        return mem;
    }

    /// Transforms char values into records starting at {@code index}.
    ///
    /// @param <R> the destination record type
    /// @param mem the destination memory view
    /// @param index the destination index of the first transformed value
    /// @param transform converts each primitive value to a record
    /// @param values the values to transform
    /// @return {@code mem}
    /// @throws IndexOutOfBoundsException if the destination range is outside {@code mem}
    /// @throws NullPointerException if {@code mem} or {@code transform} is null
    public static <R extends Record> Mem<R> transformAt(
            Mem<R> mem,
            long index,
            CharFunction<? extends R> transform,
            char... values) {
        Objects.requireNonNull(transform, "transform");
        checkRange(mem, index, values.length);
        for (int i = 0; i < values.length; i++) {
            mem.set(index + i, transform.apply(values[i]));
        }
        return mem;
    }

    /// Transforms int values into records and stores them in {@code mem}.
    ///
    /// @param <R> the destination record type
    /// @param mem the destination memory view
    /// @param transform converts each primitive value to a record
    /// @param values the values to transform; their count must equal {@code mem.size()}
    /// @return {@code mem}
    /// @throws IllegalArgumentException if the value count differs from {@code mem.size()}
    /// @throws NullPointerException if {@code mem} or {@code transform} is null
    public static <R extends Record> Mem<R> transform(
            Mem<R> mem,
            IntFunction<? extends R> transform,
            int... values) {
        Objects.requireNonNull(transform, "transform");
        checkSize(mem, values.length);
        for (int i = 0; i < values.length; i++) {
            mem.set(i, transform.apply(values[i]));
        }
        return mem;
    }

    /// Transforms int values into records starting at {@code index}.
    ///
    /// @param <R> the destination record type
    /// @param mem the destination memory view
    /// @param index the destination index of the first transformed value
    /// @param transform converts each primitive value to a record
    /// @param values the values to transform
    /// @return {@code mem}
    /// @throws IndexOutOfBoundsException if the destination range is outside {@code mem}
    /// @throws NullPointerException if {@code mem} or {@code transform} is null
    public static <R extends Record> Mem<R> transformAt(
            Mem<R> mem,
            long index,
            IntFunction<? extends R> transform,
            int... values) {
        Objects.requireNonNull(transform, "transform");
        checkRange(mem, index, values.length);
        for (int i = 0; i < values.length; i++) {
            mem.set(index + i, transform.apply(values[i]));
        }
        return mem;
    }

    /// Transforms long values into records and stores them in {@code mem}.
    ///
    /// @param <R> the destination record type
    /// @param mem the destination memory view
    /// @param transform converts each primitive value to a record
    /// @param values the values to transform; their count must equal {@code mem.size()}
    /// @return {@code mem}
    /// @throws IllegalArgumentException if the value count differs from {@code mem.size()}
    /// @throws NullPointerException if {@code mem} or {@code transform} is null
    public static <R extends Record> Mem<R> transform(
            Mem<R> mem,
            LongFunction<? extends R> transform,
            long... values) {
        Objects.requireNonNull(transform, "transform");
        checkSize(mem, values.length);
        for (int i = 0; i < values.length; i++) {
            mem.set(i, transform.apply(values[i]));
        }
        return mem;
    }

    /// Transforms long values into records starting at {@code index}.
    ///
    /// @param <R> the destination record type
    /// @param mem the destination memory view
    /// @param index the destination index of the first transformed value
    /// @param transform converts each primitive value to a record
    /// @param values the values to transform
    /// @return {@code mem}
    /// @throws IndexOutOfBoundsException if the destination range is outside {@code mem}
    /// @throws NullPointerException if {@code mem} or {@code transform} is null
    public static <R extends Record> Mem<R> transformAt(
            Mem<R> mem,
            long index,
            LongFunction<? extends R> transform,
            long... values) {
        Objects.requireNonNull(transform, "transform");
        checkRange(mem, index, values.length);
        for (int i = 0; i < values.length; i++) {
            mem.set(index + i, transform.apply(values[i]));
        }
        return mem;
    }

    /// Transforms float values into records and stores them in {@code mem}.
    ///
    /// @param <R> the destination record type
    /// @param mem the destination memory view
    /// @param transform converts each primitive value to a record
    /// @param values the values to transform; their count must equal {@code mem.size()}
    /// @return {@code mem}
    /// @throws IllegalArgumentException if the value count differs from {@code mem.size()}
    /// @throws NullPointerException if {@code mem} or {@code transform} is null
    public static <R extends Record> Mem<R> transform(
            Mem<R> mem,
            FloatFunction<? extends R> transform,
            float... values) {
        Objects.requireNonNull(transform, "transform");
        checkSize(mem, values.length);
        for (int i = 0; i < values.length; i++) {
            mem.set(i, transform.apply(values[i]));
        }
        return mem;
    }

    /// Transforms float values into records starting at {@code index}.
    ///
    /// @param <R> the destination record type
    /// @param mem the destination memory view
    /// @param index the destination index of the first transformed value
    /// @param transform converts each primitive value to a record
    /// @param values the values to transform
    /// @return {@code mem}
    /// @throws IndexOutOfBoundsException if the destination range is outside {@code mem}
    /// @throws NullPointerException if {@code mem} or {@code transform} is null
    public static <R extends Record> Mem<R> transformAt(
            Mem<R> mem,
            long index,
            FloatFunction<? extends R> transform,
            float... values) {
        Objects.requireNonNull(transform, "transform");
        checkRange(mem, index, values.length);
        for (int i = 0; i < values.length; i++) {
            mem.set(index + i, transform.apply(values[i]));
        }
        return mem;
    }

    /// Transforms double values into records and stores them in {@code mem}.
    ///
    /// @param <R> the destination record type
    /// @param mem the destination memory view
    /// @param transform converts each primitive value to a record
    /// @param values the values to transform; their count must equal {@code mem.size()}
    /// @return {@code mem}
    /// @throws IllegalArgumentException if the value count differs from {@code mem.size()}
    /// @throws NullPointerException if {@code mem} or {@code transform} is null
    public static <R extends Record> Mem<R> transform(
            Mem<R> mem,
            DoubleFunction<? extends R> transform,
            double... values) {
        Objects.requireNonNull(transform, "transform");
        checkSize(mem, values.length);
        for (int i = 0; i < values.length; i++) {
            mem.set(i, transform.apply(values[i]));
        }
        return mem;
    }

    /// Transforms double values into records starting at {@code index}.
    ///
    /// @param <R> the destination record type
    /// @param mem the destination memory view
    /// @param index the destination index of the first transformed value
    /// @param transform converts each primitive value to a record
    /// @param values the values to transform
    /// @return {@code mem}
    /// @throws IndexOutOfBoundsException if the destination range is outside {@code mem}
    /// @throws NullPointerException if {@code mem} or {@code transform} is null
    public static <R extends Record> Mem<R> transformAt(
            Mem<R> mem,
            long index,
            DoubleFunction<? extends R> transform,
            double... values) {
        Objects.requireNonNull(transform, "transform");
        checkRange(mem, index, values.length);
        for (int i = 0; i < values.length; i++) {
            mem.set(index + i, transform.apply(values[i]));
        }
        return mem;
    }

    private static void checkSize(Mem<?> mem, int valueCount) {
        Objects.requireNonNull(mem, "mem");
        if (mem.size() != valueCount) {
            throw new IllegalArgumentException(
                    "Value count " + valueCount
                    + " != memory size " + mem.size());
        }
    }

    private static void checkRange(Mem<?> mem, long index, int valueCount) {
        Objects.requireNonNull(mem, "mem");
        if (index < 0 || index > mem.size() || valueCount > mem.size() - index) {
            throw new IndexOutOfBoundsException(
                    "Index: " + index
                    + ", Value count: " + valueCount
                    + ", Size: " + mem.size());
        }
    }

    /// Converts a primitive boolean value to a result.
    ///
    /// @param <R> the result type
    @FunctionalInterface
    public interface BooleanFunction<R> {
        /// Converts a value.
        ///
        /// @param value the primitive value
        /// @return the converted result
        R apply(boolean value);
    }

    /// Converts a primitive byte value to a result.
    ///
    /// @param <R> the result type
    @FunctionalInterface
    public interface ByteFunction<R> {
        /// Converts a value.
        ///
        /// @param value the primitive value
        /// @return the converted result
        R apply(byte value);
    }

    /// Converts a primitive short value to a result.
    ///
    /// @param <R> the result type
    @FunctionalInterface
    public interface ShortFunction<R> {
        /// Converts a value.
        ///
        /// @param value the primitive value
        /// @return the converted result
        R apply(short value);
    }

    /// Converts a primitive char value to a result.
    ///
    /// @param <R> the result type
    @FunctionalInterface
    public interface CharFunction<R> {
        /// Converts a value.
        ///
        /// @param value the primitive value
        /// @return the converted result
        R apply(char value);
    }

    /// Converts a primitive float value to a result.
    ///
    /// @param <R> the result type
    @FunctionalInterface
    public interface FloatFunction<R> {
        /// Converts a value.
        ///
        /// @param value the primitive value
        /// @return the converted result
        R apply(float value);
    }
}
