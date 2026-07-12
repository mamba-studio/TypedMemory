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

import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Builders for concrete union shapes.
 */
public final class MemShapes {
    private MemShapes() {
    }

    /**
     * Starts a shape at a root record type.
     *
     * @param <R> the root record type
     * @param root the root record class
     * @return a shape builder
     */
    public static <R extends Record> Builder<R, R> of(Class<R> root) {
        Objects.requireNonNull(root);
        requireRecord(root, "Root type");
        return new Builder<>(root, root);
    }

    /**
     * Starts a shape at a root union type.
     *
     * @param <U> the declared root union type
     * @param unionType the root union class
     * @return a union shape builder
     */
    public static <U> RootUnionBuilder<U> union(Class<U> unionType) {
        Objects.requireNonNull(unionType);
        requireUnion(unionType, MemShapes.class, "root");
        return new RootUnionBuilder<>(unionType);
    }

    /**
     * Builder positioned at a record type whose union children can be
     * concretized.
     *
     * @param <R> the declared root type
     * @param <C> the current record type
     */
    public static final class Builder<R, C extends Record> {
        private final Class<R> rootType;
        private final Class<C> currentType;
        private final LinkedHashMap<String, ChoiceBuilder> choices = new LinkedHashMap<>();

        private Builder(Class<R> rootType, Class<C> currentType) {
            this.rootType = Objects.requireNonNull(rootType);
            this.currentType = Objects.requireNonNull(currentType);
        }

        /**
         * Selects a union field and configures its concrete variant.
         *
         * @param <U> the declared union type
         * @param fieldName the union field name
         * @param unionType the expected declared union type
         * @param nested variant configuration
         * @return this builder
         */
        public <U> Builder<R, C> union(
                String fieldName,
                Class<U> unionType,
                Consumer<UnionBuilder<R, C, U>> nested) {
            Objects.requireNonNull(unionType);
            Objects.requireNonNull(nested);

            var component = component(currentType, fieldName);
            if (component.getType() != unionType) {
                throw new IllegalArgumentException(
                        "%s.%s is %s, not %s".formatted(
                                currentType.getSimpleName(),
                                fieldName,
                                component.getType().getSimpleName(),
                                unionType.getSimpleName()));
            }
            requireUnion(unionType, currentType, fieldName);

            nested.accept(new UnionBuilder<>(this, fieldName, unionType));
            return this;
        }

        /**
         * Selects a union field whose sibling tag field chooses the concrete
         * payload variant.
         *
         * @param <U> the declared union payload type
         * @param tagFieldName the tag field in the current record
         * @param payloadFieldName the union payload field in the current record
         * @param unionType the expected declared payload union type
         * @param nested case configuration
         * @return this builder
         */
        public <U> Builder<R, C> taggedUnion(
                String tagFieldName,
                String payloadFieldName,
                Class<U> unionType,
                Consumer<TaggedUnionBuilder<R, C, U>> nested) {
            var tagType = component(currentType, tagFieldName).getType();
            return taggedUnion(tagFieldName, tagType, payloadFieldName, unionType, nested);
        }

        /**
         * Selects a union field whose sibling tag field chooses the concrete
         * payload variant.
         *
         * @param <U> the declared union payload type
         * @param tagFieldName the tag field in the current record
         * @param tagType the expected primitive tag carrier type
         * @param payloadFieldName the union payload field in the current record
         * @param unionType the expected declared payload union type
         * @param nested case configuration
         * @return this builder
         */
        public <U> Builder<R, C> taggedUnion(
                String tagFieldName,
                Class<?> tagType,
                String payloadFieldName,
                Class<U> unionType,
                Consumer<TaggedUnionBuilder<R, C, U>> nested) {
            return taggedUnion(tagFieldName, tagType, tagType, payloadFieldName, unionType, nested);
        }

        public <T extends Record, U> Builder<R, C> taggedUnion(
                TagAdapter<T> tagAdapter,
                String payloadFieldName,
                Class<U> unionType,
                Consumer<TaggedUnionBuilder<R, C, U>> nested) {
            Objects.requireNonNull(tagAdapter);
            return taggedUnion(
                    tagAdapter.fieldName(),
                    tagAdapter.nativeType(),
                    tagAdapter.tagType(),
                    payloadFieldName,
                    unionType,
                    nested);
        }

        private <U> Builder<R, C> taggedUnion(
                String tagFieldName,
                Class<?> tagType,
                Class<?> semanticTagType,
                String payloadFieldName,
                Class<U> unionType,
                Consumer<TaggedUnionBuilder<R, C, U>> nested) {
            Objects.requireNonNull(tagType);
            Objects.requireNonNull(semanticTagType);
            Objects.requireNonNull(unionType);
            Objects.requireNonNull(nested);

            var tagComponent = component(currentType, tagFieldName);
            if (!TagValue.isNativeSupported(tagType)) {
                throw new IllegalArgumentException(
                        "Unsupported tag type %s for %s.%s".formatted(
                                tagType.getSimpleName(),
                                currentType.getSimpleName(),
                                tagFieldName));
            }
            if (!TagValue.isSupported(semanticTagType)) {
                throw new IllegalArgumentException(
                        "Unsupported semantic tag type %s for %s.%s".formatted(
                                semanticTagType.getSimpleName(),
                                currentType.getSimpleName(),
                                tagFieldName));
            }
            if (tagComponent.getType() != tagType) {
                throw new IllegalArgumentException(
                        "%s.%s is %s, not %s".formatted(
                                currentType.getSimpleName(),
                                tagFieldName,
                                tagComponent.getType().getSimpleName(),
                                tagType.getSimpleName()));
            }

            var payloadComponent = component(currentType, payloadFieldName);
            if (payloadComponent.getType() != unionType) {
                throw new IllegalArgumentException(
                        "%s.%s is %s, not %s".formatted(
                                currentType.getSimpleName(),
                                payloadFieldName,
                                payloadComponent.getType().getSimpleName(),
                                unionType.getSimpleName()));
            }
            requireUnion(unionType, currentType, payloadFieldName);

            var caseBuilder = new TaggedUnionBuilder<R, C, U>(
                    this, tagFieldName, tagType, semanticTagType, payloadFieldName, unionType);
            nested.accept(caseBuilder);
            putTaggedUnion(caseBuilder);
            return this;
        }

        private <V extends Record> Builder<R, C> selectVariant(
                String fieldName,
                Class<?> unionType,
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            putVariant(fieldName, unionType, variantType, nested);
            return this;
        }

        /**
         * Descends into an always-present record field.
         *
         * @param <N> the nested record type
         * @param fieldName the record field name
         * @param fieldType the expected record field type
         * @param nested nested shape configuration
         * @return this builder
         */
        public <N extends Record> Builder<R, C> field(
                String fieldName,
                Class<N> fieldType,
                Consumer<Builder<R, N>> nested) {
            Objects.requireNonNull(fieldType);
            Objects.requireNonNull(nested);
            requireRecord(fieldType, "Field type");

            var component = component(currentType, fieldName);
            if (component.getType() != fieldType) {
                throw new IllegalArgumentException(
                        "%s.%s is %s, not %s".formatted(
                                currentType.getSimpleName(),
                                fieldName,
                                component.getType().getSimpleName(),
                                fieldType.getSimpleName()));
            }

            var node = choices.get(fieldName);
            if (node == null) {
                node = new RecordChoiceBuilder(fieldName, fieldType);
                choices.put(fieldName, node);
            } else if (!(node instanceof RecordChoiceBuilder record)
                    || record.recordType != fieldType) {
                throw new IllegalArgumentException(
                        "Conflicting shape choices for %s.%s".formatted(
                                currentType.getSimpleName(), fieldName));
            }

            var nestedBuilder = new Builder<R, N>(rootType, fieldType);
            nested.accept(nestedBuilder);
            node.mergeChildren(nestedBuilder.choices);
            return this;
        }

