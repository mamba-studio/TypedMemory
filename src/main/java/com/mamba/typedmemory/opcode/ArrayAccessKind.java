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
