/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.mir;

import java.lang.constant.ClassDesc;
import java.lang.foreign.AddressLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

/**
 *
 * @author joemw
 */
public class Helper {
    public static final ClassDesc CD_MemoryLayout   = ClassDesc.of(MemoryLayout.class.getName());
    public static final ClassDesc CD_StructLayout   = ClassDesc.of(StructLayout.class.getName());
    public static final ClassDesc CD_ValueLayout    = ClassDesc.of(ValueLayout.class.getName());
    public static final ClassDesc CD_PathElement    = ClassDesc.of(PathElement.class.getName());
    public static final ClassDesc CD_VarHandle      = ClassDesc.of(VarHandle.class.getName());

    
    public static String valueLayoutConstant(ValueLayout v) {
        return switch (v) {
            case ValueLayout.OfByte     _ -> "JAVA_BYTE";
            case ValueLayout.OfShort    _ -> "JAVA_SHORT";
            case ValueLayout.OfInt      _ -> "JAVA_INT";
            case ValueLayout.OfLong     _ -> "JAVA_LONG";
            case ValueLayout.OfFloat    _ -> "JAVA_FLOAT";
            case ValueLayout.OfDouble   _ -> "JAVA_DOUBLE";
            case ValueLayout.OfChar     _ -> "JAVA_CHAR";
            case ValueLayout.OfBoolean  _ -> "JAVA_BOOLEAN";
            case AddressLayout _          -> "ADDRESS";
        };
    }
    
    public static ClassDesc valueLayoutClassDesc(ValueLayout v) {
        return switch (v) {
            case ValueLayout.OfByte     _ -> ClassDesc.of("java.lang.foreign.ValueLayout$OfByte");
            case ValueLayout.OfShort    _ -> ClassDesc.of("java.lang.foreign.ValueLayout$OfShort");
            case ValueLayout.OfInt      _ -> ClassDesc.of("java.lang.foreign.ValueLayout$OfInt");
            case ValueLayout.OfLong     _ -> ClassDesc.of("java.lang.foreign.ValueLayout$OfLong");
            case ValueLayout.OfFloat    _ -> ClassDesc.of("java.lang.foreign.ValueLayout$OfFloat");
            case ValueLayout.OfDouble   _ -> ClassDesc.of("java.lang.foreign.ValueLayout$OfDouble");
            case ValueLayout.OfChar     _ -> ClassDesc.of("java.lang.foreign.ValueLayout$OfChar");
            case ValueLayout.OfBoolean  _ -> ClassDesc.of("java.lang.foreign.ValueLayout$OfBoolean");
            case AddressLayout          _ -> ClassDesc.of("java.lang.foreign.AddressLayout");
        };
    }
}