        /**
         * Descends into every element of a record array field using one uniform
         * element shape.
         *
         * @param <N> the record element type
         * @param fieldName the array field name
         * @param elementType the expected record element type
         * @param nested nested shape configuration for each element
         * @return this builder
         */
        public <N extends Record> Builder<R, C> array(
                String fieldName,
                Class<N> elementType,
                Consumer<Builder<R, N>> nested) {
            Objects.requireNonNull(elementType);
            Objects.requireNonNull(nested);
            requireRecord(elementType, "Array element type");

            var component = component(currentType, fieldName);
            var fieldType = component.getType();
            if (!fieldType.isArray() || fieldType.getComponentType() != elementType) {
                throw new IllegalArgumentException(
                        "%s.%s is %s, not %s[]".formatted(
                                currentType.getSimpleName(),
                                fieldName,
                                fieldType.getSimpleName(),
                                elementType.getSimpleName()));
            }

            var node = choices.get(fieldName);
            if (node == null) {
                node = new ArrayChoiceBuilder(fieldName, elementType);
                choices.put(fieldName, node);
            } else if (!(node instanceof ArrayChoiceBuilder array)
                    || array.elementType != elementType) {
                throw new IllegalArgumentException(
                        "Conflicting shape choices for %s.%s".formatted(
                                currentType.getSimpleName(), fieldName));
            }

            var nestedBuilder = new Builder<R, N>(rootType, elementType);
            nested.accept(nestedBuilder);
            node.mergeChildren(nestedBuilder.choices);
            return this;
        }

        /**
         * Selects an array field whose elements are unions and configures one
         * uniform concrete variant for all elements.
         *
         * @param <U> the declared union element type
         * @param fieldName the array field name
         * @param unionType the expected declared union element type
         * @param nested variant configuration
         * @return this builder
         */
        public <U> Builder<R, C> unionArray(
                String fieldName,
                Class<U> unionType,
                Consumer<UnionArrayBuilder<R, C, U>> nested) {
            Objects.requireNonNull(unionType);
            Objects.requireNonNull(nested);

            var component = component(currentType, fieldName);
            var fieldType = component.getType();
            if (!fieldType.isArray() || fieldType.getComponentType() != unionType) {
                throw new IllegalArgumentException(
                        "%s.%s is %s, not %s[]".formatted(
                                currentType.getSimpleName(),
                                fieldName,
                                fieldType.getSimpleName(),
                                unionType.getSimpleName()));
            }
            requireUnion(unionType, currentType, fieldName);

            nested.accept(new UnionArrayBuilder<>(this, fieldName, unionType));
            return this;
        }

        /**
         * Selects an array field whose elements are records shaped like
         * {@code tag + union payload}, and maps tag values to concrete payload
         * variants.
         *
         * @param <N> the array element record type
         * @param <U> the declared union payload type
         * @param fieldName the array field name
         * @param elementType the expected array element type
         * @param tagFieldName the tag field inside each element
         * @param payloadFieldName the union payload field inside each element
         * @param unionType the expected declared payload union type
         * @param nested case configuration
         * @return this builder
         */
        public <N extends Record, U> Builder<R, C> taggedUnionArray(
                String fieldName,
                Class<N> elementType,
                String tagFieldName,
                String payloadFieldName,
                Class<U> unionType,
                Consumer<TaggedUnionArrayBuilder<R, C, N, U>> nested) {
            var tagType = component(elementType, tagFieldName).getType();
            return taggedUnionArray(
                    fieldName,
                    elementType,
                    tagFieldName,
                    tagType,
                    payloadFieldName,
                    unionType,
                    nested);
        }

        /**
         * Selects an array field whose elements are records shaped like
         * {@code tag + union payload}, and maps tag values to concrete payload
         * variants.
         *
         * @param <N> the array element record type
         * @param <U> the declared union payload type
         * @param fieldName the array field name
         * @param elementType the expected array element type
         * @param tagFieldName the tag field inside each element
         * @param tagType the expected primitive tag carrier type
         * @param payloadFieldName the union payload field inside each element
         * @param unionType the expected declared payload union type
         * @param nested case configuration
         * @return this builder
         */
        public <N extends Record, U> Builder<R, C> taggedUnionArray(
                String fieldName,
                Class<N> elementType,
                String tagFieldName,
                Class<?> tagType,
                String payloadFieldName,
                Class<U> unionType,
                Consumer<TaggedUnionArrayBuilder<R, C, N, U>> nested) {
            return taggedUnionArray(fieldName, elementType, tagFieldName, tagType, tagType, payloadFieldName, unionType, nested);
        }

        public <N extends Record, T extends Record, U> Builder<R, C> taggedUnionArray(
                String fieldName,
                Class<N> elementType,
                TagAdapter<T> tagAdapter,
                String payloadFieldName,
                Class<U> unionType,
                Consumer<TaggedUnionArrayBuilder<R, C, N, U>> nested) {
            Objects.requireNonNull(tagAdapter);
            return taggedUnionArray(
                    fieldName,
                    elementType,
                    tagAdapter.fieldName(),
                    tagAdapter.nativeType(),
                    tagAdapter.tagType(),
                    payloadFieldName,
                    unionType,
                    nested);
        }

        private <N extends Record, U> Builder<R, C> taggedUnionArray(
                String fieldName,
                Class<N> elementType,
                String tagFieldName,
                Class<?> tagType,
                Class<?> semanticTagType,
                String payloadFieldName,
                Class<U> unionType,
                Consumer<TaggedUnionArrayBuilder<R, C, N, U>> nested) {
            Objects.requireNonNull(elementType);
            Objects.requireNonNull(tagType);
            Objects.requireNonNull(semanticTagType);
            Objects.requireNonNull(unionType);
            Objects.requireNonNull(nested);
            requireRecord(elementType, "Array element type");

            var component = component(currentType, fieldName);
            var fieldType = component.getType();
            if (!fieldType.isArray() || fieldType.getComponentType() != elementType) {
                throw new IllegalArgumentException(
                        "%s.%s is %s, not %s[]".formatted(
                                currentType.getSimpleName(),
                                fieldName,
                                fieldType.getSimpleName(),
                                elementType.getSimpleName()));
            }

            var tagComponent = component(elementType, tagFieldName);
            if (!TagValue.isNativeSupported(tagType)) {
                throw new IllegalArgumentException(
                        "Unsupported tag type %s for %s.%s".formatted(
                                tagType.getSimpleName(),
                                elementType.getSimpleName(),
                                tagFieldName));
            }
            if (!TagValue.isSupported(semanticTagType)) {
                throw new IllegalArgumentException(
                        "Unsupported semantic tag type %s for %s.%s".formatted(
                                semanticTagType.getSimpleName(),
                                elementType.getSimpleName(),
                                tagFieldName));
            }
            if (tagComponent.getType() != tagType) {
                throw new IllegalArgumentException(
                        "%s.%s is %s, not %s".formatted(
                                elementType.getSimpleName(),
                                tagFieldName,
                                tagComponent.getType().getSimpleName(),
                                tagType.getSimpleName()));
            }

            var payloadComponent = component(elementType, payloadFieldName);
            if (payloadComponent.getType() != unionType) {
                throw new IllegalArgumentException(
                        "%s.%s is %s, not %s".formatted(
                                elementType.getSimpleName(),
                                payloadFieldName,
                                payloadComponent.getType().getSimpleName(),
                                unionType.getSimpleName()));
            }
            requireUnion(unionType, elementType, payloadFieldName);

            var caseBuilder = new TaggedUnionArrayBuilder<R, C, N, U>(
                    this, fieldName, elementType, tagFieldName, tagType, semanticTagType, payloadFieldName, unionType);
            nested.accept(caseBuilder);
            putTaggedUnionArray(caseBuilder);
            return this;
        }

