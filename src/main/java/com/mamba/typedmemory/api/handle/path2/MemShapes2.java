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

import com.mamba.typedmemory.api.handle.path.TagValue;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Experimental no-string shape builder. Accessor lambdas are inspected through
 * the Class-File API and reduced to record-component paths.
 */
public final class MemShapes2 {
    private MemShapes2() {
    }

    public static <R extends Record> Builder<R, R> of(Class<R> rootType) {
        return new Builder<>(rootType, rootType);
    }

    public static <R> RootUnionBuilder<R> union(Class<R> unionType) {
        return new RootUnionBuilder<>(unionType);
    }

    public static final class Builder<R, T extends Record> {
        private final Class<R> rootType;
        private final Class<T> currentType;
        private final List<ShapeChoice2> choices = new ArrayList<>();

        private Builder(Class<R> rootType, Class<T> currentType) {
            this.rootType = Objects.requireNonNull(rootType);
            this.currentType = Objects.requireNonNull(currentType);
        }

        public <F extends Record> Builder<R, T> field(
                Accessor<T, F> accessor,
                Class<F> fieldType) {
            return field(accessor, fieldType, ignored -> {
            });
        }

        public <F extends Record> Builder<R, T> field(
                Accessor<T, F> accessor,
                Class<F> fieldType,
                Consumer<Builder<R, F>> configure) {
            Objects.requireNonNull(configure);
            var path = resolvePath(currentType, CapturedAccessor.capture(accessor), fieldType);
            var child = new Builder<R, F>(rootType, fieldType);
            configure.accept(child);
            child.validateComplete();
            choices.add(new ShapeChoice2.RecordField(path, fieldType, child.choices));
            return this;
        }

        public <F extends Record> Builder<R, T> array(
                Accessor<T, F[]> accessor,
                Class<F> elementType) {
            return array(accessor, elementType, ignored -> {
            });
        }

        public <F extends Record> Builder<R, T> array(
                Accessor<T, F[]> accessor,
                Class<F> elementType,
                Consumer<Builder<R, F>> configure) {
            Objects.requireNonNull(configure);
            var path = resolveArrayPath(currentType, CapturedAccessor.capture(accessor), elementType);
            var child = new Builder<R, F>(rootType, elementType);
            configure.accept(child);
            child.validateComplete();
            choices.add(new ShapeChoice2.RecordArray(path, elementType, child.choices));
            return this;
        }

        public <U> Builder<R, T> union(
                Accessor<T, U> accessor,
                Class<U> unionType,
                Consumer<UnionFieldBuilder<R, U>> configure) {
            Objects.requireNonNull(configure);
            requireUnion(unionType);
            var path = resolvePath(currentType, CapturedAccessor.capture(accessor), unionType);
            var union = new UnionFieldBuilder<R, U>(rootType, unionType, path);
            configure.accept(union);
            choices.addAll(union.build());
            return this;
        }

        public <U> Builder<R, T> unionArray(
                Accessor<T, U[]> accessor,
                Class<U> unionType,
                Consumer<UnionArrayBuilder<R, U>> configure) {
            Objects.requireNonNull(configure);
            requireUnion(unionType);
            var path = resolveArrayPath(currentType, CapturedAccessor.capture(accessor), unionType);
            var union = new UnionArrayBuilder<R, U>(rootType, unionType, path);
            configure.accept(union);
            choices.addAll(union.build());
            return this;
        }

        public <U> Builder<R, T> taggedUnion(
                Accessor<T, ?> tagAccessor,
                Accessor<T, U> payloadAccessor,
                Class<U> unionType,
                Consumer<TaggedUnionBuilder<R, U>> configure) {
            Objects.requireNonNull(configure);
            requireUnion(unionType);
            var tag = resolve(currentType, CapturedAccessor.capture(tagAccessor, 0));
            var payload = resolve(currentType, CapturedAccessor.capture(payloadAccessor, 1));
            requireTagType(tag.leafType());
            requireLeaf(payload, unionType);

            var cases = new TaggedUnionBuilder<R, U>(rootType, unionType, tag.leafType());
            configure.accept(cases);
            choices.add(new ShapeChoice2.TaggedUnion(
                    tag.fields(),
                    payload.fields(),
                    unionType,
                    cases.build()));
            return this;
        }

