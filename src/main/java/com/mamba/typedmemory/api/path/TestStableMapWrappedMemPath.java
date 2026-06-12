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

import com.mamba.typedmemory.api.path.TestStableMapWrappedMemPath.MachinePayload.Sensor;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author joemw
 */
public class TestStableMapWrappedMemPath {

    private static final List<MemPath> PATHS =
        MemPathValidation.discoverResolvablePaths(Payload.class);

    private static final Map<MemPath, MemPath> IDENTITY_MAP =
        StableValue.map(Set.copyOf(PATHS), path -> path);

    private static final Map<MemPath, WrappedPath> WRAPPER_MAP =
        StableValue.map(Set.copyOf(PATHS), WrappedPath::new);

    private static final MemPath DEEP_PATH = MemPath.of(
        Payload.class,
        MachinePayload.class,
        Sensor.class
    );

    private static volatile Object sink;

    void main() {
        IDENTITY_MAP.get(DEEP_PATH);
        WRAPPER_MAP.get(DEEP_PATH);

        for (int i = 0; i < 5_000_000; i++)
            sink = hotIdentityGet();

        for (int i = 0; i < 5_000_000; i++)
            sink = hotIdentityContains();

        for (int i = 0; i < 5_000_000; i++)
            sink = hotWrapperGet();

        for (int i = 0; i < 5_000_000; i++)
            sink = hotWrapperContains();

        IO.println(PATHS.indexOf(DEEP_PATH));
        IO.println(hotIdentityGet() == DEEP_PATH);
        IO.println(hotIdentityContains());
        IO.println(hotWrapperGet().path() == DEEP_PATH);
        IO.println(hotWrapperContains());
    }

    static MemPath hotIdentityGet() {
        return IDENTITY_MAP.get(DEEP_PATH);
    }

    static boolean hotIdentityContains() {
        return IDENTITY_MAP.get(DEEP_PATH) == DEEP_PATH;
    }

    static WrappedPath hotWrapperGet() {
        return WRAPPER_MAP.get(DEEP_PATH);
    }

    static boolean hotWrapperContains() {
        return WRAPPER_MAP.get(DEEP_PATH).path() == DEEP_PATH;
    }

    record WrappedPath(MemPath path) {}

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