        /**
         * Finishes this builder as an immutable shape.
         *
         * @return a concrete shape
         */
        public MemShape<R> shape() {
            validateComplete(currentType, choices);
            return new MemShapeImpl<>(rootType, currentType, buildChoices(choices));
        }

        private static List<ShapeChoice> buildChoices(Map<String, ChoiceBuilder> choices) {
            var out = new ArrayList<ShapeChoice>(choices.size());
            var ordered = new ArrayList<>(choices.values());
            ordered.sort(Comparator.comparing(choice -> choice.fieldName));
            for (var choice : ordered)
                out.add(choice.build());
            return List.copyOf(out);
        }

        private <V extends Record> void putVariant(
                String fieldName,
                Class<?> unionType,
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            Objects.requireNonNull(variantType);
            requireRecord(variantType, "Variant type");
            if (!unionType.isAssignableFrom(variantType)) {
                throw new IllegalArgumentException(
                        "%s does not implement/extend %s for %s.%s".formatted(
                                variantType.getSimpleName(),
                                unionType.getSimpleName(),
                                currentType.getSimpleName(),
                                fieldName));
            }

            var choice = choices.get(fieldName);
            if (choice == null) {
                choice = new UnionChoiceBuilder(fieldName, unionType, variantType);
                choices.put(fieldName, choice);
            } else if (!(choice instanceof UnionChoiceBuilder union)
                    || union.variantType != variantType) {
                throw new IllegalArgumentException(
                        "Conflicting variants for %s.%s: %s and %s".formatted(
                                currentType.getSimpleName(),
                                fieldName,
                                choice instanceof UnionChoiceBuilder existing
                                        ? existing.variantType.getSimpleName()
                                        : choice.getClass().getSimpleName(),
                                variantType.getSimpleName()));
            }

            if (nested != null) {
                var nestedBuilder = new Builder<R, V>(rootType, variantType);
                nested.accept(nestedBuilder);
                choice.mergeChildren(nestedBuilder.choices);
            }
        }

        private void putOverlayUnion(OverlayUnionBuilder<R, C, ?> builder) {
            if (builder.tagVariantType == null) {
                throw new IllegalStateException(
                        "Overlay union " + builder.fieldName + " has no tagFrom(...) variant");
            }

            var choice = choices.get(builder.fieldName);
            if (choice == null) {
                choices.put(builder.fieldName, new OverlayUnionChoiceBuilder(
                        builder.fieldName,
                        builder.unionType,
                        builder.tagVariantType,
                        builder.tagFieldName,
                        builder.tagType,
                        builder.semanticTagType,
                        builder.cases));
                return;
            }

            if (!(choice instanceof OverlayUnionChoiceBuilder overlay)
                    || !overlay.sameDefinition(builder)) {
                throw new IllegalArgumentException(
                        "Conflicting shape choices for %s.%s".formatted(
                                currentType.getSimpleName(), builder.fieldName));
            }
            overlay.mergeCases(builder.cases);
        }

        private <V extends Record> void putUnionArrayVariant(
                String fieldName,
                Class<?> unionType,
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            Objects.requireNonNull(variantType);
            requireRecord(variantType, "Variant type");
            if (!unionType.isAssignableFrom(variantType)) {
                throw new IllegalArgumentException(
                        "%s does not implement/extend %s for %s.%s[]".formatted(
                                variantType.getSimpleName(),
                                unionType.getSimpleName(),
                                currentType.getSimpleName(),
                                fieldName));
            }

            var choice = choices.get(fieldName);
            if (choice == null) {
                choice = new UnionArrayChoiceBuilder(fieldName, unionType, variantType);
                choices.put(fieldName, choice);
            } else if (!(choice instanceof UnionArrayChoiceBuilder array)
                    || array.variantType != variantType) {
                throw new IllegalArgumentException(
                        "Conflicting array variants for %s.%s: %s and %s".formatted(
                                currentType.getSimpleName(),
                                fieldName,
                                choice instanceof UnionArrayChoiceBuilder existing
                                        ? existing.variantType.getSimpleName()
                                        : choice.getClass().getSimpleName(),
                                variantType.getSimpleName()));
            }

            if (nested != null) {
                var nestedBuilder = new Builder<R, V>(rootType, variantType);
                nested.accept(nestedBuilder);
                choice.mergeChildren(nestedBuilder.choices);
            }
        }

        private void putTaggedUnion(TaggedUnionBuilder<R, C, ?> builder) {
            var choice = choices.get(builder.payloadFieldName);
            if (choice == null) {
                choices.put(builder.payloadFieldName, new TaggedUnionChoiceBuilder(
                        builder.payloadFieldName,
                        builder.tagFieldName,
                        builder.tagType,
                        builder.semanticTagType,
                        builder.unionType,
                        builder.cases));
                return;
            }

            if (!(choice instanceof TaggedUnionChoiceBuilder tagged)
                    || !tagged.sameDefinition(builder)) {
                throw new IllegalArgumentException(
                        "Conflicting shape choices for %s.%s".formatted(
                                currentType.getSimpleName(), builder.payloadFieldName));
            }
            tagged.mergeCases(builder.cases);
        }

        private void putTaggedUnionArray(TaggedUnionArrayBuilder<R, C, ?, ?> builder) {
            var choice = choices.get(builder.fieldName);
            if (choice == null) {
                choices.put(builder.fieldName, new TaggedUnionArrayChoiceBuilder(
                        builder.fieldName,
                        builder.elementType,
                        builder.tagFieldName,
                        builder.tagType,
                        builder.semanticTagType,
                        builder.payloadFieldName,
                        builder.unionType,
                        builder.cases));
                return;
            }

            if (!(choice instanceof TaggedUnionArrayChoiceBuilder tagged)
                    || !tagged.sameDefinition(builder)) {
                throw new IllegalArgumentException(
                        "Conflicting shape choices for %s.%s".formatted(
                                currentType.getSimpleName(), builder.fieldName));
            }
            tagged.mergeCases(builder.cases);
        }
    }

    /**
     * Builder for selecting the concrete variant of a root union.
     *
     * @param <U> the declared root union type
     */
    public static final class RootUnionBuilder<U> {
        private final Class<U> unionType;
        private Class<? extends Record> variantType;
        private LinkedHashMap<String, ChoiceBuilder> children;

        private RootUnionBuilder(Class<U> unionType) {
            this.unionType = unionType;
        }

        /**
         * Selects the concrete root record variant.
         *
         * @param <V> the variant record type
         * @param variantType the selected variant type
         * @return this builder
         */
        public <V extends Record> RootUnionBuilder<U> variant(Class<V> variantType) {
            return variant(variantType, null);
        }

        /**
         * Selects the concrete root record variant and configures nested choices
         * inside it.
         *
         * @param <V> the variant record type
         * @param variantType the selected variant type
         * @param nested nested shape configuration
         * @return this builder
         */
        public <V extends Record> RootUnionBuilder<U> variant(
                Class<V> variantType,
                Consumer<Builder<U, V>> nested) {
            Objects.requireNonNull(variantType);
            requireRecord(variantType, "Variant type");
            if (!unionType.isAssignableFrom(variantType)) {
                throw new IllegalArgumentException(
                        "%s does not implement/extend %s for root union".formatted(
                                variantType.getSimpleName(),
                                unionType.getSimpleName()));
            }
            if (this.variantType != null) {
                throw new IllegalStateException(
                        "Variant already selected for root union " + unionType.getSimpleName());
            }

            this.variantType = variantType;
            var nestedBuilder = new Builder<U, V>(unionType, variantType);
            if (nested != null)
                nested.accept(nestedBuilder);
            this.children = nestedBuilder.choices;
            return this;
        }

