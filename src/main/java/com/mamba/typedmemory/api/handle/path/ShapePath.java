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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A canonical path to a variant-bearing union occurrence.
 *
 * @param tokens structural path tokens
 */
public record ShapePath(List<ShapePathToken> tokens) {

    public ShapePath {
        tokens = List.copyOf(tokens);
        if (tokens.isEmpty())
            throw new IllegalArgumentException("Shape path must have at least a root type");
    }

    public static ShapePath root(Class<?> rootType) {
        Objects.requireNonNull(rootType);
        return new ShapePath(List.of(new ShapePathToken.Type(rootType)));
    }

    public ShapePath append(ShapePathToken token) {
        Objects.requireNonNull(token);
        var out = new ArrayList<ShapePathToken>(tokens.size() + 1);
        out.addAll(tokens);
        out.add(token);
        return new ShapePath(out);
    }
}
