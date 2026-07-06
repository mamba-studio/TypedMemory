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
package com.mamba.typedmemory.api.handle.path2;

import java.util.List;

/**
 * No-string shape choice. Paths are record-component names resolved from
 * accessor lambdas.
 */
public sealed interface ShapeChoice2
        permits ShapeChoice2.RecordField, ShapeChoice2.UnionField,
        ShapeChoice2.RecordArray, ShapeChoice2.UnionArray,
        ShapeChoice2.TaggedUnion, ShapeChoice2.TaggedUnionArray,
        ShapeChoice2.OverlayUnion {

    List<String> path();

    List<ShapeChoice2> children();

    record RecordField(
            List<String> path,
            Class<? extends Record> recordType,
            List<ShapeChoice2> children) implements ShapeChoice2 {
        public RecordField {
            path = List.copyOf(path);
            children = List.copyOf(children);
        }
    }

    record UnionField(
            List<String> path,
            Class<?> unionType,
            Class<? extends Record> variantType,
            List<ShapeChoice2> children) implements ShapeChoice2 {
        public UnionField {
            path = List.copyOf(path);
            children = List.copyOf(children);
        }
    }

    record RecordArray(
            List<String> path,
            Class<? extends Record> elementType,
            List<ShapeChoice2> children) implements ShapeChoice2 {
        public RecordArray {
            path = List.copyOf(path);
            children = List.copyOf(children);
        }
    }

    record UnionArray(
            List<String> path,
            Class<?> unionType,
            Class<? extends Record> variantType,
            List<ShapeChoice2> children) implements ShapeChoice2 {
        public UnionArray {
            path = List.copyOf(path);
            children = List.copyOf(children);
        }
    }

    record TaggedUnion(
            List<String> tagPath,
            List<String> payloadPath,
            Class<?> unionType,
            List<TaggedUnionCase2> cases) implements ShapeChoice2 {
        public TaggedUnion {
            tagPath = List.copyOf(tagPath);
            payloadPath = List.copyOf(payloadPath);
            cases = List.copyOf(cases);
        }

        @Override
        public List<String> path() {
            return payloadPath;
        }

        @Override
        public List<ShapeChoice2> children() {
            return List.of();
        }
    }

    record TaggedUnionArray(
            List<String> arrayPath,
            Class<? extends Record> elementType,
            List<String> tagPath,
            List<String> payloadPath,
            Class<?> unionType,
            List<TaggedUnionCase2> cases) implements ShapeChoice2 {
        public TaggedUnionArray {
            arrayPath = List.copyOf(arrayPath);
            tagPath = List.copyOf(tagPath);
            payloadPath = List.copyOf(payloadPath);
            cases = List.copyOf(cases);
        }

        @Override
        public List<String> path() {
            return arrayPath;
        }

        @Override
        public List<ShapeChoice2> children() {
            return List.of();
        }
    }

    record OverlayUnion(
            List<String> path,
            Class<?> unionType,
            Class<? extends Record> tagVariantType,
            List<String> tagPath,
            List<TaggedUnionCase2> cases) implements ShapeChoice2 {
        public OverlayUnion {
            path = List.copyOf(path);
            tagPath = List.copyOf(tagPath);
            cases = List.copyOf(cases);
        }

        @Override
        public List<ShapeChoice2> children() {
            return List.of();
        }
    }
}
