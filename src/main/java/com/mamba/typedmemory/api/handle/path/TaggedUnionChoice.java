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
 * A dynamic union shape where one record stores a sibling tag and payload.
 *
 * @param fieldName the union payload field name
 * @param tagFieldName the tag field in the same record
 * @param tagType the native tag carrier type
 * @param semanticTagType the semantic tag value type
 * @param unionType the declared payload union type
 * @param cases tag-to-variant mappings
 */
public record TaggedUnionChoice(
        String fieldName,
        String tagFieldName,
        Class<?> tagType,
        Class<?> semanticTagType,
        Class<?> unionType,
        List<TaggedUnionCase> cases) implements ShapeChoice {

    public TaggedUnionChoice {
        Objects.requireNonNull(fieldName);
        Objects.requireNonNull(tagFieldName);
        Objects.requireNonNull(tagType);
        Objects.requireNonNull(semanticTagType);
        Objects.requireNonNull(unionType);
        cases = List.copyOf(cases);
        if (fieldName.isBlank())
            throw new IllegalArgumentException("Payload field name cannot be blank");
        if (tagFieldName.isBlank())
            throw new IllegalArgumentException("Tag field name cannot be blank");
        if (!TagValue.isNativeSupported(tagType))
            throw new IllegalArgumentException("Unsupported tag type: " + tagType.getName());
        if (!TagValue.isSupported(semanticTagType))
            throw new IllegalArgumentException("Unsupported semantic tag type: " + semanticTagType.getName());
        if (cases.isEmpty())
            throw new IllegalArgumentException("Tagged union must have at least one case");
    }

    @Override
    public List<ShapeChoice> children() {
        return List.of();
    }
}
