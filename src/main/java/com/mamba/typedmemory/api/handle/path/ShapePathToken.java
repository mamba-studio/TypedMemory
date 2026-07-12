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
 * One structural token in a generated shape path.
 */
public sealed interface ShapePathToken
        permits ShapePathToken.Type, ShapePathToken.Field,
        ShapePathToken.ArrayField, ShapePathToken.Variant {

    /**
     * A declared or concrete type reached by the path.
     *
     * @param type the Java type
     */
    record Type(Class<?> type) implements ShapePathToken {
        public Type {
            Objects.requireNonNull(type);
        }
    }

    /**
     * A record field reached by the path.
     *
     * @param name the field name
     * @param type the declared field type
     */
    record Field(String name, Class<?> type) implements ShapePathToken {
        public Field {
            Objects.requireNonNull(name);
            Objects.requireNonNull(type);
            if (name.isBlank())
                throw new IllegalArgumentException("Field name cannot be blank");
        }
    }

    /**
     * A record array field reached by the path.
     *
     * @param name the field name
     * @param arrayType the declared array type
     * @param elementType the declared element type
     */
    record ArrayField(String name, Class<?> arrayType, Class<?> elementType) implements ShapePathToken {
        public ArrayField {
            Objects.requireNonNull(name);
            Objects.requireNonNull(arrayType);
            Objects.requireNonNull(elementType);
            if (name.isBlank())
                throw new IllegalArgumentException("Field name cannot be blank");
            if (!arrayType.isArray())
                throw new IllegalArgumentException("Array field type must be an array: " + arrayType.getName());
        }
    }

    /**
     * A concrete variant route that makes deeper paths reachable.
     *
     * @param unionType the declared union type
     * @param variantType the selected concrete record variant type
     */
    record Variant(Class<?> unionType, Class<? extends Record> variantType) implements ShapePathToken {
        public Variant {
            Objects.requireNonNull(unionType);
            Objects.requireNonNull(variantType);
        }
    }
}