        public <E extends Record, U> Builder<R, T> taggedUnionArray(
                Accessor<T, E[]> arrayAccessor,
                Class<E> elementType,
                Accessor<E, ?> tagAccessor,
                Accessor<E, U> payloadAccessor,
                Class<U> unionType,
                Consumer<TaggedUnionBuilder<R, U>> configure) {
            Objects.requireNonNull(configure);
            requireUnion(unionType);
            var arrayPath = resolveArrayPath(currentType, CapturedAccessor.capture(arrayAccessor, 0), elementType);
            var tag = resolve(elementType, CapturedAccessor.capture(tagAccessor, 0));
            var payload = resolve(elementType, CapturedAccessor.capture(payloadAccessor, 1));
            requireTagType(tag.leafType());
            requireLeaf(payload, unionType);

            var cases = new TaggedUnionBuilder<R, U>(rootType, unionType, tag.leafType());
            configure.accept(cases);
            choices.add(new ShapeChoice2.TaggedUnionArray(
                    arrayPath,
                    elementType,
                    tag.fields(),
                    payload.fields(),
                    unionType,
                    cases.build()));
            return this;
        }

        public MemShape2<R> shape() {
            validateComplete();
            return new MemShape2<>(rootType, currentType, choices);
        }

        private void validateComplete() {
            MemShapes2.validateComplete(currentType, choices, currentType.getSimpleName());
        }
    }

    public static final class RootUnionBuilder<R> {
        private final Class<R> unionType;
        private ShapeChoice2.UnionField variant;

        private RootUnionBuilder(Class<R> unionType) {
            this.unionType = Objects.requireNonNull(unionType);
            requireUnion(unionType);
        }

        public <V extends Record> RootUnionBuilder<R> variant(Class<V> variantType) {
            return variant(variantType, ignored -> {
            });
        }

        public <V extends Record> RootUnionBuilder<R> variant(
                Class<V> variantType,
                Consumer<Builder<R, V>> configure) {
            Objects.requireNonNull(configure);
            requireVariant(unionType, variantType);
            var child = new Builder<R, V>(unionType, variantType);
            configure.accept(child);
            child.validateComplete();
            variant = new ShapeChoice2.UnionField(List.of(), unionType, variantType, child.choices);
            return this;
        }

        public MemShape2<R> shape() {
            if (variant == null)
                throw new IllegalArgumentException("Root union shape needs a variant");
            return new MemShape2<>(unionType, variant.variantType(), variant.children());
        }
    }

    public static final class UnionFieldBuilder<R, U> {
        private final Class<R> rootType;
        private final Class<U> unionType;
        private final List<String> path;
        private final List<ShapeChoice2> choices = new ArrayList<>();

        private UnionFieldBuilder(Class<R> rootType, Class<U> unionType, List<String> path) {
            this.rootType = rootType;
            this.unionType = unionType;
            this.path = List.copyOf(path);
        }

        public <V extends Record> UnionFieldBuilder<R, U> variant(Class<V> variantType) {
            return variant(variantType, ignored -> {
            });
        }

        public <V extends Record> UnionFieldBuilder<R, U> variant(
                Class<V> variantType,
                Consumer<Builder<R, V>> configure) {
            Objects.requireNonNull(configure);
            requireVariant(unionType, variantType);
            var child = new Builder<R, V>(rootType, variantType);
            configure.accept(child);
            child.validateComplete();
            choices.add(new ShapeChoice2.UnionField(path, unionType, variantType, child.choices));
            return this;
        }

        public OverlayBuilder<R, U> overlay() {
            return new OverlayBuilder<>(rootType, unionType, path, choices);
        }

        private List<ShapeChoice2> build() {
            if (choices.isEmpty())
                throw new IllegalArgumentException("Union " + unionType.getName() + " needs a variant or overlay");
            return List.copyOf(choices);
        }
    }

    public static final class UnionArrayBuilder<R, U> {
        private final Class<R> rootType;
        private final Class<U> unionType;
        private final List<String> path;
        private final List<ShapeChoice2> choices = new ArrayList<>();

        private UnionArrayBuilder(Class<R> rootType, Class<U> unionType, List<String> path) {
            this.rootType = rootType;
            this.unionType = unionType;
            this.path = List.copyOf(path);
        }

        public <V extends Record> UnionArrayBuilder<R, U> variant(Class<V> variantType) {
            return variant(variantType, ignored -> {
            });
        }

        public <V extends Record> UnionArrayBuilder<R, U> variant(
                Class<V> variantType,
                Consumer<Builder<R, V>> configure) {
            Objects.requireNonNull(configure);
            requireVariant(unionType, variantType);
            var child = new Builder<R, V>(rootType, variantType);
            configure.accept(child);
            child.validateComplete();
            choices.add(new ShapeChoice2.UnionArray(path, unionType, variantType, child.choices));
            return this;
        }

