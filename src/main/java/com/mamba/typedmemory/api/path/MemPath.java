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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author joemw
 * 
 */
public record MemPath(List<PathToken> tokens, int hash) {

    public MemPath {
        tokens = List.copyOf(tokens);
    }

    public MemPath(List<PathToken> tokens) {
        this(tokens, hash(tokens));
    }

    public static MemPath of(Object... rawTokens) {
        var tokens = new ArrayList<PathToken>(rawTokens.length);

        for (Object rawToken : rawTokens) {
            tokens.add(PathToken.of(rawToken));
        }

        return new MemPath(tokens);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof MemPath path
            && hash == path.hash;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return tokens.toString();
    }

    private static int hash(List<PathToken> tokens) {
        var hash = 1;

        for (var token : tokens) {
            hash = 31 * hash + switch (token) {
                case PathToken.Type type -> Objects.hash(PathToken.Type.class, type.type());
                case PathToken.Field field -> Objects.hash(PathToken.Field.class, field.name());
            };
        }

        return hash;
    }
}
