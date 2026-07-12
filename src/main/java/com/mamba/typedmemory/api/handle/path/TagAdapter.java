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

import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongFunction;

/**
 * Describes how a native tag field is interpreted as a semantic record tag.
 *
 * @param <T> the semantic tag record type
 */
public final class TagAdapter<T extends Record> {
    private final String fieldName;
    private final Class<?> nativeType;
    private final Class<T> tagType;
    private final Object converter;

    private TagAdapter(
            String fieldName,
            Class<?> nativeType,
            Class<T> tagType,
            Object converter) {
        this.fieldName = Objects.requireNonNull(fieldName);
        this.nativeType = Objects.requireNonNull(nativeType);
        this.tagType = Objects.requireNonNull(tagType);
        this.converter = Objects.requireNonNull(converter);
        if (fieldName.isBlank())
            throw new IllegalArgumentException("Tag field name cannot be blank");
        if (!TagValue.isNativeSupported(nativeType))
            throw new IllegalArgumentException("Unsupported native tag type: " + nativeType.getName());
        if (!tagType.isRecord())
            throw new IllegalArgumentException("Semantic tag type must be a record: " + tagType.getName());
    }

    public static <T extends Record> TagAdapter<T> ofByte(
            String fieldName,
            Class<T> tagType,
            Function<Byte, T> converter) {
        Objects.requireNonNull(converter);
        return new TagAdapter<>(fieldName, byte.class, tagType, converter);
    }

    public static <T extends Record> TagAdapter<T> ofShort(
            String fieldName,
            Class<T> tagType,
            Function<Short, T> converter) {
        Objects.requireNonNull(converter);
        return new TagAdapter<>(fieldName, short.class, tagType, converter);
    }

    public static <T extends Record> TagAdapter<T> ofInt(
            String fieldName,
            Class<T> tagType,
            IntFunction<T> converter) {
        Objects.requireNonNull(converter);
        return new TagAdapter<>(fieldName, int.class, tagType, converter);
    }

    public static <T extends Record> TagAdapter<T> ofLong(
            String fieldName,
            Class<T> tagType,
            LongFunction<T> converter) {
        Objects.requireNonNull(converter);
        return new TagAdapter<>(fieldName, long.class, tagType, converter);
    }

    public static <T extends Record> TagAdapter<T> ofBoolean(
            String fieldName,
            Class<T> tagType,
            Function<Boolean, T> converter) {
        Objects.requireNonNull(converter);
        return new TagAdapter<>(fieldName, boolean.class, tagType, converter);
    }

    public static <T extends Record> TagAdapter<T> ofChar(
            String fieldName,
            Class<T> tagType,
            Function<Character, T> converter) {
        Objects.requireNonNull(converter);
        return new TagAdapter<>(fieldName, char.class, tagType, converter);
    }

    public String fieldName() {
        return fieldName;
    }

    public Class<?> nativeType() {
        return nativeType;
    }

    public Class<T> tagType() {
        return tagType;
    }

    public T convert(Object nativeValue) {
        var tag = convertNative(nativeValue);
        if (!tagType.isInstance(tag)) {
            throw new IllegalArgumentException(
                    "Tag adapter returned %s, not %s".formatted(
                            tag == null ? "null" : tag.getClass().getSimpleName(),
                            tagType.getSimpleName()));
        }
        return tag;
    }

    @SuppressWarnings("unchecked")
    private T convertNative(Object nativeValue) {
        Objects.requireNonNull(nativeValue);
        if (nativeType == byte.class)
            return ((Function<Byte, T>) converter).apply((Byte) nativeValue);
        if (nativeType == short.class)
            return ((Function<Short, T>) converter).apply((Short) nativeValue);
        if (nativeType == int.class)
            return ((IntFunction<T>) converter).apply((Integer) nativeValue);
        if (nativeType == long.class)
            return ((LongFunction<T>) converter).apply((Long) nativeValue);
        if (nativeType == boolean.class)
            return ((Function<Boolean, T>) converter).apply((Boolean) nativeValue);
        if (nativeType == char.class)
            return ((Function<Character, T>) converter).apply((Character) nativeValue);
        throw new IllegalStateException("Unsupported native tag type: " + nativeType.getName());
    }
}
