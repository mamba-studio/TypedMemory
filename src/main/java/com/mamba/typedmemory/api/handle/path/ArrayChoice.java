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

import java.util.List;
import java.util.Objects;

/**
 * A uniform shape for every element of a record array field.
 *
 * @param fieldName the array field name
 * @param elementType the record element type
 * @param children nested choices inside each element
 */
public record ArrayChoice(
        String fieldName,
        Class<? extends Record> elementType,
        List<ShapeChoice> children) implements ShapeChoice {

    public ArrayChoice {
        Objects.requireNonNull(fieldName);
        Objects.requireNonNull(elementType);
        children = List.copyOf(children);
        if (fieldName.isBlank())
            throw new IllegalArgumentException("Field name cannot be blank");
    }
}