        /**
         * Finishes this builder as an immutable shape.
         *
         * @return a concrete shape
         */
        public MemShape<U> shape() {
            if (variantType == null) {
                throw new IllegalArgumentException(
                        "Incomplete shape: root union %s has no selected variant".formatted(
                                unionType.getSimpleName()));
            }
            validateComplete(variantType, children);
            return new MemShapeImpl<>(unionType, variantType, Builder.buildChoices(children));
        }
    }

    /**
     * Builder for selecting one concrete variant after an explicit union field.
     *
     * @param <R> the declared root type
     * @param <C> the current record type
     * @param <U> the declared union type
     */
    public static final class UnionBuilder<R, C extends Record, U> {
        private final Builder<R, C> parent;
        private final String fieldName;
        private final Class<U> unionType;
        private boolean selected;

        private UnionBuilder(Builder<R, C> parent, String fieldName, Class<U> unionType) {
            this.parent = parent;
            this.fieldName = fieldName;
            this.unionType = unionType;
        }

        /**
         * Selects the concrete record variant for this union field.
         *
         * @param <V> the variant record type
         * @param variantType the selected variant type
         */
        public <V extends Record> void variant(Class<V> variantType) {
            variant(variantType, null);
        }

        /**
         * Selects the concrete record variant for this union field and
         * configures nested choices inside it.
         *
         * @param <V> the variant record type
         * @param variantType the selected variant type
         * @param nested nested shape configuration
         */
        public <V extends Record> void variant(
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            if (selected)
                throw new IllegalStateException(
                        "Variant already selected for " + fieldName);
            selected = true;
            parent.selectVariant(fieldName, unionType, variantType, nested);
        }

        /**
         * Configures this union as an overlay where all variants share one
         * storage region.
         *
         * @return an overlay union builder
         */
        public OverlayUnionBuilder<R, C, U> overlay() {
            if (selected)
                throw new IllegalStateException(
                        "Variant already selected for " + fieldName);
            selected = true;
            return new OverlayUnionBuilder<>(parent, fieldName, unionType);
        }
    }

    /**
     * Builder for overlay-union cases where the tag is read through one variant
     * layout.
     *
     * @param <R> the declared root type
     * @param <C> the current record type
     * @param <U> the declared union type
     */
    public static final class OverlayUnionBuilder<R, C extends Record, U> {
        private final Builder<R, C> parent;
        private final String fieldName;
        private final Class<U> unionType;
        private Class<? extends Record> tagVariantType;
        private String tagFieldName;
        private Class<?> tagType;
        private Class<?> semanticTagType;
        private final LinkedHashMap<TagValue, TaggedCaseBuilder> cases = new LinkedHashMap<>();

        private OverlayUnionBuilder(Builder<R, C> parent, String fieldName, Class<U> unionType) {
            this.parent = parent;
            this.fieldName = fieldName;
            this.unionType = unionType;
        }

        public <T extends Record> OverlayUnionBuilder<R, C, U> tagFrom(
                Class<T> tagVariantType,
                String tagFieldName) {
            var tagType = component(tagVariantType, tagFieldName).getType();
            return tagFrom(tagVariantType, tagFieldName, tagType);
        }

        public <T extends Record> OverlayUnionBuilder<R, C, U> tagFrom(
                Class<T> tagVariantType,
                String tagFieldName,
                Class<?> tagType) {
            return tagFrom(tagVariantType, tagFieldName, tagType, tagType);
        }

        public <T extends Record, G extends Record> OverlayUnionBuilder<R, C, U> tagFrom(
                Class<T> tagVariantType,
                TagAdapter<G> tagAdapter) {
            Objects.requireNonNull(tagAdapter);
            return tagFrom(
                    tagVariantType,
                    tagAdapter.fieldName(),
                    tagAdapter.nativeType(),
                    tagAdapter.tagType());
        }

        private <T extends Record> OverlayUnionBuilder<R, C, U> tagFrom(
                Class<T> tagVariantType,
                String tagFieldName,
                Class<?> tagType,
                Class<?> semanticTagType) {
            Objects.requireNonNull(tagVariantType);
            Objects.requireNonNull(tagType);
            Objects.requireNonNull(semanticTagType);
            requireRecord(tagVariantType, "Tag variant type");
            if (!unionType.isAssignableFrom(tagVariantType)) {
                throw new IllegalArgumentException(
                        "%s does not implement/extend %s for %s.%s".formatted(
                                tagVariantType.getSimpleName(),
                                unionType.getSimpleName(),
                                parent.currentType.getSimpleName(),
                                fieldName));
            }

            var tagComponent = component(tagVariantType, tagFieldName);
            if (!TagValue.isNativeSupported(tagType)) {
                throw new IllegalArgumentException(
                        "Unsupported tag type %s for %s.%s".formatted(
                                tagType.getSimpleName(),
                                tagVariantType.getSimpleName(),
                                tagFieldName));
            }
            if (!TagValue.isSupported(semanticTagType)) {
                throw new IllegalArgumentException(
                        "Unsupported semantic tag type %s for %s.%s".formatted(
                                semanticTagType.getSimpleName(),
                                tagVariantType.getSimpleName(),
                                tagFieldName));
            }
            if (tagComponent.getType() != tagType) {
                throw new IllegalArgumentException(
                        "%s.%s is %s, not %s".formatted(
                                tagVariantType.getSimpleName(),
                                tagFieldName,
                                tagComponent.getType().getSimpleName(),
                                tagType.getSimpleName()));
            }

            if (this.tagVariantType != null
                    && (this.tagVariantType != tagVariantType
                    || !this.tagFieldName.equals(tagFieldName)
                    || this.tagType != tagType
                    || this.semanticTagType != semanticTagType)) {
                throw new IllegalArgumentException(
                        "Conflicting overlay tag source for %s.%s".formatted(
                                parent.currentType.getSimpleName(), fieldName));
            }

            this.tagVariantType = tagVariantType;
            this.tagFieldName = tagFieldName;
            this.tagType = tagType;
            this.semanticTagType = semanticTagType;
            return this;
        }

        public <V extends Record> OverlayUnionBuilder<R, C, U> caseOf(byte tag, Class<V> variantType) {
            return caseOf(TagValue.of(tag), variantType, null);
        }

        public <V extends Record> OverlayUnionBuilder<R, C, U> caseOf(
                byte tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            return caseOf(TagValue.of(tag), variantType, nested);
        }

        public <V extends Record> OverlayUnionBuilder<R, C, U> caseOf(short tag, Class<V> variantType) {
            return caseOf(TagValue.of(tag), variantType, null);
        }

        public <V extends Record> OverlayUnionBuilder<R, C, U> caseOf(
                short tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            return caseOf(TagValue.of(tag), variantType, nested);
        }

        public <V extends Record> OverlayUnionBuilder<R, C, U> caseOf(int tag, Class<V> variantType) {
            return caseOf(TagValue.of(tag), variantType, null);
        }

        public <V extends Record> OverlayUnionBuilder<R, C, U> caseOf(
                int tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            return caseOf(TagValue.of(tag), variantType, nested);
        }

        public <V extends Record> OverlayUnionBuilder<R, C, U> caseOf(long tag, Class<V> variantType) {
            return caseOf(TagValue.of(tag), variantType, null);
        }

        public <V extends Record> OverlayUnionBuilder<R, C, U> caseOf(
                long tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            return caseOf(TagValue.of(tag), variantType, nested);
        }

        public <V extends Record> OverlayUnionBuilder<R, C, U> caseOf(boolean tag, Class<V> variantType) {
            return caseOf(TagValue.of(tag), variantType, null);
        }

        public <V extends Record> OverlayUnionBuilder<R, C, U> caseOf(
                boolean tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            return caseOf(TagValue.of(tag), variantType, nested);
        }

