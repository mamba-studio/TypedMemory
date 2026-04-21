package test.accessplan;

import java.util.Arrays;

public final class PlanPrinter {
    private PlanPrinter() {}

    public static String print(ValuePlan plan) {
        StringBuilder sb = new StringBuilder();
        append(plan, sb, 0);
        return sb.toString();
    }

    private static void append(ValuePlan plan, StringBuilder sb, int indent) {
        String pad = "  ".repeat(indent);

        switch (plan) {
            case ValuePlan.PrimitivePlan p -> sb.append(pad)
                    .append("PrimitivePlan(")
                    .append("name=").append(p.name())
                    .append(", type=").append(p.javaType().getSimpleName())
                    .append(", offset=").append(p.offset())
                    .append(", byteSize=").append(p.byteSize())
                    .append(")\n");

            case ValuePlan.RecordValuePlan r -> {
                sb.append(pad)
                        .append("RecordValuePlan(")
                        .append("name=").append(r.name())
                        .append(", type=").append(r.javaType().getSimpleName())
                        .append(", offset=").append(r.offset())
                        .append(", byteSize=").append(r.byteSize())
                        .append(")\n");

                Arrays.stream(r.fields())
                        .forEach(f -> append(f.valuePlan(), sb, indent + 1));
            }

            case ValuePlan.ArrayPlan a -> {
                sb.append(pad)
                        .append("ArrayPlan(")
                        .append("name=").append(a.name())
                        .append(", type=").append(a.javaType().getSimpleName())
                        .append(", offset=").append(a.offset())
                        .append(", byteSize=").append(a.byteSize())
                        .append(", length=").append(a.length())
                        .append(", stride=").append(a.elementStride())
                        .append(")\n");

                append(a.elementPlan(), sb, indent + 1);
            }

            default -> throw new IllegalStateException("Unexpected plan: " + plan);
        }
    }
}
