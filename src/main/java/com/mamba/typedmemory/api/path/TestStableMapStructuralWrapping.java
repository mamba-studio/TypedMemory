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

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author joemw
 */
public class TestStableMapStructuralWrapping {

    private static final StructuralKey CANONICAL_KEY =
        new StructuralKey("payload", "machine", "sensor");

    private static final StructuralKey EQUAL_KEY =
        new StructuralKey("payload", "machine", "sensor");

    private static final Set<StructuralKey> KEYS = Set.of(
        new StructuralKey("payload", "human", "text"),
        new StructuralKey("payload", "human", "profile"),
        CANONICAL_KEY,
        new StructuralKey("payload", "machine", "binary")
    );

    private static final Map<StructuralKey, StructuralKey> IDENTITY_MAP =
        StableValue.map(KEYS, key -> key);

    private static final Map<StructuralKey, WrappedKey> WRAPPER_MAP =
        StableValue.map(KEYS, WrappedKey::new);

    private static volatile Object sink;

    void main() {
        IDENTITY_MAP.get(EQUAL_KEY);
        WRAPPER_MAP.get(EQUAL_KEY);

        for (int i = 0; i < 5_000_000; i++)
            sink = hotCanonicalIdentityContains();

        for (int i = 0; i < 5_000_000; i++)
            sink = hotEqualIdentityContains();

        for (int i = 0; i < 5_000_000; i++)
            sink = hotCanonicalWrapperContains();

        for (int i = 0; i < 5_000_000; i++)
            sink = hotEqualWrapperContains();

        IO.println(hotCanonicalIdentityContains());
        IO.println(hotEqualIdentityContains());
        IO.println(hotCanonicalWrapperContains());
        IO.println(hotEqualWrapperContains());
    }

    static boolean hotCanonicalIdentityContains() {
        return IDENTITY_MAP.get(CANONICAL_KEY) == CANONICAL_KEY;
    }

    static boolean hotEqualIdentityContains() {
        return IDENTITY_MAP.get(EQUAL_KEY) == CANONICAL_KEY;
    }

    static boolean hotCanonicalWrapperContains() {
        return WRAPPER_MAP.get(CANONICAL_KEY).key() == CANONICAL_KEY;
    }

    static boolean hotEqualWrapperContains() {
        return WRAPPER_MAP.get(EQUAL_KEY).key() == CANONICAL_KEY;
    }

    record WrappedKey(StructuralKey key) {}

    record StructuralKey(Object[] parts, int hash) {

        StructuralKey(Object... parts) {
            this(parts, Arrays.hashCode(parts));
        }

        StructuralKey {
            parts = parts.clone();
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof StructuralKey key
                && Arrays.equals(parts, key.parts);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