        public <V extends Record> OverlayUnionBuilder<R, C, U> caseOf(char tag, Class<V> variantType) {
            return caseOf(TagValue.of(tag), variantType, null);
        }

        public <V extends Record> OverlayUnionBuilder<R, C, U> caseOf(
                char tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            return caseOf(TagValue.of(tag), variantType, nested);
        }

        public <T extends Record, V extends Record> OverlayUnionBuilder<R, C, U> caseOf(
                T tag,
                Class<V> variantType) {
            return caseOf(TagValue.of(tag), variantType, null);
        }

        public <T extends Record, V extends Record> OverlayUnionBuilder<R, C, U> caseOf(
                T tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            return caseOf(TagValue.of(tag), variantType, nested);
        }

        private <V extends Record> OverlayUnionBuilder<R, C, U> caseOf(
                TagValue tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            if (tagVariantType == null)
                throw new IllegalStateException(
                        "Call tagFrom(...) before caseOf(...) for " + fieldName);
            if (tag.type() != semanticTagType) {
                throw new IllegalArgumentException(
                        "Tag case uses %s but %s.%s is %s".formatted(
                                tag.type().getSimpleName(),
                                tagVariantType.getSimpleName(),
                                tagFieldName,
                                semanticTagType.getSimpleName()));
            }
            requireRecord(variantType, "Variant type");
            if (!unionType.isAssignableFrom(variantType)) {
                throw new IllegalArgumentException(
                        "%s does not implement/extend %s for %s.%s".formatted(
                                variantType.getSimpleName(),
                                unionType.getSimpleName(),
                                parent.currentType.getSimpleName(),
                                fieldName));
            }
            if (cases.containsKey(tag)) {
                throw new IllegalArgumentException(
                        "Duplicate tag case %s for %s.%s".formatted(
                                tag, parent.currentType.getSimpleName(), fieldName));
            }

            var nestedBuilder = new Builder<R, V>(parent.rootType, variantType);
            if (nested != null)
                nested.accept(nestedBuilder);
            cases.put(tag, new TaggedCaseBuilder(tag, variantType, nestedBuilder.choices));
            parent.putOverlayUnion(this);
            return this;
        }
    }

    /**
     * Builder for selecting one concrete variant for all elements of a union
     * array field.
     *
     * @param <R> the declared root type
     * @param <C> the current record type
     * @param <U> the declared union element type
     */
    public static final class UnionArrayBuilder<R, C extends Record, U> {
        private final Builder<R, C> parent;
        private final String fieldName;
        private final Class<U> unionType;
        private boolean selected;

        private UnionArrayBuilder(Builder<R, C> parent, String fieldName, Class<U> unionType) {
            this.parent = parent;
            this.fieldName = fieldName;
            this.unionType = unionType;
        }

        /**
         * Selects the concrete record variant for every element.
         *
         * @param <V> the variant record type
         * @param variantType the selected variant type
         */
        public <V extends Record> void variant(Class<V> variantType) {
            variant(variantType, null);
        }

        /**
         * Selects the concrete record variant for every element and configures
         * nested choices inside it.
         *
         * @param <V> the variant record type
         * @param variantType the selected variant type
         * @param nested nested shape configuration
         */
        public <V extends Record> void variant(
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            if (selected)
                throw new IllegalStateException(
                        "Variant already selected for " + fieldName);
            selected = true;
            parent.putUnionArrayVariant(fieldName, unionType, variantType, nested);
        }
    }

    /**
     * Builder for tag-to-variant mappings where a record has sibling tag and
     * union payload fields.
     *
     * @param <R> the declared root type
     * @param <C> the current record type
     * @param <U> the declared union payload type
     */
    public static final class TaggedUnionBuilder<R, C extends Record, U> {
        private final Builder<R, C> parent;
        private final String tagFieldName;
        private final Class<?> tagType;
        private final Class<?> semanticTagType;
        private final String payloadFieldName;
        private final Class<U> unionType;
        private final LinkedHashMap<TagValue, TaggedCaseBuilder> cases = new LinkedHashMap<>();

        private TaggedUnionBuilder(
                Builder<R, C> parent,
                String tagFieldName,
                Class<?> tagType,
                Class<?> semanticTagType,
                String payloadFieldName,
                Class<U> unionType) {
            this.parent = parent;
            this.tagFieldName = tagFieldName;
            this.tagType = tagType;
            this.semanticTagType = semanticTagType;
            this.payloadFieldName = payloadFieldName;
            this.unionType = unionType;
        }

        public <V extends Record> TaggedUnionBuilder<R, C, U> caseOf(byte tag, Class<V> variantType) {
            return caseOf(TagValue.of(tag), variantType, null);
        }

        public <V extends Record> TaggedUnionBuilder<R, C, U> caseOf(
                byte tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            return caseOf(TagValue.of(tag), variantType, nested);
        }

        public <V extends Record> TaggedUnionBuilder<R, C, U> caseOf(short tag, Class<V> variantType) {
            return caseOf(TagValue.of(tag), variantType, null);
        }

        public <V extends Record> TaggedUnionBuilder<R, C, U> caseOf(
                short tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            return caseOf(TagValue.of(tag), variantType, nested);
        }

        public <V extends Record> TaggedUnionBuilder<R, C, U> caseOf(int tag, Class<V> variantType) {
            return caseOf(TagValue.of(tag), variantType, null);
        }

        public <V extends Record> TaggedUnionBuilder<R, C, U> caseOf(
                int tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            return caseOf(TagValue.of(tag), variantType, nested);
        }

        public <V extends Record> TaggedUnionBuilder<R, C, U> caseOf(long tag, Class<V> variantType) {
            return caseOf(TagValue.of(tag), variantType, null);
        }

        public <V extends Record> TaggedUnionBuilder<R, C, U> caseOf(
                long tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            return caseOf(TagValue.of(tag), variantType, nested);
        }

        public <V extends Record> TaggedUnionBuilder<R, C, U> caseOf(boolean tag, Class<V> variantType) {
            return caseOf(TagValue.of(tag), variantType, null);
        }

        public <V extends Record> TaggedUnionBuilder<R, C, U> caseOf(
                boolean tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            return caseOf(TagValue.of(tag), variantType, nested);
        }

        public <V extends Record> TaggedUnionBuilder<R, C, U> caseOf(char tag, Class<V> variantType) {
            return caseOf(TagValue.of(tag), variantType, null);
        }

        public <V extends Record> TaggedUnionBuilder<R, C, U> caseOf(
                char tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            return caseOf(TagValue.of(tag), variantType, nested);
        }

        public <T extends Record, V extends Record> TaggedUnionBuilder<R, C, U> caseOf(
                T tag,
                Class<V> variantType) {
            return caseOf(TagValue.of(tag), variantType, null);
        }

        public <T extends Record, V extends Record> TaggedUnionBuilder<R, C, U> caseOf(
                T tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            return caseOf(TagValue.of(tag), variantType, nested);
        }

        private <V extends Record> TaggedUnionBuilder<R, C, U> caseOf(
                TagValue tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            if (tag.type() != semanticTagType) {
                throw new IllegalArgumentException(
                        "Tag case uses %s but %s.%s is %s".formatted(
                                tag.type().getSimpleName(),
                                parent.currentType.getSimpleName(),
                                tagFieldName,
                                semanticTagType.getSimpleName()));
            }
            requireRecord(variantType, "Variant type");
            if (!unionType.isAssignableFrom(variantType)) {
                throw new IllegalArgumentException(
                        "%s does not implement/extend %s for %s.%s".formatted(
                                variantType.getSimpleName(),
                                unionType.getSimpleName(),
                                parent.currentType.getSimpleName(),
                                payloadFieldName));
            }
            if (cases.containsKey(tag)) {
                throw new IllegalArgumentException(
                        "Duplicate tag case %s for %s.%s".formatted(
                                tag, parent.currentType.getSimpleName(), payloadFieldName));
            }

            var nestedBuilder = new Builder<R, V>(parent.rootType, variantType);
            if (nested != null)
                nested.accept(nestedBuilder);
            cases.put(tag, new TaggedCaseBuilder(tag, variantType, nestedBuilder.choices));
            return this;
        }
    }

