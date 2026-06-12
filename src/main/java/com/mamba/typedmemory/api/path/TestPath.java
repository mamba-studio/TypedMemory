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

/**
 *
 * @author joemw
 */
public class TestPath {
    void main(){                
        IO.println("Resolved paths");
        var discoveredResolvedPaths = MemPathValidation.discoverResolvablePaths(Packet.class);
        for(var path : discoveredResolvedPaths)
            IO.println(path);
    }
    
    sealed interface Packet permits HumanPacket, MachinePacket, MixedPacket {}

    sealed interface HumanPacket extends Packet {
        // Duplicate primitive fields: should stop at Text.class
        record Text(int length, int encoding) implements HumanPacket {}

        // Name has duplicate primitive fields, but Profile should still stop here
        record Profile(Name name, Age age) implements HumanPacket {}

        // Duplicate non-primitive fields: must disambiguate by field name
        record Alias(Name legal, Name display) implements HumanPacket {}
    }

    sealed interface MachinePacket extends Packet {
        // Mixed primitive types: should stop at Binary.class
        record Binary(long address, int length) implements MachinePacket {}

        // Two distinct record components: should stop at Sensor.class
        record Sensor(SensorId id, Reading reading) implements MachinePacket {}

        // Duplicate same record type: must include field names
        record SensorPair(SensorId primary, SensorId backup) implements MachinePacket {}

        // Duplicate primitive arrays: should stop at Bytes.class
        record Bytes(byte[] payload, byte[] checksum) implements MachinePacket {}
    }

    sealed interface MixedPacket extends Packet {
        // Nested union: should go deeper into Command
        record CommandEnvelope(Command command, Header header) implements MixedPacket {}

        // Duplicate nested union fields: must include "left"/"right"
        record CommandPair(Command left, Command right) implements MixedPacket {}
    }

    sealed interface Command {
        record CreateCommand(Header header, Name owner) implements Command {}

        // Duplicate non-primitive fields inside union child
        record DeleteCommand(Header request, Header audit) implements Command {}
    }

    record Name(int firstLength, int lastLength) {}
    record Age(int years) {}

    record SensorId(long value) {}
    record Reading(float value, long timestamp) {}

    record Header(int version, int flags) {}
}
