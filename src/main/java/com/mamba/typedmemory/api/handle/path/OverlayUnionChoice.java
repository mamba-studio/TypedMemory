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
 * A C-style overlay union where all variants share one storage region and a
 * tag is read through one selected variant layout.
 *
 * @param fieldName the union field name
 * @param unionType the declared union type
 * @param tagVariantType the variant layout used to read the tag
 * @param tagFieldName the tag field in the tag variant
 * @param tagType the primitive tag carrier type
 * @param cases tag-to-variant mappings
 */
public record OverlayUnionChoice(
        String fieldName,
        Class<?> unionType,
        Class<? extends Record> tagVariantType,
        String tagFieldName,
        Class<?> tagType,
        List<TaggedUnionCase> cases) implements ShapeChoice {

    public OverlayUnionChoice {
        Objects.requireNonNull(fieldName);
        Objects.requireNonNull(unionType);
        Objects.requireNonNull(tagVariantType);
        Objects.requireNonNull(tagFieldName);
        Objects.requireNonNull(tagType);
        cases = List.copyOf(cases);
        if (fieldName.isBlank())
            throw new IllegalArgumentException("Field name cannot be blank");
        if (tagFieldName.isBlank())
            throw new IllegalArgumentException("Tag field name cannot be blank");
        if (!TagValue.isSupported(tagType))
            throw new IllegalArgumentException("Unsupported tag type: " + tagType.getName());
        if (cases.isEmpty())
            throw new IllegalArgumentException("Overlay union must have at least one case");
    }

    @Override
    public List<ShapeChoice> children() {
        return List.of();
    }
}
