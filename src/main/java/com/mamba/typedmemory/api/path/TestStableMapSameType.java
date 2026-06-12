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

import com.mamba.typedmemory.api.path.TestStableMapSameType.MachinePayload.Sensor;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author joemw
 */
public class TestStableMapSameType {

    private static final List<MemPath> PATHS =
        MemPathValidation.discoverResolvablePaths(Payload.class);

    //key value same type hence JIT trusts this. Wrapping brings issues because we have to prove identity.
    private static final Map<MemPath, MemPath> PATH_MAP =
        StableValue.map(Set.copyOf(PATHS), path -> path);

    private static final MemPath FIRST_PATH = PATHS.getFirst();

    private static final MemPath DEEP_PATH = MemPath.of(
        Payload.class,
        MachinePayload.class,
        Sensor.class
    );

    private static volatile Object sink;

    void main() {
        var localDeepPath = MemPath.of(
            Payload.class,
            MachinePayload.class,
            Sensor.class
        );

        PATH_MAP.get(FIRST_PATH);
        PATH_MAP.get(DEEP_PATH);
        PATH_MAP.get(localDeepPath);

        for (int i = 0; i < 5_000_000; i++)
            sink = hotMapGetFirst();

        for (int i = 0; i < 5_000_000; i++)
            sink = hotMapGetDeep();

        for (int i = 0; i < 5_000_000; i++)
            sink = hotMapContainsDeep();

        for (int i = 0; i < 5_000_000; i++)
            sink = hotMapContainsParameter(localDeepPath);

        IO.println(PATHS.indexOf(FIRST_PATH));
        IO.println(PATHS.indexOf(DEEP_PATH));
        IO.println(hotMapGetFirst() == FIRST_PATH);
        IO.println(hotMapGetDeep() == DEEP_PATH);
        IO.println(hotMapContainsDeep());
        IO.println(hotMapContainsParameter(localDeepPath));
    }

    static MemPath hotMapGetFirst() {
        return PATH_MAP.get(FIRST_PATH);
    }

    static MemPath hotMapGetDeep() {
        return PATH_MAP.get(DEEP_PATH);
    }

    static boolean hotMapContainsDeep() {
        return PATH_MAP.get(DEEP_PATH) == DEEP_PATH;
    }

    static boolean hotMapContainsParameter(MemPath path) {
        return PATH_MAP.get(path) == path;
    }

    sealed interface Payload permits HumanPayload, MachinePayload {}

    sealed interface HumanPayload extends Payload {
        record Text(int length, int encoding) implements HumanPayload {}
        record Profile(Name name, Age age) implements HumanPayload {}
    }

    sealed interface MachinePayload extends Payload {
        record Binary(long address, int length) implements MachinePayload {}
        record Sensor(SensorId id, Reading reading) implements MachinePayload {}
    }

    record Name(int firstLength, int lastLength) {}
    record Age(int years) {}

    record SensorId(long value) {}
    record Reading(float value, long timestamp) {}
}
