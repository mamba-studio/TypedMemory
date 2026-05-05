package accessplan;

import java.lang.foreign.MemorySegment;

public final class PlanReader {
    private PlanReader() {}

    public static Object read(ValuePlan plan, MemorySegment segment, long baseOffset) throws Throwable {
        return switch (plan) {
            case ValuePlan.PrimitivePlan p -> readPrimitive(p, segment, baseOffset);
            case ValuePlan.RecordValuePlan r -> readRecord(r, segment, baseOffset);
            case ValuePlan.ArrayPlan a -> readArray(a, segment, baseOffset);
        };
    }

    private static Object readPrimitive(
            ValuePlan.PrimitivePlan plan,
            MemorySegment segment,
            long baseOffset
    ) {
        return plan.varHandle().get(segment, baseOffset);
    }

    private static Object readRecord(
            ValuePlan.RecordValuePlan plan,
            MemorySegment segment,
            long baseOffset
    ) throws Throwable {
        long recordBase = baseOffset + plan.offset();
        Object[] args = new Object[plan.fields().length];

        for (int i = 0; i < plan.fields().length; i++) {
            var child = plan.fields()[i].valuePlan();
            args[i] = read(child, segment, recordBase);
        }

        return plan.constructor().invokeWithArguments(args);
    }

    private static Object readArray(
        ValuePlan.ArrayPlan plan,
        MemorySegment segment,
        long baseOffset
    ) throws Throwable {
        long arrayBase = baseOffset + plan.offset();
        Class<?> componentType = plan.javaType().getComponentType();
        Object array = java.lang.reflect.Array.newInstance(componentType, plan.length());

        for (int i = 0; i < plan.length(); i++) {
            Object element = switch (plan.elementPlan()) {
                case ValuePlan.PrimitivePlan p ->
                        p.varHandle().get(segment, arrayBase, (long) i);

                case ValuePlan.RecordValuePlan r -> {
                    long elementBase = arrayBase + i * plan.elementStride();
                    yield read(r, segment, elementBase);
                }

                case ValuePlan.ArrayPlan _ ->
                        throw new UnsupportedOperationException("Nested arrays later");
            };

            java.lang.reflect.Array.set(array, i, element);
        }

        return array;
    }



}
