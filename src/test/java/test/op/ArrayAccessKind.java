/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.op;

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
