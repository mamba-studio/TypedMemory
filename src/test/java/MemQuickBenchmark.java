/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author joemw
 */
import com.mamba.typedmemory.api.Mem;
import java.lang.foreign.*;
import java.lang.invoke.VarHandle;

public class MemQuickBenchmark {
    
    public static final MemoryLayout layout = MemoryLayout.structLayout(
                ValueLayout.JAVA_INT.withName("x"),
                ValueLayout.JAVA_INT.withName("y")
        );

    public static final   VarHandle X = layout.varHandle(
                MemoryLayout.PathElement.groupElement("x"));
    public static final   VarHandle Y = layout.varHandle(
                MemoryLayout.PathElement.groupElement("y"));

    // -------- Test Record --------
    record Point(int x, int y) {}

    static final int SIZE  = 50_000_000;
    static final int LOOPS = 5;
    
    public static void main(String[] args) throws Throwable {

        System.out.println("Warming up...");
        for (int i = 0; i < 3; i++) {
            runAll();
        }

        System.out.println("\nMeasured run:");
        runAll();
    }

    static void runAll() throws Throwable {
        testArrayWrite();
        testManualPanamaWrite();
        testMemWrite();
        System.out.println();
    }

    // ============================================================
    // 1️⃣ Plain Java Array
    // ============================================================

    static void testArrayWrite() {
        Point[] arr = new Point[SIZE];
        long start = System.nanoTime();
        
        for (int r = 0; r < LOOPS; r++) {
            for (int i = 0; i < SIZE; i++) {
                arr[i] = new Point(i, i);
            }
        }

        long end = System.nanoTime();
        System.out.println("Array write:        " + readable(end - start));
    }

    // ============================================================
    // 2️⃣ Manual Panama
    // ============================================================

    static void testManualPanamaWrite() throws Throwable {
        try (Arena arena = Arena.ofConfined()) {

            long stride = layout.byteSize();
            MemorySegment segment = arena.allocate(stride * SIZE);

            long start = System.nanoTime();

            for (int r = 0; r < LOOPS; r++) {
                for (int i = 0; i < SIZE; i++) {
                    long offset = i * stride;
                    X.set(segment, offset, i);
                    Y.set(segment, offset, i);
                }
            }

            long end = System.nanoTime();
            System.out.println("Manual Panama:      " + readable(end - start));
        }
    }

    // ============================================================
    // 3️⃣ Your Mem<T>
    // ============================================================

    static void testMemWrite() throws Throwable {
        try (Arena arena = Arena.ofConfined()) {

            Mem<Point> mem = Mem.of(Point.class, arena, SIZE);            
            long start = System.nanoTime();

            for (int r = 0; r < LOOPS; r++) {
                for (int i = 0; i < SIZE; i++) {
                    Point p = new Point(i, i);
                    mem.set(i, p);
                }
            }

            long end = System.nanoTime();
            System.out.println("Mem<T>:             " + readable(end - start));
        }
    }

    // ============================================================

    static String readable(long nanos) {
        return (nanos / 1_000_000) + " ms";
    }
}
