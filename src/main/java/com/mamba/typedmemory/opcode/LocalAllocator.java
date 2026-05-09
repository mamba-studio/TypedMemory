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
package com.mamba.typedmemory.opcode;

import com.mamba.typedmemory.opcode.OpcodeHelper.JVMType;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 *
 * @author joemw
 */
public class LocalAllocator {
    public record LocalBinding(int slot, JVMType kind, String name) {}

    private int nextSlot;
    private final Deque<Integer> marks = new ArrayDeque<>();

    public LocalAllocator(boolean isStatic, MethodTypeDesc methodType) {
        this.nextSlot = computeStartSlot(isStatic, methodType);
    }

    public void enterScope() {
        marks.push(nextSlot);
    }

    public void exitScope() {
        nextSlot = marks.pop();
    }
    
    public static final LocalBinding THIS = new LocalBinding(0, JVMType.REFERENCE, "this");

    public LocalBinding allocate(JVMType kind, String name) {
        int slot = nextSlot;
        nextSlot += width(kind);
        return new LocalBinding(slot, kind, name);
    }
    
    private static int computeStartSlot(boolean isStatic, MethodTypeDesc methodType) {
        int slot = isStatic ? 0 : 1;

        for (int i = 0; i < methodType.parameterCount(); i++) {
            slot += width(methodType.parameterType(i));
        }
        return slot;
    }

    private static int width(JVMType kind) {
        return switch (kind) {
            case LONG, DOUBLE -> 2;
            default -> 1;
        };
    }

    private static int width(ClassDesc type) {
        return switch (type.descriptorString()) {
            case "J", "D" -> 2;
            default -> 1;
        };
    }
}
