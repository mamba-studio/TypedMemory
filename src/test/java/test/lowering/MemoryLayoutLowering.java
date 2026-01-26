/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.lowering;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import static java.lang.constant.ConstantDescs.CD_String;
import java.lang.constant.MethodTypeDesc;
import java.lang.foreign.AddressLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.PaddingLayout;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.UnionLayout;
import java.lang.foreign.ValueLayout;
import java.util.Optional;
import test.ir.Expr;
import test.ir.Expr.BaseExpr;
import test.ir.Expr.CompositeExpr;

/**
 *
 * @author joemw
 */
public class MemoryLayoutLowering {    
    private static final ClassDesc CD_MemoryLayout         = ClassDesc.of(MemoryLayout.class.getName());
    private static final ClassDesc CD_StructLayout         = ClassDesc.of("java.lang.foreign.StructLayout");
    private static final ClassDesc CD_ValueLayout          = ClassDesc.of("java.lang.foreign.ValueLayout");
    
    public static Expr ofExpr(MemoryLayout memoryLayout) {
        return switch (memoryLayout) {

            /* ---------------- Group layouts ---------------- */

            case StructLayout struct -> {
                Expr base =
                    new CompositeExpr.Call(
                        Expr.InvokeKind.STATIC,
                        CD_MemoryLayout,
                        "structLayout",
                        MethodTypeDesc.of(
                            CD_StructLayout,
                            CD_MemoryLayout.arrayType()
                        ),
                        new CompositeExpr.ArrayLiteral(
                            CD_MemoryLayout,
                            struct.memberLayouts()
                                  .stream()
                                  .map(m -> ofExpr(m))
                                  .toList()
                        )
                    );

                yield maybeWithName(
                    base,
                    struct.name()
                );
            }

            case UnionLayout union -> {
                Expr base =
                    new CompositeExpr.Call(
                        Expr.InvokeKind.STATIC,
                        CD_MemoryLayout,
                        "unionLayout",
                        MethodTypeDesc.of(
                            CD_StructLayout,
                            CD_MemoryLayout.arrayType()
                        ),
                        new CompositeExpr.ArrayLiteral(
                            CD_MemoryLayout,
                            union.memberLayouts()
                                 .stream()
                                 .map(m -> ofExpr(m))
                                 .toList()
                        )
                    );

                yield maybeWithName(
                    base,
                    union.name()
                );
            }

            /* ---------------- Sequence ---------------- */

            case SequenceLayout seq -> {
                Expr base =
                    new CompositeExpr.Call(
                        Expr.InvokeKind.STATIC,
                        CD_MemoryLayout,
                        "sequenceLayout",
                        MethodTypeDesc.of(
                            CD_MemoryLayout,
                            ConstantDescs.CD_long,
                            CD_MemoryLayout
                        ),
                        new BaseExpr.LongLiteral(seq.elementCount()),
                        ofExpr(seq.elementLayout())
                    );

                yield maybeWithName(
                    base,
                    seq.name()
                );
            }

            /* ---------------- Value ---------------- */

            case ValueLayout value -> {
                Expr base =
                    new BaseExpr.GetStatic(
                        CD_ValueLayout,
                        valueLayoutConstant(value),   // e.g. "JAVA_BYTE"
                        valueLayoutClassDesc(value)    
                    );

                yield maybeWithName(
                    base,
                    value.name()
                );
            }

            /* ---------------- Padding ---------------- */

            case PaddingLayout padding ->
                new CompositeExpr.Call(
                    Expr.InvokeKind.STATIC,
                    CD_MemoryLayout,
                    "paddingLayout",
                    MethodTypeDesc.of(
                        CD_MemoryLayout,
                        ConstantDescs.CD_long
                    ),
                    new BaseExpr.LongLiteral(padding.byteSize())
                );
        };
    }

    
    private static Expr maybeWithName(Expr base, Optional<String> name) {
        return name.isPresent()
            ? new CompositeExpr.Call(
                        Expr.InvokeKind.INTERFACE,
                        CD_MemoryLayout,
                        "withName",
                        MethodTypeDesc.of(CD_MemoryLayout, CD_String),
                        base,
                        new BaseExpr.StringLiteral(name.get())
                    )
            : base;
    }
    
    private static String valueLayoutConstant(ValueLayout v) {
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
    
    private static ClassDesc valueLayoutClassDesc(ValueLayout v) {
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
