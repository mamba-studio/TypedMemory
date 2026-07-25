import com.mamba.typedmemory.api.Mem;
import com.mamba.typedmemory.api.size;
import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;
import java.util.Arrays;

public class MemQuickBenchmark {
    static final MemoryLayout POINT_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("x"),
            ValueLayout.JAVA_INT.withName("y")
    );

    static final VarHandle POINT_X = POINT_LAYOUT.varHandle(
            MemoryLayout.PathElement.groupElement("x"));
    static final VarHandle POINT_Y = POINT_LAYOUT.varHandle(
            MemoryLayout.PathElement.groupElement("y"));

    record Point(int x, int y) {}
    record Pixel(short x, short y) {}
    record Sample(int id, Pixel origin, @size(4) int[] scores) {}

    static final int DEFAULT_SIZE = 5_000_000;
    static final int DEFAULT_LOOPS = 3;
    static final int WARMUPS = 2;

    static volatile long blackhole;

    public static void main(String[] args) throws Throwable {
        int size = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_SIZE;
        int loops = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_LOOPS;

        IO.println("MemQuickBenchmark");
        IO.println("size=" + size + ", loops=" + loops);
        IO.println();

        for (int i = 0; i < WARMUPS; i++) {
            runAll(size, loops, false);
        }

        runAll(size, loops, true);
        IO.println("blackhole=" + blackhole);
    }

    static void runAll(int size, int loops, boolean report) throws Throwable {
        if (report) {
            IO.println("Writes");
        }
        arrayWrite(size, loops, report);
        manualPanamaWrite(size, loops, report);
        memWrite(size, loops, report);
        memInit(size, loops, report);

        if (report) {
            IO.println();
            IO.println("Reads and traversal");
        }
        arrayRead(size, loops, report);
        manualPanamaRead(size, loops, report);
        memGet(size, loops, report);
        memForEachIndexed(size, loops, report);

        if (report) {
            IO.println();
            IO.println("Nested records and arrays");
        }
        memNestedArrayWriteRead(Math.max(1, size / 10), loops, report);

        if (report) {
            IO.println();
        }
    }

    static void arrayWrite(int size, int loops, boolean report) {
        Point[] points = new Point[size];

        long elapsed = time(() -> {
            for (int r = 0; r < loops; r++) {
                for (int i = 0; i < size; i++) {
                    points[i] = new Point(i, i + 1);
                }
            }
        });

        blackhole ^= points[size - 1].x();
        print(report, "Array write", elapsed);
    }

    static void manualPanamaWrite(int size, int loops, boolean report) {
        try (Arena arena = Arena.ofConfined()) {
            long stride = POINT_LAYOUT.byteSize();
            MemorySegment segment = arena.allocate(stride * size);

            long elapsed = time(() -> {
                for (int r = 0; r < loops; r++) {
                    for (int i = 0; i < size; i++) {
                        long offset = i * stride;
                        POINT_X.set(segment, offset, i);
                        POINT_Y.set(segment, offset, i + 1);
                    }
                }
            });

            blackhole ^= (int) POINT_X.get(segment, (size - 1L) * stride);
            print(report, "Manual Panama write", elapsed);
        }
    }

    static void memWrite(int size, int loops, boolean report) {
        try (Arena arena = Arena.ofConfined()) {
            Mem<Point> points = Mem.of(Point.class, arena, size);

            long elapsed = time(() -> {
                for (int r = 0; r < loops; r++) {
                    for (int i = 0; i < size; i++) {
                        points.set(i, new Point(i, i + 1));
                    }
                }
            });

            blackhole ^= points.get(size - 1).x();
            print(report, "Mem write", elapsed);
        }
    }

    static void memInit(int size, int loops, boolean report) {
        long elapsed = 0;

        for (int r = 0; r < loops; r++) {
            try (Arena arena = Arena.ofConfined()) {
                Mem<Point> points = Mem.of(Point.class, arena, size);
                elapsed += time(() -> points.setAll(i -> new Point((int) i, (int) i + 1)));

                blackhole ^= points.get(size - 1).y();
            }
        }

        print(report, "Mem setAll(LongFunction)", elapsed);
    }

    static void arrayRead(int size, int loops, boolean report) {
        Point[] points = pointArray(size);

        long elapsed = time(() -> {
            long sum = 0;
            for (int r = 0; r < loops; r++) {
                for (int i = 0; i < size; i++) {
                    Point point = points[i];
                    sum += point.x() + point.y();
                }
            }
            blackhole ^= sum;
        });

        print(report, "Array read sum", elapsed);
    }

    static void manualPanamaRead(int size, int loops, boolean report) {
        try (Arena arena = Arena.ofConfined()) {
            long stride = POINT_LAYOUT.byteSize();
            MemorySegment segment = arena.allocate(stride * size);
            fillManualPanama(segment, size);

            long elapsed = time(() -> {
                long sum = 0;
                for (int r = 0; r < loops; r++) {
                    for (int i = 0; i < size; i++) {
                        long offset = i * stride;
                        sum += (int) POINT_X.get(segment, offset);
                        sum += (int) POINT_Y.get(segment, offset);
                    }
                }
                blackhole ^= sum;
            });

            print(report, "Manual Panama read sum", elapsed);
        }
    }

    static void memGet(int size, int loops, boolean report) {
        try (Arena arena = Arena.ofConfined()) {
            Mem<Point> points = filledPointMem(arena, size);

            long elapsed = time(() -> {
                long sum = 0;
                for (int r = 0; r < loops; r++) {
                    for (int i = 0; i < size; i++) {
                        Point point = points.get(i);
                        sum += point.x() + point.y();
                    }
                }
                blackhole ^= sum;
            });

            print(report, "Mem get sum", elapsed);
        }
    }

    static void memForEachIndexed(int size, int loops, boolean report) {
        try (Arena arena = Arena.ofConfined()) {
            Mem<Point> points = filledPointMem(arena, size);

            long elapsed = time(() -> {
                long[] sum = {0};
                for (int r = 0; r < loops; r++) {
                    points.forEach((point, index) -> sum[0] += point.x() + point.y() + index);
                }
                blackhole ^= sum[0];
            });

            print(report, "Mem indexed forEach sum", elapsed);
        }
    }

    static void memNestedArrayWriteRead(int size, int loops, boolean report) {
        try (Arena arena = Arena.ofConfined()) {
            Mem<Sample> samples = Mem.of(Sample.class, arena, size);

            long elapsed = time(() -> {
                long checksum = 0;
                for (int r = 0; r < loops; r++) {
                    for (int i = 0; i < size; i++) {
                        samples.set(i, sample(i));
                    }

                    for (int i = 0; i < size; i++) {
                        Sample sample = samples.get(i);
                        checksum += sample.id();
                        checksum += sample.origin().x();
                        checksum += Arrays.stream(sample.scores()).sum();
                    }
                }
                blackhole ^= checksum;
            });

            print(report, "Mem nested record/array", elapsed);
        }
    }

    static Point[] pointArray(int size) {
        Point[] points = new Point[size];
        for (int i = 0; i < size; i++) {
            points[i] = new Point(i, i + 1);
        }
        return points;
    }

    static Mem<Point> filledPointMem(Arena arena, int size) {
        Mem<Point> points = Mem.of(Point.class, arena, size);
        for (int i = 0; i < size; i++) {
            points.set(i, new Point(i, i + 1));
        }
        return points;
    }

    static void fillManualPanama(MemorySegment segment, int size) {
        long stride = POINT_LAYOUT.byteSize();
        for (int i = 0; i < size; i++) {
            long offset = i * stride;
            POINT_X.set(segment, offset, i);
            POINT_Y.set(segment, offset, i + 1);
        }
    }

    static Sample sample(int i) {
        return new Sample(
                i,
                new Pixel((short) i, (short) (i + 1)),
                new int[] {i, i + 1, i + 2, i + 3}
        );
    }

    static long time(Runnable action) {
        long start = System.nanoTime();
        action.run();
        return System.nanoTime() - start;
    }

    static void print(boolean report, String label, long nanos) {
        if (report) {
            IO.println("%-28s %8d ms".formatted(label + ":", nanos / 1_000_000));
        }
    }
}
