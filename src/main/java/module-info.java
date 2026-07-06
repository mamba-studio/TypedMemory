/**
 * {@summary TypedMemory provides strongly-typed views over off-heap memory.}
 *
 * <p>
 * The {@code com.mamba.typedmemory} module offers a high-level programming model
 * for working with structured memory using the Java Foreign Function &amp; Memory (FFM) API.
 * It allows Java types, typically {@code record}s, to be mapped deterministically to
 * memory layouts and accessed through strongly-typed views.
 *
 * <p>
 * At the core of the module is the {@link com.mamba.typedmemory.api.Mem} abstraction,
 * which represents a contiguous region of memory storing elements of a given type.
 * Each element is described by a {@link java.lang.foreign.MemoryLayout} derived from
 * the structure of the element type.
 *
 * <h2>Example</h2>
 *
 * {@snippet :
 *      import java.lang.foreign.Arena;
 *      import com.mamba.typedmemory.api.Mem;
 *
 *      record Color(float r, float g, float b) {}
 *
 *      try (Arena arena = Arena.ofConfined()) {
 *          Mem<Color> colors = Mem.of(Color.class, arena, 10);
 *          colors.set(0, new Color(1f, 0f, 0f));
 *      }
 * }
 *
 * <h2>Key Features</h2>
 *
 * <ul>
 * <li>Strongly-typed access to off-heap memory</li>
 * <li>Deterministic memory layouts derived from Java types</li>
 * <li>Runtime generation of optimized memory access implementations</li>
 * </ul>
 *
 * <h2>Relationship to the Foreign Memory API</h2>
 *
 * <p>
 * This module builds on top of the Java Foreign Function &amp; Memory API
 * ({@link java.lang.foreign.MemorySegment}, {@link java.lang.foreign.MemoryLayout},
 * {@link java.lang.foreign.Arena}) while providing a higher-level abstraction
 * tailored for structured data and record-based memory layouts.
 *
 * <p>
 * TypedMemory is particularly useful for applications involving binary formats,
 * native interoperability, and high-performance data processing.
 *
 * @since 0.1
 */

module com.mamba.typedmemory {
    /**
     * Public API for typed off-heap memory views and derived memory layouts.
     */
    exports com.mamba.typedmemory.api;
    exports com.mamba.typedmemory.api.handle.path;
    exports com.mamba.typedmemory.api.handle.path2;
}
