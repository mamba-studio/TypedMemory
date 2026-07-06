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

/**
 * A typed discriminator value for a tagged union case.
 *
 * @param type the primitive tag carrier type
 * @param bits the normalized tag bits
 */
public record TagValue(Class<?> type, long bits) {

    public TagValue {
        Objects.requireNonNull(type);
        if (!isSupported(type))
            throw new IllegalArgumentException("Unsupported tag type: " + type.getName());
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

    static boolean isSupported(Class<?> type) {
        return type == byte.class
                || type == short.class
                || type == int.class
                || type == long.class
                || type == boolean.class
                || type == char.class;
    }
}
