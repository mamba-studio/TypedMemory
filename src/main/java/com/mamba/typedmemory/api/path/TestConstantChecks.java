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

import com.mamba.typedmemory.api.path.TestConstantChecks.MachinePayload.Sensor;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author joemw
 */
public class TestConstantChecks {

    private static final List<MemPath> PATHS =
        MemPathValidation.discoverResolvablePaths(Payload.class);

    private static final List<MemPath> STABLE_PATHS =
        StableValue.list(PATHS.size(), PATHS::get);

    private static final Map<Integer, Double> SORT_MAP =
        StableValue.map(Set.of(1, 2, 4, 8, 16), i -> StrictMath.sqrt(i));

    private static final MemPath TEXT_LENGTH = MemPath.of(
        Payload.class,
        MachinePayload.class,
        Sensor.class
    );

    private static final MemPath FIRST_PATH = PATHS.getFirst();
    
    private static volatile Object sink;
    
    void main(){
        for (int i = 0; i < 5_000_000; i++)
            sink = hotListContains();

        for (int i = 0; i < 5_000_000; i++)
            sink = hotListContainsFirst();

        for (int i = 0; i < 5_000_000; i++)
            sink = hotLocalPathContains();

        var path = MemPath.of(
            Payload.class,
            MachinePayload.class,
            Sensor.class
        );

        for (int i = 0; i < 5_000_000; i++)
            sink = hotParameterContains(path);

        for (int i = 0; i < 5_000_000; i++)
            sink = hotSortMap();

        IO.println(PATHS.indexOf(TEXT_LENGTH));
        IO.println(hotListContains());
        IO.println(hotListContainsFirst());
        IO.println(hotLocalPathContains());
        IO.println(hotParameterContains(path));
        IO.println(hotSortMap());
    }

    static boolean hotListContains() {
        return STABLE_PATHS.contains(TEXT_LENGTH);
    }

    static boolean hotListContainsFirst() {
        return STABLE_PATHS.contains(FIRST_PATH);
    }

    static boolean hotLocalPathContains() {
        var path = MemPath.of(
            Payload.class,
            MachinePayload.class,
            Sensor.class
        );
        return STABLE_PATHS.contains(path);
    }

    static boolean hotParameterContains(MemPath path) {
        return STABLE_PATHS.contains(path);
    }

    static Double hotSortMap() {
        return SORT_MAP.get(16);
    }
    
    sealed interface Payload permits HumanPayload, MachinePayload {}

    sealed interface HumanPayload extends Payload {
        record Text(int length, int encoding) implements HumanPayload {}
        record Profile(Name name, Age age) implements HumanPayload {}
    }

    sealed interface MachinePayload extends Payload{
        record Binary(long address, int length) implements MachinePayload {}
        record Sensor(SensorId id, Reading reading) implements MachinePayload {}
    }

    record Name(int firstLength, int lastLength) {}
    record Age(int years) {}

    record SensorId(long value) {}
    record Reading(float value, long timestamp) {}
}
