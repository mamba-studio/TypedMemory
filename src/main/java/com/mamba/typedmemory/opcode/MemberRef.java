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

import java.lang.constant.ClassDesc;
import static java.lang.constant.ConstantDescs.INIT_NAME;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.RecordComponent;

///
/// @author joemw
public sealed interface MemberRef {
    public record FieldRef(ClassDesc owner, String name, ClassDesc type) implements MemberRef {}
    public record MethodRef(ClassDesc owner, String name, MethodTypeDesc type) implements MemberRef {              
        public static MethodRef recordComponentMethodRef(RecordComponent component) {
            return new MethodRef(
                    ClassDesc.ofDescriptor(component.getDeclaringRecord().descriptorString()),
                    component.getName(),
                    MethodTypeDesc.of(ClassDesc.ofDescriptor(component.getType().descriptorString()))
            );
        }
    }
    public record ConstructorRef(ClassDesc owner, MethodTypeDesc type) implements MemberRef {
        public String name() {return INIT_NAME;}
    }

    public ClassDesc owner();
}
