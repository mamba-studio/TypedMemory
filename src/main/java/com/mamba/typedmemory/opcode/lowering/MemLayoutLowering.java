package com.mamba.typedmemory.opcode.lowering;


import com.mamba.typedmemory.api.MemLayout;
import com.mamba.typedmemory.opcode.OpcodeHelper;
import static com.mamba.typedmemory.opcode.OpcodeHelper.CD_MemoryLayout;
import static com.mamba.typedmemory.opcode.OpcodeHelper.CD_SequenceLayout;
import static com.mamba.typedmemory.opcode.OpcodeHelper.CD_StructLayout;
import static com.mamba.typedmemory.opcode.OpcodeHelper.CD_ValueLayout;
import com.mamba.typedmemory.opcode.MemberRef.FieldRef;
import com.mamba.typedmemory.opcode.MemberRef.MethodRef;
import com.mamba.typedmemory.opcode.expr.Expr;
import com.mamba.typedmemory.opcode.expr.arrays.ArrayExpr;
import com.mamba.typedmemory.opcode.expr.arrays.ArrayInitialiserExpr;
import com.mamba.typedmemory.opcode.expr.arrays.NewObjectArrayExpr;
import com.mamba.typedmemory.opcode.expr.values.ConstantExpr;
import com.mamba.typedmemory.opcode.expr.values.IntExpr.IntLiteralExpr;
import com.mamba.typedmemory.opcode.fields.GetStaticFieldExpr;
import com.mamba.typedmemory.opcode.methods.InstanceMethodExpr;
import com.mamba.typedmemory.opcode.methods.StaticMethodExpr;
import com.mamba.typedmemory.opcode.stmt.PutStaticStmt;
import com.mamba.typedmemory.opcode.stmt.Stmt;
import java.lang.constant.ClassDesc;
import static java.lang.constant.ConstantDescs.CD_String;
import static java.lang.constant.ConstantDescs.CD_long;
import java.lang.constant.MethodTypeDesc;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.PaddingLayout;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.util.Optional;

/**
 *
 * @author joemw
 */
public class MemLayoutLowering {
    private static final MethodTypeDesc MTD_WITH_NAME = MethodTypeDesc.of(CD_MemoryLayout, CD_String);
    private static final MethodTypeDesc MTD_STRUCT_LAYOUT = MethodTypeDesc.of(CD_StructLayout, CD_MemoryLayout.arrayType());
    private static final MethodTypeDesc MTD_PADDING_LAYOUT = MethodTypeDesc.of(CD_MemoryLayout, CD_long);
    private static final MethodTypeDesc MTD_SEQUENCE_LAYOUT = MethodTypeDesc.of(CD_SequenceLayout, CD_long, CD_MemoryLayout);
    
    private MemLayoutLowering() {}
    
    public static Stmt lower(MemLayout layout, ClassDesc owner) {
        return new PutStaticStmt(
                new FieldRef(owner, "layout", CD_MemoryLayout), build(layout.layout()));
    }
    
    public static Expr build(MemoryLayout layout) {
        return switch (layout) {
            case StructLayout struct -> buildStructLayout(struct);
            case ValueLayout value -> buildValueLayout(value);
            case PaddingLayout padding -> buildPaddingLayout(padding);
            case SequenceLayout sequence -> buildSequenceLayout(sequence);
            default -> throw new UnsupportedOperationException("Unsupported layout: " + layout);
        };
    }
    
    private static Expr buildStructLayout(StructLayout struct) {
        var membersArray = new ArrayExpr(
                new NewObjectArrayExpr(
                        CD_MemoryLayout,
                        new IntLiteralExpr(struct.memberLayouts().size())
                ),
                new ArrayInitialiserExpr(
                        struct.memberLayouts()
                                .stream()
                                .map(MemLayoutLowering::build)
                                .toList()
                )
        );
        var base = new StaticMethodExpr(
                new MethodRef(CD_MemoryLayout, "structLayout", MTD_STRUCT_LAYOUT),
                true, membersArray);
        return maybeWithName(base, struct.name());
    }
    
    private static Expr buildValueLayout(ValueLayout value) {
        Expr base = new GetStaticFieldExpr(
                new FieldRef(CD_ValueLayout,OpcodeHelper.valueLayoutConstant(value), OpcodeHelper.valueLayoutClassDesc(value)));
        return maybeWithName(base, value.name());
    }
    
    private static Expr buildPaddingLayout(PaddingLayout padding) {
        Expr base = new StaticMethodExpr(
                new MethodRef(CD_MemoryLayout, "paddingLayout", MTD_PADDING_LAYOUT), true, new ConstantExpr(padding.byteSize()));
        return maybeWithName(base, padding.name());
    }
        
    private static Expr buildSequenceLayout(SequenceLayout sequence) {
        Expr base = new StaticMethodExpr(
                new MethodRef(CD_MemoryLayout, "sequenceLayout", MTD_SEQUENCE_LAYOUT), true, 
                new ConstantExpr(sequence.elementCount()), build(sequence.elementLayout()));

        return maybeWithName(base, sequence.name());
    }
    
    private static Expr maybeWithName(Expr base, Optional<String> name) {
        return name.<Expr>map(name_ ->
                new InstanceMethodExpr(
                        base, 
                        new MethodRef(CD_MemoryLayout, "withName", MTD_WITH_NAME), 
                        OpcodeHelper.InvokeKind.INTERFACE, new ConstantExpr(name_)))
                .orElse(base);
    }
}