    /**
     * Builder for tag-to-variant mappings in a tagged union array.
     *
     * @param <R> the declared root type
     * @param <C> the current record type
     * @param <N> the array element record type
     * @param <U> the declared union payload type
     */
    public static final class TaggedUnionArrayBuilder<R, C extends Record, N extends Record, U> {
        private final Builder<R, C> parent;
        private final String fieldName;
        private final Class<N> elementType;
        private final String tagFieldName;
        private final Class<?> tagType;
        private final Class<?> semanticTagType;
        private final String payloadFieldName;
        private final Class<U> unionType;
        private final LinkedHashMap<TagValue, TaggedCaseBuilder> cases = new LinkedHashMap<>();

        private TaggedUnionArrayBuilder(
                Builder<R, C> parent,
                String fieldName,
                Class<N> elementType,
                String tagFieldName,
                Class<?> tagType,
                Class<?> semanticTagType,
                String payloadFieldName,
                Class<U> unionType) {
            this.parent = parent;
            this.fieldName = fieldName;
            this.elementType = elementType;
            this.tagFieldName = tagFieldName;
            this.tagType = tagType;
            this.semanticTagType = semanticTagType;
            this.payloadFieldName = payloadFieldName;
            this.unionType = unionType;
        }

        public <V extends Record> TaggedUnionArrayBuilder<R, C, N, U> caseOf(byte tag, Class<V> variantType) {
            return caseOf(TagValue.of(tag), variantType, null);
        }

        public <V extends Record> TaggedUnionArrayBuilder<R, C, N, U> caseOf(
                byte tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            return caseOf(TagValue.of(tag), variantType, nested);
        }

        public <V extends Record> TaggedUnionArrayBuilder<R, C, N, U> caseOf(short tag, Class<V> variantType) {
            return caseOf(TagValue.of(tag), variantType, null);
        }

        public <V extends Record> TaggedUnionArrayBuilder<R, C, N, U> caseOf(
                short tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            return caseOf(TagValue.of(tag), variantType, nested);
        }

        public <V extends Record> TaggedUnionArrayBuilder<R, C, N, U> caseOf(int tag, Class<V> variantType) {
            return caseOf(TagValue.of(tag), variantType, null);
        }

        public <V extends Record> TaggedUnionArrayBuilder<R, C, N, U> caseOf(
                int tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            return caseOf(TagValue.of(tag), variantType, nested);
        }

        public <V extends Record> TaggedUnionArrayBuilder<R, C, N, U> caseOf(long tag, Class<V> variantType) {
            return caseOf(TagValue.of(tag), variantType, null);
        }

        public <V extends Record> TaggedUnionArrayBuilder<R, C, N, U> caseOf(
                long tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            return caseOf(TagValue.of(tag), variantType, nested);
        }

        public <V extends Record> TaggedUnionArrayBuilder<R, C, N, U> caseOf(boolean tag, Class<V> variantType) {
            return caseOf(TagValue.of(tag), variantType, null);
        }

        public <V extends Record> TaggedUnionArrayBuilder<R, C, N, U> caseOf(
                boolean tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            return caseOf(TagValue.of(tag), variantType, nested);
        }

        public <V extends Record> TaggedUnionArrayBuilder<R, C, N, U> caseOf(char tag, Class<V> variantType) {
            return caseOf(TagValue.of(tag), variantType, null);
        }

        public <V extends Record> TaggedUnionArrayBuilder<R, C, N, U> caseOf(
                char tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            return caseOf(TagValue.of(tag), variantType, nested);
        }

        public <T extends Record, V extends Record> TaggedUnionArrayBuilder<R, C, N, U> caseOf(
                T tag,
                Class<V> variantType) {
            return caseOf(TagValue.of(tag), variantType, null);
        }

        public <T extends Record, V extends Record> TaggedUnionArrayBuilder<R, C, N, U> caseOf(
                T tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            return caseOf(TagValue.of(tag), variantType, nested);
        }

        private <V extends Record> TaggedUnionArrayBuilder<R, C, N, U> caseOf(
                TagValue tag,
                Class<V> variantType,
                Consumer<Builder<R, V>> nested) {
            if (tag.type() != semanticTagType) {
                throw new IllegalArgumentException(
                        "Tag case uses %s but %s.%s is %s".formatted(
                                tag.type().getSimpleName(),
                                elementType.getSimpleName(),
                                tagFieldName,
                                semanticTagType.getSimpleName()));
            }
            requireRecord(variantType, "Variant type");
            if (!unionType.isAssignableFrom(variantType)) {
                throw new IllegalArgumentException(
                        "%s does not implement/extend %s for %s.%s[]".formatted(
                                variantType.getSimpleName(),
                                unionType.getSimpleName(),
                                parent.currentType.getSimpleName(),
                                fieldName));
            }
            if (cases.containsKey(tag)) {
                throw new IllegalArgumentException(
                        "Duplicate tag case %s for %s.%s".formatted(
                                tag, parent.currentType.getSimpleName(), fieldName));
            }

            var nestedBuilder = new Builder<R, V>(parent.rootType, variantType);
            if (nested != null)
                nested.accept(nestedBuilder);
            cases.put(tag, new TaggedCaseBuilder(tag, variantType, nestedBuilder.choices));
            return this;
        }
    }

    private abstract static class ChoiceBuilder {
        protected final String fieldName;
        protected final LinkedHashMap<String, ChoiceBuilder> children = new LinkedHashMap<>();

        private ChoiceBuilder(String fieldName) {
            this.fieldName = fieldName;
        }

        private void mergeChildren(Map<String, ChoiceBuilder> incoming) {
            for (var entry : incoming.entrySet()) {
                var existing = children.get(entry.getKey());
                var next = entry.getValue();
                if (existing == null) {
                    children.put(entry.getKey(), next);
                } else if (!existing.sameChoice(next)) {
                    throw new IllegalArgumentException(
                            "Conflicting shape choices for nested field %s".formatted(entry.getKey()));
                } else {
                    existing.mergeChildren(next.children);
                }
            }
        }

        abstract boolean sameChoice(ChoiceBuilder other);

        abstract ShapeChoice build();
    }

    private static final class UnionChoiceBuilder extends ChoiceBuilder {
        private final Class<?> unionType;
        private final Class<? extends Record> variantType;

        private UnionChoiceBuilder(String fieldName, Class<?> unionType, Class<? extends Record> variantType) {
            super(fieldName);
            this.unionType = unionType;
            this.variantType = variantType;
        }

        @Override
        boolean sameChoice(ChoiceBuilder other) {
            return other instanceof UnionChoiceBuilder union
                    && union.unionType == unionType
                    && union.variantType == variantType;
        }

        @Override
        ShapeChoice build() {
            return new UnionChoice(fieldName, unionType, variantType, Builder.buildChoices(children));
        }
    }

    private static final class OverlayUnionChoiceBuilder extends ChoiceBuilder {
        private final Class<?> unionType;
        private final Class<? extends Record> tagVariantType;
        private final String tagFieldName;
        private final Class<?> tagType;
        private final Class<?> semanticTagType;
        private final LinkedHashMap<TagValue, TaggedCaseBuilder> cases;