        private List<ShapeChoice2> build() {
            if (choices.isEmpty())
                throw new IllegalArgumentException("Union array " + unionType.getName() + " needs a variant");
            return List.copyOf(choices);
        }
    }

    public static final class OverlayBuilder<R, U> {
        private final Class<R> rootType;
        private final Class<U> unionType;
        private final List<String> path;
        private final List<ShapeChoice2> choices;
        private Class<? extends Record> tagVariantType;
        private List<String> tagPath;
        private Class<?> tagType;

        private OverlayBuilder(
                Class<R> rootType,
                Class<U> unionType,
                List<String> path,
                List<ShapeChoice2> choices) {
            this.rootType = rootType;
            this.unionType = unionType;
            this.path = path;
            this.choices = choices;
        }

        public <V extends Record> OverlayCasesBuilder<R, U> tagFrom(
                Class<V> tagVariantType,
                Accessor<V, ?> tagAccessor) {
            requireVariant(unionType, tagVariantType);
            var tag = resolve(tagVariantType, CapturedAccessor.capture(tagAccessor));
            requireTagType(tag.leafType());
            this.tagVariantType = tagVariantType;
            this.tagPath = tag.fields();
            this.tagType = tag.leafType();
            return new OverlayCasesBuilder<>(rootType, unionType, path, choices, this);
        }
    }

    public static final class OverlayCasesBuilder<R, U> {
        private final Class<R> rootType;
        private final Class<U> unionType;
        private final List<String> path;
        private final List<ShapeChoice2> choices;
        private final OverlayBuilder<R, U> overlay;
        private final List<TaggedUnionCase2> cases = new ArrayList<>();

        private OverlayCasesBuilder(
                Class<R> rootType,
                Class<U> unionType,
                List<String> path,
                List<ShapeChoice2> choices,
                OverlayBuilder<R, U> overlay) {
            this.rootType = rootType;
            this.unionType = unionType;
            this.path = path;
            this.choices = choices;
            this.overlay = overlay;
        }

        public OverlayCasesBuilder<R, U> caseOf(int tag, Class<? extends Record> variantType) {
            return caseOf(TagValue.of(tag), variantType, ignored -> {
            });
        }

        public OverlayCasesBuilder<R, U> caseOf(long tag, Class<? extends Record> variantType) {
            return caseOf(TagValue.of(tag), variantType, ignored -> {
            });
        }

        public OverlayCasesBuilder<R, U> caseOf(boolean tag, Class<? extends Record> variantType) {
            return caseOf(TagValue.of(tag), variantType, ignored -> {
            });
        }

        public <V extends Record> OverlayCasesBuilder<R, U> caseOf(
                int tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> configure) {
            return caseOf(TagValue.of(tag), variantType, configure);
        }

        public <V extends Record> OverlayCasesBuilder<R, U> caseOf(
                TagValue tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> configure) {
            Objects.requireNonNull(configure);
            requireVariant(unionType, variantType);
            requireMatchingTag(overlay.tagType, tag);
            var child = new Builder<R, V>(rootType, variantType);
            configure.accept(child);
            child.validateComplete();
            cases.add(new TaggedUnionCase2(tag, variantType, child.choices));
            choices.removeIf(ShapeChoice2.OverlayUnion.class::isInstance);
            choices.add(new ShapeChoice2.OverlayUnion(
                    path,
                    unionType,
                    overlay.tagVariantType,
                    overlay.tagPath,
                    cases));
            return this;
        }
    }

    public static final class TaggedUnionBuilder<R, U> {
        private final Class<R> rootType;
        private final Class<U> unionType;
        private final Class<?> tagType;
        private final List<TaggedUnionCase2> cases = new ArrayList<>();

        private TaggedUnionBuilder(Class<R> rootType, Class<U> unionType, Class<?> tagType) {
            this.rootType = rootType;
            this.unionType = unionType;
            this.tagType = tagType;
        }

        public TaggedUnionBuilder<R, U> caseOf(int tag, Class<? extends Record> variantType) {
            return caseOf(TagValue.of(tag), variantType, ignored -> {
            });
        }

        public TaggedUnionBuilder<R, U> caseOf(long tag, Class<? extends Record> variantType) {
            return caseOf(TagValue.of(tag), variantType, ignored -> {
            });
        }

