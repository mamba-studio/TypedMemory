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

/**
 *
 * @author joemw
 */
public enum ArrayAccessKind {
    REFERENCE,
    BOOLEAN,
    BYTE,
    SHORT,
    CHAR,
    INT,
    LONG,
    FLOAT,
    DOUBLE;
    
    public static ArrayAccessKind kind(Class<?> elementType) {
        if (!elementType.isPrimitive()) {
            return ArrayAccessKind.REFERENCE;
        }
        if (elementType == boolean.class) return ArrayAccessKind.BOOLEAN;
        if (elementType == byte.class) return ArrayAccessKind.BYTE;
        if (elementType == short.class) return ArrayAccessKind.SHORT;
        if (elementType == char.class) return ArrayAccessKind.CHAR;
        if (elementType == int.class) return ArrayAccessKind.INT;
        if (elementType == long.class) return ArrayAccessKind.LONG;
        if (elementType == float.class) return ArrayAccessKind.FLOAT;
        if (elementType == double.class) return ArrayAccessKind.DOUBLE;
        throw new UnsupportedOperationException("Primitive array kind not supported yet: " + elementType);
    }
}
