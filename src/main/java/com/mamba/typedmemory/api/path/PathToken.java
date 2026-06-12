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
package com.mamba.typedmemory.api.path;

import java.util.Objects;

/**
 *
 * @author joemw
 */
public sealed interface PathToken {

    record Type(Class<?> type) implements PathToken {
        public Type {
            Objects.requireNonNull(type);
        }
        
        @Override
        public String toString(){
            return type.getSimpleName()+ ".class";
        }
    }

    record Field(String name) implements PathToken {
        public Field {
            Objects.requireNonNull(name);
            if (name.isBlank()) {
                throw new IllegalArgumentException("Field name cannot be blank");
            }
        }
        @Override
        public String toString(){
            return "\"%s\"".formatted(name);
        }
    }

    static PathToken of(Object value) {
        return switch (value) {
            case Class<?> c -> new Type(c);
            case String s -> new Field(s);
            case PathToken t -> t;
            case null -> throw new NullPointerException("Path token cannot be null");
            default -> throw new IllegalArgumentException(
                    "Unsupported path token: " + value
            );
        };
    }
}