        public TaggedUnionBuilder<R, U> caseOf(boolean tag, Class<? extends Record> variantType) {
            return caseOf(TagValue.of(tag), variantType, ignored -> {
            });
        }

        public <V extends Record> TaggedUnionBuilder<R, U> caseOf(
                int tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> configure) {
            return caseOf(TagValue.of(tag), variantType, configure);
        }

        public <V extends Record> TaggedUnionBuilder<R, U> caseOf(
                TagValue tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> configure) {
            Objects.requireNonNull(configure);
            requireVariant(unionType, variantType);
            requireMatchingTag(tagType, tag);
            var child = new Builder<R, V>(rootType, variantType);
            configure.accept(child);
            child.validateComplete();
            cases.add(new TaggedUnionCase2(tag, variantType, child.choices));
            return this;
        }

        private List<TaggedUnionCase2> build() {
            if (cases.isEmpty())
                throw new IllegalArgumentException("Tagged union " + unionType.getName() + " needs cases");
            return List.copyOf(cases);
        }
    }

    private static ResolvedAccessor resolve(Class<?> rootType, CapturedAccessor<?, ?> captured) {
        return LambdaAccessorResolver.resolve(rootType, captured);
    }

    private static List<String> resolvePath(
            Class<?> rootType,
            CapturedAccessor<?, ?> captured,
            Class<?> expectedLeaf) {
        var resolved = LambdaAccessorResolver.resolve(rootType, captured, expectedLeaf);
        requireLeaf(resolved, expectedLeaf);
        return resolved.fields();
    }

    private static List<String> resolveArrayPath(
            Class<?> rootType,
            CapturedAccessor<?, ?> captured,
            Class<?> expectedElement) {
        var arrayType = Array.newInstance(expectedElement, 0).getClass();
        var resolved = LambdaAccessorResolver.resolve(rootType, captured, arrayType);
        var leaf = resolved.leafType();
        if (!leaf.isArray() || leaf.componentType() != expectedElement) {
            throw new IllegalArgumentException(
                    "Accessor ends at %s, not %s[]".formatted(
                            leaf.getTypeName(),
                            expectedElement.getTypeName()));
        }
        return resolved.fields();
    }

    private static void requireLeaf(ResolvedAccessor resolved, Class<?> expectedLeaf) {
        if (resolved.leafType() != expectedLeaf) {
            throw new IllegalArgumentException(
                    "Accessor ends at %s, not %s".formatted(
                            resolved.leafType().getTypeName(),
                            expectedLeaf.getTypeName()));
        }
    }

    private static void requireUnion(Class<?> unionType) {
        if (!unionType.isInterface() && !Modifier.isAbstract(unionType.getModifiers())) {
            throw new IllegalArgumentException("Union type must be an interface or abstract type: "
                    + unionType.getName());
        }
    }

    private static void requireVariant(Class<?> unionType, Class<? extends Record> variantType) {
        if (!variantType.isRecord()) {
            throw new IllegalArgumentException("Variant must be a record: " + variantType.getName());
        }
        if (!unionType.isAssignableFrom(variantType)) {
            throw new IllegalArgumentException(
                    "%s is not a variant of %s".formatted(variantType.getName(), unionType.getName()));
        }
    }

    private static void requireTagType(Class<?> type) {
        if (!(type == byte.class
                || type == short.class
                || type == int.class
                || type == long.class
                || type == boolean.class
                || type == char.class)) {
            throw new IllegalArgumentException("Unsupported tag type: " + type.getTypeName());
        }
    }

    private static void requireMatchingTag(Class<?> tagType, TagValue tag) {
        if (tag.type() != tagType) {
            throw new IllegalArgumentException(
                    "Tag value type %s does not match tag field type %s".formatted(
                            tag.type().getTypeName(),
                            tagType.getTypeName()));
        }
    }

    private static void validateComplete(
            Class<? extends Record> recordType,
            List<ShapeChoice2> choices,
            String displayPath) {
        for (var component : recordType.getRecordComponents()) {
            var type = component.getType();
            var componentPath = displayPath + "." + component.getName();
            var componentChoices = choicesStartingWith(choices, component.getName());

            if (isUnion(type)) {
                if (!hasUnionChoice(componentChoices)) {
                    throw new IllegalArgumentException("Incomplete shape: " + componentPath
                            + " is a union but no variant/tag/overlay was selected");
                }
                validateUnionChoices(componentChoices);
                continue;
            }

            if (type.isArray()) {
                var elementType = type.componentType();
                if (isUnion(elementType) && !hasUnionArrayChoice(componentChoices)) {
                    throw new IllegalArgumentException("Incomplete shape: " + componentPath
                            + " is a union array but no variant/tag array rule was selected");
                }
                validateUnionArrayChoices(componentChoices);
                if (hasTaggedUnionArrayChoice(componentChoices))
                    continue;
                if (elementType.isRecord()) {
                    validateRecordArrayChoices(elementType.asSubclass(Record.class), componentChoices, componentPath);
                }
                continue;
            }

            if (type.isRecord()) {
                validateRecordChoices(type.asSubclass(Record.class), componentChoices, componentPath);
            }
        }
    }

