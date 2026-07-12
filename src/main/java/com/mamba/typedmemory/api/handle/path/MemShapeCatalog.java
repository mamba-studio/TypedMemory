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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Generated registry of variant-bearing paths reachable from a root type.
 *
 * @param <R> the root type
 * @param rootType the root type
 * @param shapesByPath valid variant shapes at each union path
 */
public record MemShapeCatalog<R>(
        Class<R> rootType,
        Map<ShapePath, Map<MemShape<?>, Class<? extends Record>>> shapesByPath) {

    public MemShapeCatalog {
        Objects.requireNonNull(rootType);
        var copy = new LinkedHashMap<ShapePath, Map<MemShape<?>, Class<? extends Record>>>();
        for (var entry : shapesByPath.entrySet()) {
            copy.put(entry.getKey(), Map.copyOf(entry.getValue()));
        }
        shapesByPath = Map.copyOf(copy);
    }

    public static <R> MemShapeCatalog<R> of(Class<R> rootType) {
        Objects.requireNonNull(rootType);
        var generator = new Generator<R>(rootType);
        return generator.generate();
    }

    public Set<ShapePath> paths() {
        return shapesByPath.keySet();
    }

    public Map<MemShape<?>, Class<? extends Record>> shapesAt(ShapePath path) {
        return shapesByPath.getOrDefault(path, Map.of());
    }

    public boolean contains(ShapePath path, MemShape<?> shape) {
        return shapesAt(path).containsKey(shape);
    }

    private static final class Generator<R> {
        private final Class<R> rootType;
        private final LinkedHashMap<ShapePath, LinkedHashMap<MemShape<?>, Class<? extends Record>>> shapesByPath
                = new LinkedHashMap<>();

        private Generator(Class<R> rootType) {
            this.rootType = rootType;
        }

        private MemShapeCatalog<R> generate() {
            var rootPath = ShapePath.root(rootType);
            var stack = new ArrayDeque<Class<?>>();
            if (isUnion(rootType)) {
                addUnion(rootPath, rootType);
                for (var variant : variants(rootType)) {
                    walkRecord(variant, rootPath.append(new ShapePathToken.Variant(rootType, variant)), stack);
                }
            } else if (rootType.isRecord()) {
                walkRecord(asRecord(rootType), rootPath, stack);
            } else {
                throw new IllegalArgumentException(
                        "Root type must be a record or union interface/abstract type: " + rootType.getName());
            }
            return new MemShapeCatalog<>(rootType, freeze());
        }

        private Map<ShapePath, Map<MemShape<?>, Class<? extends Record>>> freeze() {
            var out = new LinkedHashMap<ShapePath, Map<MemShape<?>, Class<? extends Record>>>();
            for (var entry : shapesByPath.entrySet()) {
                out.put(entry.getKey(), entry.getValue());
            }
            return out;
        }

        private void walkRecord(
                Class<? extends Record> recordType,
                ShapePath path,
                ArrayDeque<Class<?>> stack) {
            if (stack.contains(recordType))
                return;
            stack.push(recordType);
            try {
                for (var component : recordType.getRecordComponents()) {
                    var componentType = component.getType();
                    if (isUnion(componentType)) {
                        var unionPath = path.append(new ShapePathToken.Field(component.getName(), componentType));
                        addUnion(unionPath, componentType);
                        for (var variant : variants(componentType)) {
                            walkRecord(variant, unionPath.append(new ShapePathToken.Variant(componentType, variant)), stack);
                        }
                        continue;
                    }

                    if (componentType.isRecord()) {
                        walkRecord(
                                asRecord(componentType),
                                path.append(new ShapePathToken.Field(component.getName(), componentType)),
                                stack);
                        continue;
                    }

                    if (componentType.isArray()) {
                        var elementType = componentType.getComponentType();
                        var arrayPath = path.append(
                                new ShapePathToken.ArrayField(component.getName(), componentType, elementType));
                        if (isUnion(elementType)) {
                            addUnion(arrayPath, elementType);
                            for (var variant : variants(elementType)) {
                                walkRecord(variant, arrayPath.append(new ShapePathToken.Variant(elementType, variant)), stack);
                            }
                            continue;
                        }

                        if (elementType.isRecord()) {
                            walkRecord(asRecord(elementType), arrayPath, stack);
                        }
                    }
                }
            } finally {
                stack.pop();
            }
        }

        private void addUnion(ShapePath path, Class<?> unionType) {
            var shapes = shapesByPath.computeIfAbsent(path, ignored -> new LinkedHashMap<>());
            for (var variant : variants(unionType)) {
                shapes.put(variantShape(unionType, variant), variant);
            }
        }
    }

    private static boolean isUnion(Class<?> type) {
        if (type.isPrimitive() || type.isArray() || type.isEnum() || type.isRecord())
            return false;
        return type.isInterface() || Modifier.isAbstract(type.getModifiers());
    }

    private static List<Class<? extends Record>> variants(Class<?> unionType) {
        var permitted = unionType.getPermittedSubclasses();
        if (permitted == null || permitted.length == 0) {
            throw new IllegalArgumentException(
                    "Union type must be sealed with permitted record variants: " + unionType.getName());
        }

        var out = new ArrayList<Class<? extends Record>>(permitted.length);
        for (var variant : permitted) {
            if (!variant.isRecord()) {
                throw new IllegalArgumentException(
                        "Union variant must be a record: %s permits %s".formatted(
                                unionType.getSimpleName(), variant.getName()));
            }
            out.add(asRecord(variant));
        }
        out.sort(Comparator.comparing(Class::getName));
        return List.copyOf(out);
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Record> asRecord(Class<?> type) {
        return (Class<? extends Record>) type;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static MemShape<?> variantShape(Class<?> unionType, Class<? extends Record> variantType) {
        return new MemShapeImpl(unionType, variantType, List.of());
    }
}
