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

/**
 * {@summary Factory methods for native null memory references.}
 *
 * <p>This class represents the native null address, not the Java
 * {@code null} reference. Use {@link #of()} when no element type is known and
 * {@link #of(Class)} when runtime type metadata must be retained.
 */
public final class Nulls {

    private Nulls() {
    }

    /**
     * Returns the untyped native null pointer.
     *
     * @return {@link Ptr#NULL}
     */
    public static Ptr of() {
        return Ptr.NULL;
    }

    /**
     * Returns a typed reference to the native null address.
     *
     * @param <T> the record element type
     * @param type the record element class
     * @return a typed native-null reference retaining {@code type} and its
     *         derived layout
     * @throws NullPointerException if {@code type} is null
     */
    public static <T extends Record> RawMem<T> of(Class<T> type) {
        return RawMem.of(type);
    }
}