    private static void validateRecordChoices(
            Class<? extends Record> recordType,
            List<ShapeChoice2> choices,
            String displayPath) {
        var explicitRecord = choices.stream()
                .filter(ShapeChoice2.RecordField.class::isInstance)
                .map(ShapeChoice2.RecordField.class::cast)
                .filter(choice -> choice.path().size() == 1)
                .findFirst();
        if (explicitRecord.isPresent()) {
            validateComplete(explicitRecord.get().recordType(), explicitRecord.get().children(), displayPath);
        } else {
            validateComplete(recordType, List.of(), displayPath);
        }
    }

    private static void validateRecordArrayChoices(
            Class<? extends Record> elementType,
            List<ShapeChoice2> choices,
            String displayPath) {
        var explicitArray = choices.stream()
                .filter(ShapeChoice2.RecordArray.class::isInstance)
                .map(ShapeChoice2.RecordArray.class::cast)
                .filter(choice -> choice.path().size() == 1)
                .findFirst();
        if (explicitArray.isPresent()) {
            validateComplete(explicitArray.get().elementType(), explicitArray.get().children(), displayPath + "[]");
        } else {
            validateComplete(elementType, List.of(), displayPath + "[]");
        }
    }

    private static void validateUnionChoices(List<ShapeChoice2> choices) {
        for (var choice : choices) {
            if (choice instanceof ShapeChoice2.UnionField union) {
                validateComplete(union.variantType(), union.children(), union.variantType().getSimpleName());
            } else if (choice instanceof ShapeChoice2.TaggedUnion tagged) {
                for (var taggedCase : tagged.cases()) {
                    validateComplete(
                            taggedCase.variantType(),
                            taggedCase.children(),
                            taggedCase.variantType().getSimpleName());
                }
            } else if (choice instanceof ShapeChoice2.OverlayUnion overlay) {
                for (var taggedCase : overlay.cases()) {
                    validateComplete(
                            taggedCase.variantType(),
                            taggedCase.children(),
                            taggedCase.variantType().getSimpleName());
                }
            }
        }
    }

    private static void validateUnionArrayChoices(List<ShapeChoice2> choices) {
        for (var choice : choices) {
            if (choice instanceof ShapeChoice2.UnionArray union) {
                validateComplete(union.variantType(), union.children(), union.variantType().getSimpleName());
            } else if (choice instanceof ShapeChoice2.TaggedUnionArray tagged) {
                for (var taggedCase : tagged.cases()) {
                    validateComplete(
                            taggedCase.variantType(),
                            taggedCase.children(),
                            taggedCase.variantType().getSimpleName());
                }
            }
        }
    }

    private static List<ShapeChoice2> choicesStartingWith(List<ShapeChoice2> choices, String fieldName) {
        return choices.stream()
                .filter(choice -> !choice.path().isEmpty() && choice.path().getFirst().equals(fieldName))
                .toList();
    }

    private static boolean hasUnionChoice(List<ShapeChoice2> choices) {
        return choices.stream().anyMatch(choice -> choice instanceof ShapeChoice2.UnionField
                || choice instanceof ShapeChoice2.TaggedUnion
                || choice instanceof ShapeChoice2.OverlayUnion);
    }

    private static boolean hasUnionArrayChoice(List<ShapeChoice2> choices) {
        return choices.stream().anyMatch(choice -> choice instanceof ShapeChoice2.UnionArray
                || choice instanceof ShapeChoice2.TaggedUnionArray);
    }

    private static boolean hasTaggedUnionArrayChoice(List<ShapeChoice2> choices) {
        return choices.stream().anyMatch(ShapeChoice2.TaggedUnionArray.class::isInstance);
    }

    private static boolean isUnion(Class<?> type) {
        if (type.isPrimitive() || type.isArray() || type.isRecord())
            return false;
        return (type.isInterface() || Modifier.isAbstract(type.getModifiers()))
                && type != java.io.Serializable.class;
    }
}
