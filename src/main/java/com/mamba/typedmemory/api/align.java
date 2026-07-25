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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Requests a byte alignment for a record's native struct layout.
///
/// The value is expressed in bytes. It must be a positive power of two and
/// must not be smaller than the record's natural alignment. TypedMemory adds
/// trailing padding when necessary and applies the requested alignment to the
/// generated {@link java.lang.foreign.StructLayout}. Nested fields of the
/// annotated record type use that alignment, and arrays use the padded record
/// size as their element stride.
///
/// {@snippet :
/// @align(16)
/// record Float3(float x, float y, float z) {}
/// }
///
/// The example has 12 bytes of component data, a 16-byte alignment, and a
/// 16-byte size. This matches the size and alignment of an OpenCL
/// {@code float3}. This annotation is interpreted only for record schemas.
/// {@link ElementType#TYPE TYPE} is used because Java does not define a
/// record-specific annotation target.
///
/// @see MemLayout#of(Class)
/// @since 0.1
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface align {
    /// Returns the requested byte alignment.
    ///
    /// @return the byte alignment
    long value();
}
