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

import java.util.Map;
import java.util.Set;

/**
 *
 * @author joemw
 */
public class TestStableMapWrapping {

    private static final Set<Key> KEYS =
        Set.of(new Key(1), new Key(2), new Key(4), new Key(8), new Key(16));

    private static final Key KEY = new Key(16);

    private static final Map<Key, Key> IDENTITY_MAP =
        StableValue.map(KEYS, key -> key);

    private static final Map<Key, Wrapper> WRAPPER_MAP =
        StableValue.map(KEYS, Wrapper::new);

    private static volatile Object sink;

    void main() {
        for (int i = 0; i < 5_000_000; i++)
            sink = hotIdentityGet();

        for (int i = 0; i < 5_000_000; i++)
            sink = hotIdentityContains();

        for (int i = 0; i < 5_000_000; i++)
            sink = hotWrapperGet();

        for (int i = 0; i < 5_000_000; i++)
            sink = hotWrapperContains();

        IO.println(hotIdentityGet() == KEY);
        IO.println(hotIdentityContains());
        IO.println(hotWrapperGet().key() == KEY);
        IO.println(hotWrapperContains());
    }

    static Key hotIdentityGet() {
        return IDENTITY_MAP.get(KEY);
    }

    static boolean hotIdentityContains() {
        return IDENTITY_MAP.get(KEY) == KEY;
    }

    static Wrapper hotWrapperGet() {
        return WRAPPER_MAP.get(KEY);
    }

    static boolean hotWrapperContains() {
        return WRAPPER_MAP.get(KEY).key() == KEY;
    }

    record Key(int value) {}

    record Wrapper(Key key) {}
}
