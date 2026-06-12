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

final class TypeMetadataCache {
    private TypeMetadataCache() {}

    private static final ClassValue<TypeMetadata> TYPES = new ClassValue<>() {
        @Override
        protected TypeMetadata computeValue(Class<?> type) {
            if (!type.isRecord()) {
                throw new IllegalArgumentException("Must be record");
            }

            @SuppressWarnings("unchecked")
            Class<? extends Record> recordType = (Class<? extends Record>) type;
            return new TypeMetadata(recordType, MemLayout.of(recordType));
        }
    };

    static TypeMetadata get(Class<? extends Record> type) {
        return TYPES.get(type);
    }
}
