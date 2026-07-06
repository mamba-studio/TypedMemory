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
 * A uniform concrete variant shape for every element of a union array field.
 *
 * @param fieldName the array field name
 * @param unionType the declared union element type
 * @param variantType the selected record variant type
 * @param children nested choices inside the selected variant
 */
public record UnionArrayChoice(
        String fieldName,
        Class<?> unionType,
        Class<? extends Record> variantType,
        List<ShapeChoice> children) implements ShapeChoice {

    public UnionArrayChoice {
        Objects.requireNonNull(fieldName);
        Objects.requireNonNull(unionType);
        Objects.requireNonNull(variantType);
        children = List.copyOf(children);
        if (fieldName.isBlank())
            throw new IllegalArgumentException("Field name cannot be blank");
    }
}