        private OverlayUnionChoiceBuilder(
                String fieldName,
                Class<?> unionType,
                Class<? extends Record> tagVariantType,
                String tagFieldName,
                Class<?> tagType,
                Class<?> semanticTagType,
                Map<TagValue, TaggedCaseBuilder> cases) {
            super(fieldName);
            this.unionType = unionType;
            this.tagVariantType = tagVariantType;
            this.tagFieldName = tagFieldName;
            this.tagType = tagType;
            this.semanticTagType = semanticTagType;
            this.cases = new LinkedHashMap<>(cases);
            if (this.cases.isEmpty())
                throw new IllegalArgumentException("Overlay union must have at least one case");
        }

        private boolean sameDefinition(OverlayUnionBuilder<?, ?, ?> builder) {
            return unionType == builder.unionType
                    && tagVariantType == builder.tagVariantType
                    && tagFieldName.equals(builder.tagFieldName)
                    && tagType == builder.tagType
                    && semanticTagType == builder.semanticTagType;
        }

        private void mergeCases(Map<TagValue, TaggedCaseBuilder> incoming) {
            for (var entry : incoming.entrySet()) {
                var existing = cases.get(entry.getKey());
                if (existing == null) {
                    cases.put(entry.getKey(), entry.getValue());
                } else if (!existing.sameCase(entry.getValue())) {
                    throw new IllegalArgumentException(
                            "Conflicting overlay union case for %s".formatted(entry.getKey()));
                } else {
                    existing.mergeChildren(entry.getValue().children);
                }
            }
        }

        @Override
        boolean sameChoice(ChoiceBuilder other) {
            return other instanceof OverlayUnionChoiceBuilder overlay
                    && overlay.fieldName.equals(fieldName)
                    && overlay.unionType == unionType
                    && overlay.tagVariantType == tagVariantType
                    && overlay.tagFieldName.equals(tagFieldName)
                    && overlay.tagType == tagType
                    && overlay.semanticTagType == semanticTagType
                    && overlay.cases.equals(cases);
        }

        @Override
        ShapeChoice build() {
            return new OverlayUnionChoice(
                    fieldName,
                    unionType,
                    tagVariantType,
                    tagFieldName,
                    tagType,
                    semanticTagType,
                    buildCases(cases));
        }
    }

    private static final class RecordChoiceBuilder extends ChoiceBuilder {
        private final Class<? extends Record> recordType;

        private RecordChoiceBuilder(String fieldName, Class<? extends Record> recordType) {
            super(fieldName);
            this.recordType = recordType;
        }

        @Override
        boolean sameChoice(ChoiceBuilder other) {
            return other instanceof RecordChoiceBuilder record
                    && record.recordType == recordType;
        }

        @Override
        ShapeChoice build() {
            return new RecordChoice(fieldName, recordType, Builder.buildChoices(children));
        }
    }

    private static final class ArrayChoiceBuilder extends ChoiceBuilder {
        private final Class<? extends Record> elementType;

        private ArrayChoiceBuilder(String fieldName, Class<? extends Record> elementType) {
            super(fieldName);
            this.elementType = elementType;
        }

        @Override
        boolean sameChoice(ChoiceBuilder other) {
            return other instanceof ArrayChoiceBuilder array
                    && array.elementType == elementType;
        }

        @Override
        ShapeChoice build() {
            return new ArrayChoice(fieldName, elementType, Builder.buildChoices(children));
        }
    }

    private static final class UnionArrayChoiceBuilder extends ChoiceBuilder {
        private final Class<?> unionType;
        private final Class<? extends Record> variantType;

        private UnionArrayChoiceBuilder(String fieldName, Class<?> unionType, Class<? extends Record> variantType) {
            super(fieldName);
            this.unionType = unionType;
            this.variantType = variantType;
        }

        @Override
        boolean sameChoice(ChoiceBuilder other) {
            return other instanceof UnionArrayChoiceBuilder array
                    && array.unionType == unionType
                    && array.variantType == variantType;
        }

        @Override
        ShapeChoice build() {
            return new UnionArrayChoice(fieldName, unionType, variantType, Builder.buildChoices(children));
        }
    }

    private static final class TaggedUnionChoiceBuilder extends ChoiceBuilder {
        private final String tagFieldName;
        private final Class<?> tagType;
        private final Class<?> semanticTagType;
        private final Class<?> unionType;
        private final LinkedHashMap<TagValue, TaggedCaseBuilder> cases;

        private TaggedUnionChoiceBuilder(
                String payloadFieldName,
                String tagFieldName,
                Class<?> tagType,
                Class<?> semanticTagType,
                Class<?> unionType,
                Map<TagValue, TaggedCaseBuilder> cases) {
            super(payloadFieldName);
            this.tagFieldName = tagFieldName;
            this.tagType = tagType;
            this.semanticTagType = semanticTagType;
            this.unionType = unionType;
            this.cases = new LinkedHashMap<>(cases);
            if (this.cases.isEmpty())
                throw new IllegalArgumentException("Tagged union must have at least one case");
        }

        private boolean sameDefinition(TaggedUnionBuilder<?, ?, ?> builder) {
            return tagFieldName.equals(builder.tagFieldName)
                    && tagType == builder.tagType
                    && semanticTagType == builder.semanticTagType
                    && unionType == builder.unionType;
        }

        private void mergeCases(Map<TagValue, TaggedCaseBuilder> incoming) {
            for (var entry : incoming.entrySet()) {
                var existing = cases.get(entry.getKey());
                if (existing == null) {
                    cases.put(entry.getKey(), entry.getValue());
                } else if (!existing.sameCase(entry.getValue())) {
                    throw new IllegalArgumentException(
                            "Conflicting tagged union case for %s".formatted(entry.getKey()));
                } else {
                    existing.mergeChildren(entry.getValue().children);
                }
            }
        }

        @Override
        boolean sameChoice(ChoiceBuilder other) {
            return other instanceof TaggedUnionChoiceBuilder tagged
                    && tagged.fieldName.equals(fieldName)
                    && tagged.tagFieldName.equals(tagFieldName)
                    && tagged.tagType == tagType
                    && tagged.semanticTagType == semanticTagType
                    && tagged.unionType == unionType
                    && tagged.cases.equals(cases);
        }

        @Override
        ShapeChoice build() {
            return new TaggedUnionChoice(fieldName, tagFieldName, tagType, semanticTagType, unionType, buildCases(cases));
        }
    }

    private static final class TaggedUnionArrayChoiceBuilder extends ChoiceBuilder {
        private final Class<? extends Record> elementType;
        private final String tagFieldName;
        private final Class<?> tagType;
        private final Class<?> semanticTagType;
        private final String payloadFieldName;
        private final Class<?> unionType;
        private final LinkedHashMap<TagValue, TaggedCaseBuilder> cases;

        private TaggedUnionArrayChoiceBuilder(
                String fieldName,
                Class<? extends Record> elementType,
                String tagFieldName,
                Class<?> tagType,
                Class<?> semanticTagType,
                String payloadFieldName,
                Class<?> unionType,
                Map<TagValue, TaggedCaseBuilder> cases) {
            super(fieldName);
            this.elementType = elementType;
            this.tagFieldName = tagFieldName;
            this.tagType = tagType;
            this.semanticTagType = semanticTagType;
            this.payloadFieldName = payloadFieldName;
            this.unionType = unionType;
            this.cases = new LinkedHashMap<>(cases);
            if (this.cases.isEmpty())
                throw new IllegalArgumentException("Tagged union array must have at least one case");
        }

        private boolean sameDefinition(TaggedUnionArrayBuilder<?, ?, ?, ?> builder) {
            return elementType == builder.elementType
                    && tagFieldName.equals(builder.tagFieldName)
                    && tagType == builder.tagType
                    && semanticTagType == builder.semanticTagType
                    && payloadFieldName.equals(builder.payloadFieldName)
                    && unionType == builder.unionType;
        }

