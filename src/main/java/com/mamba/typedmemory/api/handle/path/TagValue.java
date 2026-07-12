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

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * A typed discriminator value for a tagged union case.
 *
 * @param type the semantic tag type
 * @param value the semantic tag value
 */
public record TagValue(Class<?> type, Object value) {

    public TagValue {
        Objects.requireNonNull(type);
        Objects.requireNonNull(value);
        if (!isSupported(type))
            throw new IllegalArgumentException("Unsupported tag type: " + type.getName());
        if (type.isPrimitive()) {
            if (!wrapperType(type).isInstance(value)) {
                throw new IllegalArgumentException(
                        "Tag value %s is not %s".formatted(value, type.getSimpleName()));
            }
        } else if (!type.isInstance(value)) {
            throw new IllegalArgumentException(
                    "Tag value %s is not %s".formatted(value, type.getSimpleName()));
        }
    }

    public static TagValue of(byte value) {
        return new TagValue(byte.class, value);
    }

    public static TagValue of(short value) {
        return new TagValue(short.class, value);
    }

    public static TagValue of(int value) {
        return new TagValue(int.class, value);
    }

    public static TagValue of(long value) {
        return new TagValue(long.class, value);
    }

    public static TagValue of(boolean value) {
        return new TagValue(boolean.class, value ? 1L : 0L);
    }

    public static TagValue of(char value) {
        return new TagValue(char.class, value);
    }

    public static <T extends Record> TagValue of(T value) {
        Objects.requireNonNull(value);
        return new TagValue(value.getClass(), value);
    }

    static boolean isSupported(Class<?> type) {
        return isNativeSupported(type) || type.isRecord();
    }

    static boolean isNativeSupported(Class<?> type) {
        return type == byte.class
                || type == short.class
                || type == int.class
                || type == long.class
                || type == boolean.class
                || type == char.class;
    }

    String sortKey() {
        return type.getName() + ":" + valueKey(value);
    }

    private static Class<?> wrapperType(Class<?> primitiveType) {
        if (primitiveType == byte.class)
            return Byte.class;
        if (primitiveType == short.class)
            return Short.class;
        if (primitiveType == int.class)
            return Integer.class;
        if (primitiveType == long.class)
            return Long.class;
        if (primitiveType == boolean.class)
            return Boolean.class;
        if (primitiveType == char.class)
            return Character.class;
        throw new IllegalArgumentException("Unsupported primitive tag type: " + primitiveType.getName());
    }

    private static String valueKey(Object value) {
        var type = value.getClass();
        if (!type.isRecord())
            return String.valueOf(value);

        var out = new StringBuilder(type.getName()).append('(');
        var components = type.getRecordComponents();
        for (var i = 0; i < components.length; i++) {
            if (i > 0)
                out.append(',');
            var component = components[i];
            out.append(component.getName()).append('=');
            try {
                var accessor = component.getAccessor();
                if (!accessor.canAccess(value))
                    accessor.setAccessible(true);
                out.append(valueKey(accessor.invoke(value)));
            } catch (IllegalAccessException | InvocationTargetException ex) {
                throw new IllegalStateException(
                        "Cannot read tag record component " + type.getSimpleName() + "." + component.getName(), ex);
            }
        }
        return out.append(')').toString();
    }
}
