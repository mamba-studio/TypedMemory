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
package com.mamba.typedmemory.api.handle.path;

import com.mamba.typedmemory.api.size;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builders for handle paths.
 */
public final class MemPaths {
    private MemPaths() {
    }

    /**
     * Starts a path at a root record type.
     *
     * @param <R> the root record type
     * @param root the root record class
     * @return a path builder
     */
    public static <R extends Record> Builder<R, R> from(Class<R> root) {
        Objects.requireNonNull(root);
        requireRecord(root, "Root type");
        return new Builder<>(root, root, List.of(new HandlePathToken.Type(root)), 0);
    }

    /**
     * Builder positioned at a record type.
     *
     * @param <R> the root record type
     * @param <C> the current record type
     */
    public record Builder<R extends Record, C extends Record>(
            Class<R> rootType,
            Class<C> currentType,
            List<HandlePathToken> tokens,
            int openCoordinateCount) {

        public Builder {
            Objects.requireNonNull(rootType);
            Objects.requireNonNull(currentType);
            tokens = List.copyOf(tokens);
        }

        /**
         * Selects a nested record field.
         *
         * @param <N> the nested record type
         * @param name the record component name
         * @param nextType the expected nested record type
         * @return a builder positioned at the nested record
         */
        public <N extends Record> Builder<R, N> field(String name, Class<N> nextType) {
            Objects.requireNonNull(nextType);
            requireRecord(nextType, "Field type");
            var component = component(currentType, name);
            if (component.getType() != nextType) {
                throw new IllegalArgumentException(
                        "%s.%s is %s, not %s".formatted(
                                currentType.getSimpleName(),
                                name,
                                component.getType().getSimpleName(),
                                nextType.getSimpleName()));
            }

            var out = new ArrayList<>(tokens);
            out.add(new HandlePathToken.Field(name));
            out.add(new HandlePathToken.Type(nextType));
            return new Builder<>(rootType, nextType, out, openCoordinateCount);
        }

        /**
         * Selects an array field whose elements are records.
         *
         * @param <E> the record element type
         * @param name the array record component name
         * @param elementType the expected record element type
         * @return an array-coordinate builder
         */
        public <E extends Record> ArrayBuilder<R, E> array(String name, Class<E> elementType) {
            Objects.requireNonNull(elementType);
            requireRecord(elementType, "Array element type");

            var component = component(currentType, name);
            var fieldType = component.getType();
            if (!fieldType.isArray() || fieldType.getComponentType() != elementType) {
                throw new IllegalArgumentException(
                        "%s.%s is %s, not %s[]".formatted(
                                currentType.getSimpleName(),
                                name,
                                fieldType.getSimpleName(),
                                elementType.getSimpleName()));
            }

            var out = new ArrayList<>(tokens);
            out.add(new HandlePathToken.Field(name));
            out.add(new HandlePathToken.Type(fieldType));
            return new ArrayBuilder<>(
                    rootType, elementType, out, openCoordinateCount, arraySize(component));
        }

        /**
         * Finishes this path as a record-region path.
         *
         * @return an immutable region path
         */
        public RegionPath<R, C> region() {
            return new RegionPathImpl<>(rootType, currentType, tokens, openCoordinateCount);
        }
    }

    /**
     * Builder positioned at an array field before its element coordinate has
     * been selected.
     *
     * @param <R> the root record type
     * @param <E> the array element record type
     */
    public record ArrayBuilder<R extends Record, E extends Record>(
            Class<R> rootType,
            Class<E> elementType,
            List<HandlePathToken> tokens,
            int openCoordinateCount,
            long size) {

        public ArrayBuilder {
            Objects.requireNonNull(rootType);
            Objects.requireNonNull(elementType);
            tokens = List.copyOf(tokens);
        }

        /**
         * Selects a fixed array element.
         *
         * @param index the fixed element index
         * @return a builder positioned at the element record type
         */
        public Builder<R, E> at(long index) {
            if (index < 0 || index >= size)
                throw new IndexOutOfBoundsException(
                        "Array path index: " + index + ", Size: " + size);
            var out = new ArrayList<>(tokens);
            out.add(new HandlePathToken.Index(index));
            out.add(new HandlePathToken.Type(elementType));
            return new Builder<>(rootType, elementType, out, openCoordinateCount);
        }

        /**
         * Leaves this array coordinate open for handle access.
         *
         * @return a builder positioned at the element record type
         */
        public Builder<R, E> any() {
            var out = new ArrayList<>(tokens);
            out.add(new HandlePathToken.AnyIndex());
            out.add(new HandlePathToken.Type(elementType));
            return new Builder<>(rootType, elementType, out, openCoordinateCount + 1);
        }
    }

    private static void requireRecord(Class<?> type, String label) {
        if (!type.isRecord())
            throw new IllegalArgumentException(label + " must be a record: " + type.getName());
    }

    private static RecordComponent component(Class<?> recordType, String name) {
        Objects.requireNonNull(name);
        if (!recordType.isRecord())
            throw new IllegalArgumentException("Cannot select a field from non-record type: " + recordType.getName());
        for (var component : recordType.getRecordComponents()) {
            if (component.getName().equals(name))
                return component;
        }
        throw new IllegalArgumentException(
                "Unknown field '%s' in %s".formatted(name, recordType.getSimpleName()));
    }

    private static long arraySize(RecordComponent component) {
        var annotation = component.getAnnotation(size.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    "Missing @size on array component: "
                    + component.getDeclaringRecord().getSimpleName()
                    + "." + component.getName());
        }
        return annotation.value();
    }
}
