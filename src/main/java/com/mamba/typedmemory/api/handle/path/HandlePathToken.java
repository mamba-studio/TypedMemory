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
 * A structural token in a handle path.
 */
public sealed interface HandlePathToken{

    /**
     * A type coordinate.
     *
     * @param type the selected type
     */
    record Type(Class<?> type) implements HandlePathToken {
        public Type {
            Objects.requireNonNull(type);
        }

        @Override
        public String toString() {
            return type.getSimpleName() + ".class";
        }
    }

    /**
     * A record field coordinate.
     *
     * @param name the record component name
     */
    record Field(String name) implements HandlePathToken {
        public Field {
            Objects.requireNonNull(name);
            if (name.isBlank())
                throw new IllegalArgumentException("Field name cannot be blank");
        }

        @Override
        public String toString() {
            return "\"%s\"".formatted(name);
        }
    }

    /**
     * A fixed array element coordinate.
     *
     * @param index the fixed element index
     */
    record Index(long index) implements HandlePathToken {
        public Index {
            if (index < 0)
                throw new IndexOutOfBoundsException("Array path index: " + index);
        }

        @Override
        public String toString() {
            return "[" + index + "]";
        }
    }

    /**
     * An open array coordinate supplied when the handle is accessed.
     */
    record AnyIndex() implements HandlePathToken {
        @Override
        public String toString() {
            return "[*]";
        }
    }
}
