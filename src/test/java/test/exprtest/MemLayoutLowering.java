package test.exprtest;

import test.exprtest.expr.arrays.ArrayExpr;
import com.mamba.typedmemory.api.MemLayout;
import com.mamba.typedmemory.internal.ir.IRHelper;
import static com.mamba.typedmemory.internal.ir.IRHelper.CD_MemoryLayout;
import static com.mamba.typedmemory.internal.ir.IRHelper.CD_SequenceLayout;
import static com.mamba.typedmemory.internal.ir.IRHelper.CD_StructLayout;
import static com.mamba.typedmemory.internal.ir.IRHelper.CD_ValueLayout;
import java.lang.constant.ClassDesc;
import static java.lang.constant.ConstantDescs.CD_String;
import java.lang.constant.MethodTypeDesc;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.PaddingLayout;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.util.Optional;
import test.exprtest.expr.Expr;
import test.exprtest.expr.Stmt;
import test.exprtest.expr.arrays.ArrayInitialiserExpr;
import test.exprtest.expr.arrays.NewObjectArrayExpr;
import test.exprtest.expr.fields.GetStaticFieldExpr;
import test.exprtest.expr.methods.InstanceMethodExpr;
import test.exprtest.expr.methods.StaticMethodExpr;
import test.exprtest.expr.values.ConstantExpr;
import test.exprtest.expr.values.IntLiteralExpr;

/**
 *
 * @author joemw
 */
public class MemLayoutLowering {
    private static final MethodTypeDesc MTD_WITH_NAME = MethodTypeDesc.of(CD_MemoryLayout, CD_String);
    private static final MethodTypeDesc MTD_STRUCT_LAYOUT = MethodTypeDesc.of(CD_StructLayout, CD_MemoryLayout.arrayType());
    private static final MethodTypeDesc MTD_PADDING_LAYOUT = MethodTypeDesc.of(CD_MemoryLayout, ClassDesc.ofDescriptor("J"));
    private static final MethodTypeDesc MTD_SEQUENCE_LAYOUT = MethodTypeDesc.of(CD_SequenceLayout, ClassDesc.ofDescriptor("J"), CD_MemoryLayout);
    
    private MemLayoutLowering() {
    }
    
    public static Stmt lower(MemLayout layout, ClassDesc owner) {
        return new Stmt.PutStatic(
                owner,
                "layout",
                CD_MemoryLayout,
                build(layout.layout())
        );
    }
    
    public static Expr build(MemoryLayout layout) {
        return switch (layout) {
            case StructLayout struct -> buildStructLayout(struct);
            case ValueLayout value -> buildValueLayout(value);
            case PaddingLayout padding -> buildPaddingLayout(padding);
            case SequenceLayout sequence -> buildSequenceLayout(sequence);
            default -> throw new UnsupportedOperationException(
                    "Unsupported layout: " + layout
            );
        };
    }
    
    private static Expr buildStructLayout(StructLayout struct) {
        Expr membersArray = new ArrayExpr(
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

        Expr base = new StaticMethodExpr(
                CD_MemoryLayout,
                "structLayout",
                MTD_STRUCT_LAYOUT,
                membersArray
        );

        return maybeWithName(base, struct.name());
    }
    
    private static Expr buildValueLayout(ValueLayout value) {
        Expr base = new GetStaticFieldExpr(
                CD_ValueLayout,
                IRHelper.valueLayoutConstant(value),
                IRHelper.valueLayoutClassDesc(value)
        );

        return maybeWithName(base, value.name());
    }
    
    private static Expr buildPaddingLayout(PaddingLayout padding) {
        Expr base = new StaticMethodExpr(
                CD_MemoryLayout,
                "paddingLayout",
                MTD_PADDING_LAYOUT,
                new ConstantExpr(padding.byteSize())
        );

        return maybeWithName(base, padding.name());
    }
        
    private static Expr buildSequenceLayout(SequenceLayout sequence) {
        Expr base = new StaticMethodExpr(
                CD_MemoryLayout,
                "sequenceLayout",
                MTD_SEQUENCE_LAYOUT,
                new ConstantExpr(sequence.elementCount()),
                build(sequence.elementLayout())
        );

        return maybeWithName(base, sequence.name());
    }
    
    private static Expr maybeWithName(Expr base, Optional<String> name) {
        return name.<Expr>map(n ->
                new InstanceMethodExpr(base, CD_MemoryLayout, "withName", 
                        MTD_WITH_NAME, IRHelper.InvokeKind.INTERFACE, new ConstantExpr(n)))
                .orElse(base);
    }
}
