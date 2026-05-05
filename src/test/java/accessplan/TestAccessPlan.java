package accessplan;

import com.mamba.typedmemory.api.MemLayout;
import com.mamba.typedmemory.api.size;
import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;

public class TestAccessPlan {
    record Pixel(int i, int j) {}
    record Point(Pixel p, int z) {}

    void main() throws Throwable {
    record Pixel(int i, int j) {}
    record Point(@size(2) Pixel[] pixels, int z) {}

    var layout = MemLayout.of(Point.class).layout();
    var plan = build(Point.class, layout);

    IO.println(PlanPrinter.print(plan));

    try (Arena arena = Arena.ofConfined()) {
        MemorySegment segment = arena.allocate(layout, 1);

        var iHandle = layout.varHandle(
                MemoryLayout.PathElement.groupElement("pixels"),
                MemoryLayout.PathElement.sequenceElement(),
                MemoryLayout.PathElement.groupElement("i")
        );

        var jHandle = layout.varHandle(
                MemoryLayout.PathElement.groupElement("pixels"),
                MemoryLayout.PathElement.sequenceElement(),
                MemoryLayout.PathElement.groupElement("j")
        );

        iHandle.set(segment, 0L, 0L, 10);
        jHandle.set(segment, 0L, 0L, 20);

        iHandle.set(segment, 0L, 1L, 30);
        jHandle.set(segment, 0L, 1L, 40);

        layout.varHandle(
                MemoryLayout.PathElement.groupElement("z")
        ).set(segment, 0L, 99);

        Point p = (Point) PlanReader.read(plan, segment, 0L);

        IO.println("pixels[0] = " + p.pixels()[0]);
        IO.println("pixels[1] = " + p.pixels()[1]);
        IO.println("z = " + p.z());
        IO.println("Point[pixels=" + java.util.Arrays.toString(p.pixels()) + ", z=" + p.z() + "]");
    }
}


    @SuppressWarnings("unchecked")
    static ValuePlan.RecordValuePlan build(Class<? extends Record> type, MemoryLayout layout) throws Throwable {
        var lookup = MethodHandles.lookup();
        var components = type.getRecordComponents();
        var fields = new ValuePlan.FieldPlan[components.length];
        var ctorTypes = new Class<?>[components.length];

        for (int i = 0; i < components.length; i++) {
            var c = components[i];
            ctorTypes[i] = c.getType();

            long offset = layout.byteOffset(
                    MemoryLayout.PathElement.groupElement(c.getName())
            );

            var fieldLayout = layout.select(
                    MemoryLayout.PathElement.groupElement(c.getName())
            );

            ValuePlan childPlan;

            if (c.getType().isArray()) {
                childPlan = buildArrayPlan(c, fieldLayout, offset);
            } else if (c.getType().isRecord()) {
                childPlan = buildNestedRecord(c.getName(), c.getType(), fieldLayout, offset);
            } else {
                VarHandle vh = layout.varHandle(
                        MemoryLayout.PathElement.groupElement(c.getName())
                );

                childPlan = new ValuePlan.PrimitivePlan(
                        c.getName(),
                        c.getType(),
                        offset,
                        fieldLayout.byteSize(),
                        vh
                );
            }

            fields[i] = new ValuePlan.FieldPlan(childPlan);
        }

        var ctor = lookup.findConstructor(
                type,
                MethodType.methodType(void.class, ctorTypes)
        );

        return new ValuePlan.RecordValuePlan(
                type.getSimpleName(),
                type,
                0L,
                layout.byteSize(),
                ctor,
                fields
        );
    }

    @SuppressWarnings("unchecked")
    static ValuePlan.RecordValuePlan buildNestedRecord(
            String fieldName,
            Class<?> nestedType,
            MemoryLayout nestedLayout,
            long offset
    ) throws Throwable {
        var lookup = MethodHandles.lookup();
        var recordType = (Class<? extends Record>) nestedType;
        var components = recordType.getRecordComponents();
        var fields = new ValuePlan.FieldPlan[components.length];
        var ctorTypes = new Class<?>[components.length];

        for (int i = 0; i < components.length; i++) {
            var c = components[i];
            ctorTypes[i] = c.getType();

            long childOffset = nestedLayout.byteOffset(
                    MemoryLayout.PathElement.groupElement(c.getName())
            );

            var fieldLayout = nestedLayout.select(
                    MemoryLayout.PathElement.groupElement(c.getName())
            );

            ValuePlan childPlan;

            if (c.getType().isArray()) {
                childPlan = buildArrayPlan(c, fieldLayout, childOffset);
            } else if (c.getType().isRecord()) {
                childPlan = buildNestedRecord(c.getName(), c.getType(), fieldLayout, childOffset);
            } else {
                VarHandle vh = nestedLayout.varHandle(
                        MemoryLayout.PathElement.groupElement(c.getName())
                );

                childPlan = new ValuePlan.PrimitivePlan(
                        c.getName(),
                        c.getType(),
                        childOffset,
                        fieldLayout.byteSize(),
                        vh
                );
            }

            fields[i] = new ValuePlan.FieldPlan(childPlan);
        }

        var ctor = lookup.findConstructor(
                recordType,
                MethodType.methodType(void.class, ctorTypes)
        );

        return new ValuePlan.RecordValuePlan(
                fieldName,
                recordType,
                offset,
                nestedLayout.byteSize(),
                ctor,
                fields
        );
    }
    
    static ValuePlan.ArrayPlan buildArrayPlan(
        java.lang.reflect.RecordComponent c,
        MemoryLayout fieldLayout,
        long offset
    ) throws Throwable {
        if (!(fieldLayout instanceof java.lang.foreign.SequenceLayout seq)) {
            throw new IllegalArgumentException("Expected SequenceLayout for array component: " + c.getName());
        }

        var arrayType = c.getType();
        var componentType = arrayType.getComponentType();

        var sizeAnn = c.getAnnotation(com.mamba.typedmemory.api.size.class);
        if (sizeAnn == null) {
            throw new IllegalArgumentException("Missing @size on array component: " + c.getName());
        }

        int length = sizeAnn.value();
        if (length <= 0) {
            throw new IllegalArgumentException("@size must be > 0 for component: " + c.getName());
        }

        long elementStride = seq.elementLayout().byteSize();
        ValuePlan elementPlan;

        if (componentType.isRecord()) {
            elementPlan = buildNestedRecord(
                    c.getName() + "Element",
                    componentType,
                    seq.elementLayout(),
                    0L
            );
        } else {
            VarHandle vh = seq.varHandle(
                    MemoryLayout.PathElement.sequenceElement()
            );

            elementPlan = new ValuePlan.PrimitivePlan(
                    c.getName() + "Element",
                    componentType,
                    0L,
                    elementStride,
                    vh
            );
        }

        return new ValuePlan.ArrayPlan(
                c.getName(),
                arrayType,
                offset,
                fieldLayout.byteSize(),
                length,
                elementStride,
                elementPlan
        );
    }
}
