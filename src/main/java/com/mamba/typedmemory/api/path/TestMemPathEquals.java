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
public class TestMemPathEquals {

    void main() {
        var text = MemPath.of(
            Packet.class,
            HumanPacket.class,
            HumanPacket.Text.class
        );

        var sameText = MemPath.of(
            Packet.class,
            HumanPacket.class,
            HumanPacket.Text.class
        );

        var profile = MemPath.of(
            Packet.class,
            HumanPacket.class,
            HumanPacket.Profile.class
        );

        var legalName = MemPath.of(
            Packet.class,
            HumanPacket.class,
            HumanPacket.Alias.class,
            "legal"
        );

        var displayName = MemPath.of(
            Packet.class,
            HumanPacket.class,
            HumanPacket.Alias.class,
            "display"
        );

        assertEqual(text, sameText);
        assertNotEqual(text, profile);
        assertNotEqual(legalName, displayName);
    }

    private static void assertEqual(MemPath left, MemPath right) {
        if (!left.equals(right) || left.hashCode() != right.hashCode()) {
            throw new AssertionError("%s should equal %s".formatted(left, right));
        }
    }

    private static void assertNotEqual(MemPath left, MemPath right) {
        if (left.equals(right)) {
            throw new AssertionError("%s should not equal %s".formatted(left, right));
        }
    }

    sealed interface Packet permits HumanPacket, MachinePacket {}

    sealed interface HumanPacket extends Packet {
        record Text(int length, int encoding) implements HumanPacket {}
        record Profile(Name name, Age age) implements HumanPacket {}
        record Alias(Name legal, Name display) implements HumanPacket {}
    }

    sealed interface MachinePacket extends Packet {
        record Binary(long address, int length) implements MachinePacket {}
    }

    record Name(int firstLength, int lastLength) {}
    record Age(int years) {}
}
