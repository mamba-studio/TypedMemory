package test.exprtest;

import com.mamba.typedmemory.api.MemLayout;
import static com.mamba.typedmemory.internal.ir.IRHelper.CD_MemoryLayout;
import com.mamba.typedmemory.internal.ir.IRHelper.JVMType;
import com.mamba.typedmemory.internal.ir.Stmt;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.StructLayout;
import java.util.ArrayList;
import java.util.List;
import test.exprtest.LocalAllocator.LocalBinding;
import test.exprtest.expr.Expr;
import test.exprtest.expr.arrays.ArrayStoreStmt;
import test.exprtest.expr.arrays.NewArrayExpr;
import test.exprtest.expr.bind.BlockExpr;
import test.exprtest.expr.bind.LetExpr;
import test.exprtest.expr.methods.StaticMethodExpr;
import test.exprtest.expr.values.IntLiteralExpr;
import test.exprtest.expr.values.LocalExpr;

/**
 *
 * @author joemw
 */
public class MemLayoutLowering {
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
    
    private static Expr buildStructLayout(StructLayout struct, LocalAllocator locals) {
        var arrayLocal = locals.allocate(JVMType.REFERENCE).named("members");

        var stores = new ArrayList<Stmt>();
        var members = struct.memberLayouts();

        for (int i = 0; i < members.size(); i++) {
            stores.add(new ArrayStoreStmt(
                    new LocalExpr(arrayLocal),
                    new IntLiteralExpr(i),
                    build(members.get(i), locals)
            ));
        }
        
        var membersArray = new LetExpr(
                arrayLocal,
                new NewArrayExpr(CD_MemoryLayout, new IntLiteralExpr(members.size())),
                new BlockExpr(
                        stores,
                        new LocalExpr(arrayLocal)
                )
        );

        var base = new StaticMethodExpr(
                CD_MemoryLayout,
                "structLayout",
                MethodTypeDesc.of(CD_MemoryLayout, CD_MemoryLayout.arrayType()),
                membersArray);

        return struct.name()
                .<Expr>map(n -> new WithNameExpr(base, n, CD_MemoryLayout))
                .orElse(base);
    }
}
