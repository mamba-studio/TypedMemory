package com.mamba.typedmemory.opcode;

/**
 *
 * @author joemw
 */
public enum ArrayAccessKind {
    REFERENCE,
    INT;
    
    public static ArrayAccessKind kind(Class<?> elementType) {
        if (!elementType.isPrimitive()) {
            return ArrayAccessKind.REFERENCE;
        }
        if (elementType == int.class) {
            return ArrayAccessKind.INT;
        }
        throw new UnsupportedOperationException("Primitive array kind not supported yet: " + elementType);
    }
}
