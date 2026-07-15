

import com.mamba.typedmemory.api.Mem;
import com.mamba.typedmemory.api.MemLayout;
import com.mamba.typedmemory.api.Nulls;
import com.mamba.typedmemory.api.Ptr;
import com.mamba.typedmemory.api.RawMem;
import com.mamba.typedmemory.api.size;
import com.mamba.typedmemory.util.MemLayoutString.SummaryStyle;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public class TestMemScenarios {
    record AllPrimitives(
            boolean boolValue,
            byte byteValue,
            short shortValue,
            char charValue,
            int intValue,
            long longValue,
            float floatValue,
            double doubleValue) {}

    record Pixel(short x, short y) {}
    record PointerRecord(Ptr opaque, RawMem<Pixel> pixels) {}
    record RecursiveNode(int value, RawMem<RecursiveNode> next) {}
    @SuppressWarnings("rawtypes")
    record RawRawMemField(RawMem value) {}
    record WildcardRawMemField(RawMem<?> value) {}
    record GenericRawMemField<T>(RawMem<T> value) {}
    record CountedMemField(Mem<Pixel> value) {}
    record PtrArrayField(@size(2) Ptr[] values) {}
    record RawMemArrayField(@size(2) RawMem<Pixel>[] values) {}
    record LayoutPixel(int x, @size(2) double[] y) {}
    record LayoutPoint(char c, LayoutPixel pixel) {}
    record LayoutPixelArrayPoint(char c, @size(4) LayoutPixel[] pixels) {}
    record LayoutRecordNameThatIsLongEnoughToStretchTypeColumn(int x) {}
    record LayoutWithLongSummaryNames(LayoutRecordNameThatIsLongEnoughToStretchTypeColumn nestedRecordComponentNameThatIsAlsoLongEnough) {}
    record Color(float r, float g, float b, Pixel pixel) {}
    record PrimitiveArrays(@size(4) int[] ints, @size(3) float[] floats, @size(2) short[] shorts) {}
    record AllPrimitiveArrays(
            @size(2) boolean[] booleans,
            @size(2) byte[] bytes,
            @size(2) short[] shorts,
            @size(2) char[] chars,
            @size(2) int[] ints,
            @size(2) long[] longs,
            @size(2) float[] floats,
            @size(2) double[] doubles) {}
    record RecordArray(@size(3) Pixel[] pixels) {}
    record NestedWithArrays(Pixel origin, PrimitiveArrays samples, RecordArray palette) {}

    public static void main(String[] args) {
        try (var arena = Arena.ofConfined()) {
            testPrimitiveFields(arena);
            testNestedRecords(arena);
            testPrimitiveArrays(arena);
            testAllPrimitiveArrayKinds(arena);
            testRecordArrays(arena);
            testNestedRecordsWithArrays(arena);
            testMultipleElements(arena);
            testArrayLengthValidation(arena);
            testMemConvenienceMethods(arena);
            testIndexValidation(arena);
            testWrap(arena);
            testMemoryReferenceFactories(arena);
            testPointerRecordFields(arena);
            testPointerSchemaValidation();
            testHeapSegmentRejected();
            testReinterpret(arena);
            testCopyAndSwap(arena);
            testLayoutText();
        }

        IO.println("TestMemScenarios passed");
    }

    static void testPrimitiveFields(Arena arena) {
        var mem = Mem.of(AllPrimitives.class, arena, 2);
        var expected = new AllPrimitives(
                true,
                (byte) 7,
                (short) 32000,
                'Z',
                123456,
                9_000_000_000L,
                0.25f,
                0.125d);

        mem.set(0, expected);
        assertEquals(expected, mem.get(0), "all primitive fields round-trip");
    }

    static void testNestedRecords(Arena arena) {
        var mem = Mem.of(Color.class, arena, 2);
        var expected = new Color(0.5f, 0.25f, 0.75f, new Pixel((short) 4, (short) 5));

        mem.set(0, expected);
        assertEquals(expected, mem.get(0), "nested record round-trip");
    }

    static void testPrimitiveArrays(Arena arena) {
        var mem = Mem.of(PrimitiveArrays.class, arena, 2);
        var ints = new int[] {1, 2, 3, 4};
        var floats = new float[] {0.25f, 0.5f, 0.75f};
        var shorts = new short[] {(short) 8, (short) 13};

        mem.set(0, new PrimitiveArrays(ints, floats, shorts));
        ints[0] = 99;
        floats[1] = 99f;
        shorts[1] = 99;

        assertPrimitiveArrays(new PrimitiveArrays(
                new int[] {1, 2, 3, 4},
                new float[] {0.25f, 0.5f, 0.75f},
                new short[] {(short) 8, (short) 13}),
                mem.get(0),
                "primitive arrays round-trip");

        var read = mem.get(0);
        read.ints()[0] = 44;
        read.floats()[0] = 44f;
        read.shorts()[0] = 44;
        assertPrimitiveArrays(new PrimitiveArrays(
                new int[] {1, 2, 3, 4},
                new float[] {0.25f, 0.5f, 0.75f},
                new short[] {(short) 8, (short) 13}),
                mem.get(0),
                "get returns array copies");
    }

    static void testAllPrimitiveArrayKinds(Arena arena) {
        var mem = Mem.of(AllPrimitiveArrays.class, arena, 2);
        var expected = new AllPrimitiveArrays(
                new boolean[] {true, false},
                new byte[] {(byte) 1, (byte) -2},
                new short[] {(short) 300, (short) -400},
                new char[] {'a', 'Z'},
                new int[] {1000, -2000},
                new long[] {3_000_000_000L, -4_000_000_000L},
                new float[] {1.25f, -2.5f},
                new double[] {3.5d, -4.75d});

        mem.set(0, expected);
        assertAllPrimitiveArrays(expected, mem.get(0), "all primitive array kinds round-trip");
    }

    static void testRecordArrays(Arena arena) {
        var mem = Mem.of(RecordArray.class, arena, 2);
        var pixels = new Pixel[] {
            new Pixel((short) 1, (short) 2),
            new Pixel((short) 3, (short) 4),
            new Pixel((short) 5, (short) 6)
        };

        mem.set(0, new RecordArray(pixels));
        pixels[0] = new Pixel((short) 99, (short) 99);

        assertRecordArray(new RecordArray(new Pixel[] {
            new Pixel((short) 1, (short) 2),
            new Pixel((short) 3, (short) 4),
            new Pixel((short) 5, (short) 6)
        }), mem.get(0), "record array round-trip");

        var read = mem.get(0);
        read.pixels()[1] = new Pixel((short) 77, (short) 77);
        assertRecordArray(new RecordArray(new Pixel[] {
            new Pixel((short) 1, (short) 2),
            new Pixel((short) 3, (short) 4),
            new Pixel((short) 5, (short) 6)
        }), mem.get(0), "get returns record array copies");
    }

    static void testNestedRecordsWithArrays(Arena arena) {
        var mem = Mem.of(NestedWithArrays.class, arena, 2);
        var expected = new NestedWithArrays(
                new Pixel((short) 10, (short) 20),
                new PrimitiveArrays(
                        new int[] {10, 20, 30, 40},
                        new float[] {1f, 2f, 3f},
                        new short[] {(short) 11, (short) 12}),
                new RecordArray(new Pixel[] {
                    new Pixel((short) 7, (short) 8),
                    new Pixel((short) 9, (short) 10),
                    new Pixel((short) 11, (short) 12)
                }));

        mem.set(0, expected);
        assertNestedWithArrays(expected, mem.get(0), "nested record with arrays round-trip");
    }

    static void testMultipleElements(Arena arena) {
        var mem = Mem.of(Color.class, arena, 3);
        var first = new Color(1f, 0f, 0f, new Pixel((short) 1, (short) 2));
        var second = new Color(0f, 1f, 0f, new Pixel((short) 3, (short) 4));

        mem.set(0, first);
        mem.set(1, second);

        assertEquals(first, mem.get(0), "first element remains isolated");
        assertEquals(second, mem.get(1), "second element remains isolated");
        assertEquals(3L, mem.size(), "mem size");
        assertEquals(Color.class, mem.type(), "mem type");
    }

    static void testArrayLengthValidation(Arena arena) {
        var mem = Mem.of(PrimitiveArrays.class, arena, 1);

        assertThrows(IllegalArgumentException.class,
                () -> mem.set(0, new PrimitiveArrays(new int[] {1}, new float[] {1f, 2f, 3f}, new short[] {(short) 1, (short) 2})),
                "short int array rejected");
        assertThrows(NullPointerException.class,
                () -> mem.set(0, new PrimitiveArrays(null, new float[] {1f, 2f, 3f}, new short[] {(short) 1, (short) 2})),
                "null array rejected");
    }

    static void testMemConvenienceMethods(Arena arena) {
        var mem = Mem.of(Pixel.class, arena, 4);
        var fillValue = new Pixel((short) 1, (short) 2);

        assertSame(mem, mem.fill(fillValue), "fill returns receiver");
        for (long i = 0; i < mem.size(); i++) {
            assertEquals(fillValue, mem.get(i), "fill value at " + i);
        }

        var supplied = new Pixel((short) 3, (short) 4);
        assertSame(mem, mem.init(() -> supplied), "init returns receiver");
        for (long i = 0; i < mem.size(); i++) {
            assertEquals(supplied, mem.get(i), "init value at " + i);
        }

        assertSame(mem, mem.initIndexed(i -> new Pixel((short) i, (short) (i + 10))), "initIndexed returns receiver");
        var visited = new boolean[(int) mem.size()];
        mem.forEachIndexed((pixel, index) -> {
            assertEquals(new Pixel((short) index, (short) (index + 10)), pixel, "forEachIndexed value at " + index);
            visited[(int) index] = true;
        });

        for (boolean wasVisited : visited) {
            if (!wasVisited) {
                throw new AssertionError("forEachIndexed visits every element");
            }
        }

        var sum = new int[1];
        mem.forEach(pixel -> sum[0] += pixel.x() + pixel.y());
        assertEquals(52, sum[0], "forEach visits values");
        assertEquals(mem.segment().address(), mem.nativeAddress(), "native address");
    }

    static void testIndexValidation(Arena arena) {
        var mem = Mem.of(Pixel.class, arena, 2);

        assertThrows(IndexOutOfBoundsException.class,
                () -> mem.get(-1),
                "negative get index rejected");
        assertThrows(IndexOutOfBoundsException.class,
                () -> mem.get(2),
                "oversized get index rejected");
        assertThrows(IndexOutOfBoundsException.class,
                () -> mem.set(-1, new Pixel((short) 1, (short) 1)),
                "negative set index rejected");
        assertThrows(IndexOutOfBoundsException.class,
                () -> mem.set(2, new Pixel((short) 1, (short) 1)),
                "oversized set index rejected");
    }

    static void testWrap(Arena arena) {
        var layout = MemLayout.of(Pixel.class);
        var segment = arena.allocate(layout.layout(), 4);
        var mem = Mem.wrap(Pixel.class, segment, 2);
        var expected = new Pixel((short) 11, (short) 22);

        assertEquals(2L, mem.size(), "wrapped mem size respects requested element count");
        assertEquals(layout.layout().byteSize() * 2, mem.segment().byteSize(), "wrapped segment is sliced to requested size");

        mem.set(1, expected);
        assertEquals(expected, mem.get(1), "wrapped segment round-trip");
        assertThrows(IndexOutOfBoundsException.class,
                () -> mem.get(2),
                "wrapped mem does not expose extra segment capacity");

        assertThrows(IllegalArgumentException.class,
                () -> Mem.wrap(Pixel.class, segment, 5),
                "wrap rejects segments that are too small");
    }

    static void testHeapSegmentRejected() {
        var heapSegment = MemorySegment.ofArray(new byte[4]);

        assertThrows(IllegalArgumentException.class,
                () -> Mem.wrap(Pixel.class, heapSegment, 1),
                "wrap rejects heap segments");
    }

    static void testMemoryReferenceFactories(Arena arena) {
        var layout = MemLayout.of(Pixel.class).layout();
        var segment = arena.allocate(layout);

        var ptr = Ptr.of(segment);
        assertEquals(segment, ptr.segment(), "Ptr retains its native segment");
        assertEquals(segment.address(), ptr.nativeAddress(), "Ptr exposes its native address");

        assertTrue(Ptr.NULL.isNull(), "Ptr.NULL represents native address zero");
        assertSame(Ptr.NULL, Ptr.of(MemorySegment.NULL), "Ptr.of canonicalizes native NULL");
        assertSame(Ptr.NULL, Nulls.of(), "Nulls.of() returns the canonical untyped NULL");

        var rawMem = RawMem.of(Pixel.class, segment);
        assertEquals(segment, rawMem.segment(), "RawMem retains its native segment");
        assertEquals(Pixel.class, rawMem.type(), "RawMem retains its element type");
        assertEquals(layout, rawMem.layout(), "RawMem derives its element layout");
        assertTrue(ptr.equals(rawMem), "Ptr equals RawMem at the same address");
        assertTrue(rawMem.equals(ptr), "RawMem equality with Ptr is symmetric");
        assertEquals(ptr.hashCode(), rawMem.hashCode(), "same-address pointers share a hash code");

        var mem = Mem.wrap(Pixel.class, segment, 1);
        assertTrue(ptr.equals(mem), "Ptr equals Mem at the same address");
        assertTrue(mem.equals(ptr), "Mem equality with Ptr is symmetric");
        assertTrue(rawMem.equals(mem), "RawMem equals Mem at the same address");
        assertTrue(mem.equals(rawMem), "Mem equality with RawMem is symmetric");
        assertEquals(ptr.hashCode(), mem.hashCode(), "same-address Mem shares pointer hash code");
        assertTrue(rawMem.hasSameType(mem), "RawMem and Mem carry the same element type");
        assertTrue(mem.hasSameType(rawMem), "Mem inherits symmetric type comparison");
        assertTrue(!rawMem.hasSameType(RawMem.of(Color.class)),
                "different element types do not match");
        assertTrue(!rawMem.hasSameType(null), "null has no matching element type");

        RawMem<Pixel> nullRawMem = RawMem.of(Pixel.class);
        RawMem<Pixel> factoryNullRawMem = Nulls.of(Pixel.class);
        Ptr widenedNull = nullRawMem;
        assertTrue(nullRawMem.isNull(), "RawMem.of(type) creates typed native NULL");
        assertTrue(widenedNull.isNull(), "typed native NULL widens to a null Ptr");
        assertTrue(Ptr.NULL.equals(nullRawMem), "untyped and typed native NULL are equal");
        assertTrue(nullRawMem.equals(Ptr.NULL), "native NULL equality is symmetric");
        assertEquals(Ptr.NULL.hashCode(), nullRawMem.hashCode(), "native NULL values share a hash code");
        assertEquals(Pixel.class, nullRawMem.type(), "typed native NULL retains its element type");
        assertEquals(layout, nullRawMem.layout(), "typed native NULL retains its element layout");
        assertTrue(factoryNullRawMem.isNull(), "Nulls.of(type) creates typed native NULL");
        assertTrue(factoryNullRawMem.hasSameType(nullRawMem),
                "Nulls.of(type) retains runtime element metadata");

        var heapSegment = MemorySegment.ofArray(new byte[4]);
        assertThrows(IllegalArgumentException.class,
                () -> Ptr.of(heapSegment),
                "Ptr rejects heap segments");
        assertThrows(IllegalArgumentException.class,
                () -> RawMem.of(Pixel.class, heapSegment),
                "RawMem rejects heap segments");
    }

    @SuppressWarnings("unchecked")
    static void testPointerRecordFields(Arena arena) {
        var pixelSegment = arena.allocate(MemLayout.of(Pixel.class).layout());
        var opaqueSegment = arena.allocate(1);
        var ptr = Ptr.of(opaqueSegment);
        var pixels = RawMem.of(Pixel.class, pixelSegment);
        var refs = Mem.of(PointerRecord.class, arena, 2);

        var expected = new PointerRecord(ptr, pixels);
        refs.set(0, expected);
        var actual = refs.get(0);
        assertEquals(expected, actual, "Ptr and RawMem fields round-trip by address");
        assertEquals(Pixel.class, actual.pixels().type(),
                "RawMem field reconstruction retains its declared type");

        var nulls = new PointerRecord(Ptr.NULL, Nulls.of(Pixel.class));
        refs.set(1, nulls);
        var actualNulls = refs.get(1);
        assertTrue(actualNulls.opaque().isNull(), "Ptr field preserves native NULL");
        assertTrue(actualNulls.pixels().isNull(), "RawMem field preserves typed native NULL");
        assertEquals(Pixel.class, actualNulls.pixels().type(),
                "typed native NULL is reconstructed from declaration metadata");

        RawMem<Color> colors = RawMem.of(
                Color.class, arena.allocate(MemLayout.of(Color.class).layout()));
        RawMem<Pixel> forged = (RawMem<Pixel>) (RawMem<?>) colors;
        assertThrows(IllegalArgumentException.class,
                () -> refs.set(0, new PointerRecord(Ptr.NULL, forged)),
                "runtime type witness rejects an unchecked RawMem cast");
        assertThrows(NullPointerException.class,
                () -> refs.set(0, new PointerRecord(null, pixels)),
                "pointer record fields reject Java null");

        var nodes = Mem.of(RecursiveNode.class, arena, 1);
        var node = new RecursiveNode(7, Nulls.of(RecursiveNode.class));
        nodes.set(0, node);
        var actualNode = nodes.get(0);
        assertEquals(7, actualNode.value(), "recursive pointer record retains primitive state");
        assertTrue(actualNode.next().isNull(), "recursive RawMem field remains one native address");
        assertEquals(RecursiveNode.class, actualNode.next().type(),
                "recursive RawMem field retains its pointee type");
    }

    static void testPointerSchemaValidation() {
        assertThrows(IllegalArgumentException.class,
                () -> MemLayout.of(RawRawMemField.class),
                "raw RawMem record fields are rejected");
        assertThrows(IllegalArgumentException.class,
                () -> MemLayout.of(WildcardRawMemField.class),
                "wildcard RawMem record fields are rejected");
        assertThrows(IllegalArgumentException.class,
                () -> MemLayout.of(GenericRawMemField.class),
                "unresolved RawMem type variables are rejected");
        assertThrows(UnsupportedOperationException.class,
                () -> MemLayout.of(CountedMemField.class),
                "counted Mem values are not representable as one record address field");
        assertThrowsContaining(UnsupportedOperationException.class,
                () -> MemLayout.of(PtrArrayField.class),
                "Arrays of pointers are not supported",
                "Ptr arrays have a specific schema diagnostic");
        assertThrowsContaining(UnsupportedOperationException.class,
                () -> MemLayout.of(RawMemArrayField.class),
                "Arrays of pointers are not supported",
                "RawMem arrays have a specific schema diagnostic");
    }

    static void testReinterpret(Arena arena) {
        var layout = MemLayout.of(Pixel.class);
        var segment = arena.allocate(layout.layout(), 2);
        var source = Mem.wrap(Pixel.class, segment, 2);
        var reinterpreted = Mem.reinterpret(Pixel.class, segment.address(), arena, 2);
        var expected = new Pixel((short) 33, (short) 44);

        source.set(0, expected);

        assertEquals(2L, reinterpreted.size(), "reinterpreted mem size");
        assertEquals(expected, reinterpreted.get(0), "raw address reinterpret reads original memory");
    }

    static void testCopyAndSwap(Arena arena) {
        var src = Mem.of(Pixel.class, arena, 4)
                .initIndexed(i -> new Pixel((short) (i + 1), (short) (i + 11)));
        var dst = Mem.of(Pixel.class, arena, 4)
                .fill(new Pixel((short) 0, (short) 0));

        dst.copyFrom(src);
        for (long i = 0; i < src.size(); i++) {
            assertEquals(src.get(i), dst.get(i), "copyFrom full value at " + i);
        }

        dst.fill(new Pixel((short) 0, (short) 0));
        src.copyTo(dst, 1, 2, 2);
        assertEquals(new Pixel((short) 0, (short) 0), dst.get(0), "range copy leaves prefix unchanged");
        assertEquals(new Pixel((short) 0, (short) 0), dst.get(1), "range copy leaves gap unchanged");
        assertEquals(src.get(1), dst.get(2), "range copy first element");
        assertEquals(src.get(2), dst.get(3), "range copy second element");

        dst.copyFrom(src, 4, 4, 0);
        assertEquals(src.get(1), dst.get(2), "zero-count copy is allowed at end");

        dst.swap(0, 3);
        assertEquals(src.get(2), dst.get(0), "swap moved last to first");
        assertEquals(new Pixel((short) 0, (short) 0), dst.get(3), "swap moved first to last");
        dst.swap(1, 1);
        assertEquals(new Pixel((short) 0, (short) 0), dst.get(1), "same-index swap leaves value");

        assertThrows(IllegalArgumentException.class,
                () -> Mem.of(Pixel.class, arena, 3).copyFrom(src),
                "full copy requires equal sizes");
        assertThrows(IndexOutOfBoundsException.class,
                () -> dst.copyFrom(src, 3, 0, 2),
                "range copy rejects oversized source range");
        assertThrows(IndexOutOfBoundsException.class,
                () -> dst.copyFrom(src, 0, 3, 2),
                "range copy rejects oversized destination range");
        assertThrows(IndexOutOfBoundsException.class,
                () -> dst.copyFrom(src, 0, 0, -1),
                "range copy rejects negative count");
    }

    static void testLayoutText() {
        var expectedSummary = """
                LayoutPixel [0..24) - 24 B
                +-- x: int [0..4) - 4 B
                +-- padding [4..8) - 4 B
                `-- y: double[2] [8..24) - 16 B
                """;
        var expectedSource = """
                MemoryLayout.structLayout(
                    ValueLayout.JAVA_INT.withName("x"),
                    MemoryLayout.paddingLayout(4),
                    MemoryLayout.sequenceLayout(2,
                        ValueLayout.JAVA_DOUBLE
                    ).withName("y")
                ).withName("LayoutPixel")""";
        var expectedNestedSummary = """
                LayoutPoint [0..32) - 32 B
                +-- c: char [0..2) - 2 B
                +-- padding [2..8) - 6 B
                `-- pixel: LayoutPixel [8..32) - 24 B
                    +-- x: int [8..12) - 4 B
                    +-- padding [12..16) - 4 B
                    `-- y: double[2] [16..32) - 16 B
                """;
        var expectedArraySummary = """
                LayoutPixelArrayPoint [0..104) - 104 B
                +-- c: char [0..2) - 2 B
                +-- padding [2..8) - 6 B
                `-- pixels: LayoutPixel[4] [8..104) - 96 B
                    `-- element: LayoutPixel [8..32) - 24 B x 4
                        +-- x: int [8..12) - 4 B
                        +-- padding [12..16) - 4 B
                        `-- y: double[2] [16..32) - 16 B
                """;

        assertEquals(expectedSummary, MemLayout.typeSummary(LayoutPixel.class), "layout summary");
        assertEquals(expectedSource, MemLayout.of(LayoutPixel.class).source(), "layout source");
        assertEquals(expectedNestedSummary, MemLayout.typeSummary(LayoutPoint.class), "nested layout summary");
        assertEquals(expectedArraySummary, MemLayout.typeSummary(LayoutPixelArrayPoint.class), "record array layout summary");
        assertTrue(MemLayout.typeSummary(LayoutPoint.class, SummaryStyle.UNICODE).contains("\u2514\u2500\u2500 pixel"),
                "unicode layout summary uses box drawing");
        var pointerSummary = MemLayout.typeSummary(PointerRecord.class);
        assertTrue(pointerSummary.contains("opaque: Ptr"),
                "pointer layout summary uses the Ptr schema type");
        assertTrue(pointerSummary.contains("pixels: RawMem<Pixel>"),
                "typed pointer layout summary retains its pointee type");
        assertPrintTypeSummary(LayoutPoint.class, SummaryStyle.UNICODE, "print type summary");
        assertDefaultPrintTypeSummary(LayoutPoint.class, "default print type summary");
        
        var longSummary = MemLayout.typeSummary(LayoutWithLongSummaryNames.class);
        assertTrue(longSummary.contains("nestedRecordComponentNameThatIsAlsoLongEnough"), "long summary includes field name");
        assertTrue(longSummary.contains("LayoutRecordNameThatIsLongEnoughToStretchTypeColumn"), "long summary includes record type");
        assertTrue(longSummary.contains("[0..4) - 4 B"), "long summary includes byte range");
    }

    static void assertPrintTypeSummary(Class<? extends Record> type, SummaryStyle style, String label) {
        var original = System.out;
        var bytes = new ByteArrayOutputStream();
        try (var capture = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            MemLayout.printTypeSummary(type, style);
        } finally {
            System.setOut(original);
        }
        assertEquals(MemLayout.typeSummary(type, style), bytes.toString(StandardCharsets.UTF_8), label);
    }
    
    static void assertDefaultPrintTypeSummary(Class<? extends Record> type, String label) {
        var original = System.out;
        var bytes = new ByteArrayOutputStream();
        try (var capture = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            MemLayout.printTypeSummary(type);
        } finally {
            System.setOut(original);
        }
        assertEquals(MemLayout.typeSummary(type, SummaryStyle.UNICODE),
                bytes.toString(StandardCharsets.UTF_8), label);
    }

    static void assertPrimitiveArrays(PrimitiveArrays expected, PrimitiveArrays actual, String label) {
        if (!Arrays.equals(expected.ints(), actual.ints())
                || !Arrays.equals(expected.floats(), actual.floats())
                || !Arrays.equals(expected.shorts(), actual.shorts())) {
            throw new AssertionError(label + ": expected " + primitiveArraysToString(expected)
                    + " but got " + primitiveArraysToString(actual));
        }
    }

    static void assertRecordArray(RecordArray expected, RecordArray actual, String label) {
        if (!Arrays.equals(expected.pixels(), actual.pixels())) {
            throw new AssertionError(label + ": expected " + Arrays.toString(expected.pixels())
                    + " but got " + Arrays.toString(actual.pixels()));
        }
    }

    static void assertAllPrimitiveArrays(AllPrimitiveArrays expected, AllPrimitiveArrays actual, String label) {
        if (!Arrays.equals(expected.booleans(), actual.booleans())
                || !Arrays.equals(expected.bytes(), actual.bytes())
                || !Arrays.equals(expected.shorts(), actual.shorts())
                || !Arrays.equals(expected.chars(), actual.chars())
                || !Arrays.equals(expected.ints(), actual.ints())
                || !Arrays.equals(expected.longs(), actual.longs())
                || !Arrays.equals(expected.floats(), actual.floats())
                || !Arrays.equals(expected.doubles(), actual.doubles())) {
            throw new AssertionError(label);
        }
    }

    static void assertNestedWithArrays(NestedWithArrays expected, NestedWithArrays actual, String label) {
        assertEquals(expected.origin(), actual.origin(), label + " origin");
        assertPrimitiveArrays(expected.samples(), actual.samples(), label + " samples");
        assertRecordArray(expected.palette(), actual.palette(), label + " palette");
    }

    static String primitiveArraysToString(PrimitiveArrays value) {
        return "ints=" + Arrays.toString(value.ints())
                + ", floats=" + Arrays.toString(value.floats())
                + ", shorts=" + Arrays.toString(value.shorts());
    }

    static void assertEquals(Object expected, Object actual, String label) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(label + ": expected " + expected + " but got " + actual);
        }
    }

    static void assertTrue(boolean value, String label) {
        if (!value) {
            throw new AssertionError(label);
        }
    }
    
    static void assertSame(Object expected, Object actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected same instance");
        }
    }

    static void assertThrows(Class<? extends Throwable> expected, Runnable action, String label) {
        try {
            action.run();
        } catch (Throwable actual) {
            if (expected.isInstance(actual)) {
                return;
            }
            throw new AssertionError(label + ": expected " + expected.getName()
                    + " but got " + actual.getClass().getName(), actual);
        }

        throw new AssertionError(label + ": expected " + expected.getName());
    }

    static void assertThrowsContaining(
            Class<? extends Throwable> expected,
            Runnable action,
            String messagePart,
            String label) {
        try {
            action.run();
        } catch (Throwable actual) {
            if (!expected.isInstance(actual)) {
                throw new AssertionError(label + ": expected " + expected.getName()
                        + " but got " + actual.getClass().getName(), actual);
            }
            if (actual.getMessage() == null || !actual.getMessage().contains(messagePart)) {
                throw new AssertionError(label + ": expected message containing '"
                        + messagePart + "' but got '" + actual.getMessage() + "'", actual);
            }
            return;
        }

        throw new AssertionError(label + ": expected " + expected.getName());
    }
}