        private void mergeCases(Map<TagValue, TaggedCaseBuilder> incoming) {
            for (var entry : incoming.entrySet()) {
                var existing = cases.get(entry.getKey());
                if (existing == null) {
                    cases.put(entry.getKey(), entry.getValue());
                } else if (!existing.sameCase(entry.getValue())) {
                    throw new IllegalArgumentException(
                            "Conflicting tagged union case for %s".formatted(entry.getKey()));
                } else {
                    existing.mergeChildren(entry.getValue().children);
                }
            }
        }

        @Override
        boolean sameChoice(ChoiceBuilder other) {
            return other instanceof TaggedUnionArrayChoiceBuilder tagged
                    && tagged.elementType == elementType
                    && tagged.tagFieldName.equals(tagFieldName)
                    && tagged.tagType == tagType
                    && tagged.semanticTagType == semanticTagType
                    && tagged.payloadFieldName.equals(payloadFieldName)
                    && tagged.unionType == unionType
                    && tagged.cases.equals(cases);
        }

        @Override
        ShapeChoice build() {
            return new TaggedUnionArrayChoice(
                    fieldName,
                    elementType,
                    tagFieldName,
                    tagType,
                    semanticTagType,
                    payloadFieldName,
                    unionType,
                    buildCases(cases));
        }
    }

    private static List<TaggedUnionCase> buildCases(Map<TagValue, TaggedCaseBuilder> cases) {
        var ordered = new ArrayList<>(cases.values());
        ordered.sort(Comparator.comparing((TaggedCaseBuilder taggedCase) -> taggedCase.tag, MemShapes::compareTags)
                .thenComparing(taggedCase -> taggedCase.variantType.getName()));

        var out = new ArrayList<TaggedUnionCase>(ordered.size());
        for (var taggedCase : ordered) {
            out.add(taggedCase.build());
        }
        return List.copyOf(out);
    }

    private static int compareTags(TagValue left, TagValue right) {
        return left.sortKey().compareTo(right.sortKey());
    }

    private static final class TaggedCaseBuilder {
        private final TagValue tag;
        private final Class<? extends Record> variantType;
        private final LinkedHashMap<String, ChoiceBuilder> children;

        private TaggedCaseBuilder(
                TagValue tag,
                Class<? extends Record> variantType,
                Map<String, ChoiceBuilder> children) {
            this.tag = tag;
            this.variantType = variantType;
            this.children = new LinkedHashMap<>(children);
        }

        private boolean sameCase(TaggedCaseBuilder other) {
            return tag.equals(other.tag)
                    && variantType == other.variantType;
        }

        private void mergeChildren(Map<String, ChoiceBuilder> incoming) {
            for (var entry : incoming.entrySet()) {
                var existing = children.get(entry.getKey());
                var next = entry.getValue();
                if (existing == null) {
                    children.put(entry.getKey(), next);
                } else if (!existing.sameChoice(next)) {
                    throw new IllegalArgumentException(
                            "Conflicting shape choices for nested field %s".formatted(entry.getKey()));
                } else {
                    existing.mergeChildren(next.children);
                }
            }
        }

        private TaggedUnionCase build() {
            return new TaggedUnionCase(tag, variantType, Builder.buildChoices(children));
        }
    }

    private static void validateComplete(
            Class<? extends Record> recordType,
            Map<String, ChoiceBuilder> choices) {
        for (var component : recordType.getRecordComponents()) {
            var componentType = component.getType();
            if (isUnion(componentType)) {
                var choice = choices.get(component.getName());
                if (choice instanceof OverlayUnionChoiceBuilder overlay) {
                    if (overlay.unionType != componentType) {
                        throw new IllegalArgumentException(
                                "Invalid overlay union field %s.%s".formatted(
                                        recordType.getSimpleName(), component.getName()));
                    }
                    for (var taggedCase : overlay.cases.values()) {
                        validateComplete(taggedCase.variantType, taggedCase.children);
                    }
                    continue;
                }

                if (choice instanceof TaggedUnionChoiceBuilder tagged) {
                    if (tagged.unionType != componentType) {
                        throw new IllegalArgumentException(
                                "Invalid tagged union field %s.%s".formatted(
                                        recordType.getSimpleName(), component.getName()));
                    }
                    for (var taggedCase : tagged.cases.values()) {
                        validateComplete(taggedCase.variantType, taggedCase.children);
                    }
                    continue;
                }

                if (!(choice instanceof UnionChoiceBuilder union)) {
                    throw new IllegalArgumentException(
                            "Incomplete shape: %s.%s is %s but no variant was selected".formatted(
                                    recordType.getSimpleName(),
                                    component.getName(),
                                    componentType.getSimpleName()));
                }

                validateComplete(union.variantType, union.children);
                continue;
            }

            if (componentType.isRecord()) {
                var choice = choices.get(component.getName());
                var children = switch (choice) {
                    case null -> Map.<String, ChoiceBuilder>of();
                    case RecordChoiceBuilder record when record.recordType == componentType -> record.children;
                    default -> throw new IllegalArgumentException(
                            "Invalid shape choice for record field %s.%s".formatted(
                                    recordType.getSimpleName(), component.getName()));
                };
                validateComplete((Class<? extends Record>) componentType, children);
                continue;
            }

            if (componentType.isArray()) {
                var elementType = componentType.getComponentType();
                if (isUnion(elementType)) {
                    var choice = choices.get(component.getName());
                    if (!(choice instanceof UnionArrayChoiceBuilder array)) {
                        throw new IllegalArgumentException(
                                "Incomplete shape: %s.%s is %s[] but no element variant was selected".formatted(
                                        recordType.getSimpleName(),
                                        component.getName(),
                                        elementType.getSimpleName()));
                    }
                    validateComplete(array.variantType, array.children);
                    continue;
                }

                if (elementType.isRecord()) {
                    var choice = choices.get(component.getName());
                    if (choice instanceof TaggedUnionArrayChoiceBuilder tagged) {
                        if (tagged.elementType != elementType) {
                            throw new IllegalArgumentException(
                                    "Invalid tagged union array field %s.%s".formatted(
                                            recordType.getSimpleName(), component.getName()));
                        }
                        for (var taggedCase : tagged.cases.values()) {
                            validateComplete(taggedCase.variantType, taggedCase.children);
                        }
                        continue;
                    }

                    var children = switch (choice) {
                        case null -> Map.<String, ChoiceBuilder>of();
                        case ArrayChoiceBuilder array when array.elementType == elementType -> array.children;
                        default -> throw new IllegalArgumentException(
                                "Invalid shape choice for record array field %s.%s".formatted(
                                        recordType.getSimpleName(), component.getName()));
                    };
                    validateComplete((Class<? extends Record>) elementType, children);
                }
            }
        }
    }

    private static void requireRecord(Class<?> type, String label) {
        if (!type.isRecord())
            throw new IllegalArgumentException(label + " must be a record: " + type.getName());
    }

    private static void requireUnion(Class<?> type, Class<?> owner, String fieldName) {
        if (!isUnion(type)) {
            throw new IllegalArgumentException(
                    "%s.%s is %s, not a union interface/abstract type".formatted(
                            owner.getSimpleName(), fieldName, type.getSimpleName()));
        }
    }

    private static boolean isUnion(Class<?> type) {
        if (type.isPrimitive() || type.isArray() || type.isEnum() || type.isRecord())
            return false;
        return type.isInterface() || Modifier.isAbstract(type.getModifiers());
    }

    private static RecordComponent component(Class<?> recordType, String name) {
        Objects.requireNonNull(name);
        if (!recordType.isRecord())
            throw new IllegalArgumentException("Cannot select a field from non-record type: " + recordType.getName());
        for (var component : recordType.getRecordComponents()) {
            if (component.getName().equals(name))
                return component;
        }
        throw new IllegalArgumentException(
                "Unknown field '%s' in %s".formatted(name, recordType.getSimpleName()));
    }
}
