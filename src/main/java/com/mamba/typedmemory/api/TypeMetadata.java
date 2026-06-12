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
package com.mamba.typedmemory.api;

import com.mamba.typedmemory.opcode.Generator;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 *
 * @author joemw
 */
final class TypeMetadata {
    private final Class<? extends Record> type;
    private final MemLayout layout;    
    private volatile MethodHandle constructor; //To replace with future lazyconstants

    TypeMetadata(Class<? extends Record> type, MemLayout layout) {
        this.type = type;
        this.layout = layout;
    }

    MemLayout layout() {
        return layout;
    }

    MethodHandle constructor(MethodHandles.Lookup lookup) throws Throwable {
        // Still enforce access for this caller.
        var privateLookup = MethodHandles.privateLookupIn(type, lookup);

        MethodHandle existing = constructor;
        if (existing != null) {
            return existing;
        }

        synchronized (this) {
            if (constructor == null) {
                constructor = defineConstructor(privateLookup);
            }
            return constructor;
        }
    }

    private MethodHandle defineConstructor(MethodHandles.Lookup privateLookup) throws Throwable {
        var owner = Generator.generateUserImplName(type);
        byte[] bytes = Generator.generate(owner, type, layout);

        var hiddenLookup = privateLookup.defineHiddenClass(
                bytes,
                true,
                MethodHandles.Lookup.ClassOption.NESTMATE
        );

        var hiddenClass = hiddenLookup.lookupClass();

        return hiddenLookup.findConstructor(
                hiddenClass,
                MethodType.methodType(void.class, MemorySegment.class)
        ).asType(MethodType.methodType(Mem.class, MemorySegment.class));
    }
}
