package op;

import java.lang.constant.ClassDesc;

/**
 *
 * @author joemw
 */
public sealed interface Op {
    public record PutStatic(ClassDesc owner, String name, ClassDesc type, Expr value) implements Op {}
    public record Return() implements Op {}
}
