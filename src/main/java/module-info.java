/// {@summary Strongly typed, record-based views over contiguous off-heap memory.}
///
/// TypedMemory is a Java 25 library built on the Foreign Function &amp; Memory
/// (FFM) API. It maps Java records to deterministic native
/// {@link java.lang.foreign.MemoryLayout memory layouts} and exposes contiguous
/// memory through ordinary record values. It removes most manual offset and
/// layout plumbing while leaving allocation, lifetime, layout, and the backing
/// {@link java.lang.foreign.MemorySegment memory segment} visible to the caller.
/// It is intended for native interoperability, binary data, graphics and
/// simulation workloads, and other data-oriented or performance-sensitive code.
///
/// # Programming model
///
/// A record acts as the schema for one element. A
/// {@link com.mamba.typedmemory.api.Mem Mem&lt;T&gt;} is a counted view containing
/// zero or more such elements in one contiguous native segment. Calling
/// {@link com.mamba.typedmemory.api.Mem#get(long) get} reconstructs a record from
/// its bytes; calling {@link com.mamba.typedmemory.api.Mem#set(long, Object) set}
/// writes the record components into the segment. The segment stores structured
/// binary data, not references to the Java record objects.
///
/// {@snippet :
/// import java.lang.foreign.Arena;
/// import com.mamba.typedmemory.api.Mem;
///
/// record Point(float x, float y) {}
///
/// try (Arena arena = Arena.ofConfined()) {
///     Mem<Point> points = Mem.of(Point.class, arena, 10);
///     points.set(0, new Point(5, 3));
///
///     Point point = points.get(0);
///     MemoryLayout elementLayout = points.layout();
///     MemorySegment backingSegment = points.segment();
/// }
/// }
///
/// {@link java.lang.foreign.Arena Arena} allocation gives the view the
/// arena's lifetime. {@link com.mamba.typedmemory.api.Mem#wrap(Class,
/// java.lang.foreign.MemorySegment, long) Mem.wrap} instead places a typed,
/// counted view over an existing native segment without taking ownership.
/// {@code Mem} also provides indexed and bulk initialization, traversal,
/// copying, range copying, and element swapping.
///
/// # Record schemas
///
/// Record components may be:
///
///
/// - The primitive types {@code boolean}, {@code byte}, {@code short},
///   {@code char}, {@code int}, {@code long}, {@code float}, and
///   {@code double}.
/// - Nested records whose components follow the same rules.
/// - Fixed-size arrays of primitives or records, annotated with
///   {@link com.mamba.typedmemory.api.size @size}.
/// - Records with an explicit struct alignment declared using
///   {@link com.mamba.typedmemory.api.align @align}.
/// - {@link com.mamba.typedmemory.api.Ptr Ptr}, an opaque native pointer.
/// - {@link com.mamba.typedmemory.api.RawMem RawMem&lt;T&gt;}, a typed native
///   pointer carrying a record type and its single-element layout.
///
///
/// {@snippet :
/// record Pixel(short x, short y) {}
///
/// record Particle(
///         int id,
///         Pixel origin,
///         @size(4) float[] weights,
///         @size(3) Pixel[] trail,
///         Ptr userData,
///         RawMem<Particle> next) {}
/// }
///
/// Every array component must declare a positive fixed size. Object
/// references such as {@link java.lang.String}, collections, nested arrays,
/// arrays of pointers, raw or wildcard {@code RawMem} types, counted
/// {@code Mem} fields, and unions are not supported. A typed pointer component
/// must name a concrete record type.
///
/// {@snippet :
/// record Samples(@size(8) int[] values) {}
///
/// try (Arena arena = Arena.ofConfined()) {
///     Mem<Samples> samples = Mem.of(Samples.class, arena, 2);
///     samples.set(0, new Samples(new int[] {1, 2, 3, 4, 5, 6, 7, 8}));
/// }
/// }
///
/// {@snippet :
/// @align(16)
/// record Float3(float x, float y, float z) {}
///
/// MemoryLayout float3Layout = MemLayout.of(Float3.class).layout();
/// // byteSize() == 16 and byteAlignment() == 16
/// }
///
/// An explicit alignment is measured in bytes, must be a positive power of
/// two, and cannot weaken the record's natural alignment. It becomes part of
/// the record layout when the record is nested or used as an array element;
/// trailing padding therefore determines the correct aligned array stride.
///
/// # Memory references and native null
///
/// {@link com.mamba.typedmemory.api.Ptr Ptr} models an address without an
/// element type or count. {@link com.mamba.typedmemory.api.RawMem RawMem&lt;T&gt;}
/// adds the pointee record type and element layout but deliberately makes no
/// claim about accessible bounds. {@code Mem<T>} adds a count and indexed
/// access. Use {@link com.mamba.typedmemory.api.Nulls Nulls},
/// {@link com.mamba.typedmemory.api.Ptr#NULL Ptr.NULL}, or
/// {@link com.mamba.typedmemory.api.RawMem#of(Class) RawMem.of(Class)} for native
/// address zero; Java {@code null} is not a native-null pointer value.
///
/// Pointer equality compares native addresses, so differently typed
/// references to the same address compare equal. Use
/// {@link com.mamba.typedmemory.api.RawMem#hasSameType(
/// com.mamba.typedmemory.api.RawMem) RawMem.hasSameType} when the pointee type
/// must also match.
///
/// {@snippet :
/// record Node(int value, RawMem<Node> next) {}
///
/// RawMem<Node> noNext = Nulls.of(Node.class); // typed native NULL
/// Ptr opaqueNull = Nulls.of();                 // the canonical Ptr.NULL
///
/// try (Arena arena = Arena.ofConfined()) {
///     Mem<Node> nodes = Mem.of(Node.class, arena, 1);
///     nodes.set(0, new Node(42, noNext));
///
///     RawMem<Node> firstNode = RawMem.of(Node.class, nodes.segment());
///     long address = firstNode.nativeAddress();
///     boolean sameType = firstNode.hasSameType(noNext);
/// }
/// }
///
/// # Wrapping and reinterpreting memory
///
/// {@code Mem.wrap} can derive the element count from an exact-size native
/// segment. An explicit count may instead be supplied to expose an exact-size
/// prefix. The segment's owner remains responsible for its lifetime.
///
/// {@snippet :
/// record Point(float x, float y) {}
///
/// MemoryLayout pointLayout = MemLayout.of(Point.class).layout();
/// try (Arena arena = Arena.ofConfined()) {
///     MemorySegment segment = arena.allocate(pointLayout, 100);
///     Mem<Point> points = Mem.wrap(Point.class, segment);
///
///     points.set(0, new Point(5, 3));
/// }
/// }
///
/// {@link com.mamba.typedmemory.api.Mem#reinterpret(Class,
/// com.mamba.typedmemory.api.Ptr, java.lang.foreign.Arena, long)
/// Mem.reinterpret} turns a non-null native address into a counted view.
/// Reinterpretation is an advanced, restricted FFM operation: the caller must
/// ensure that the address is valid, correctly aligned, sufficiently large, and
/// alive for the view's lifetime. An overload accepts a cleanup action for
/// arena-owned native allocations. Applications using reinterpretation must
/// enable native access for their calling module (or for {@code ALL-UNNAMED}
/// when running on the class path).
///
/// {@snippet :
/// // The address and allocation size come from a native API.
/// long address = nativeAddress();
/// long pointCount = nativePointCount();
/// Ptr pointer = Ptr.of(address);
///
/// try (Arena arena = Arena.ofConfined()) {
///     Mem<Point> points =
///             Mem.reinterpret(Point.class, pointer, arena, pointCount);
///     Point first = points.get(0);
/// }
/// }
///
/// # Layout and implementation
///
/// The generated layout is available through
/// {@link com.mamba.typedmemory.api.RawMem#layout() layout()}, making byte size,
/// alignment, padding, and native compatibility inspectable. TypedMemory
/// generates specialized hidden implementation classes at runtime and caches
/// the generated access machinery by record type.
///
/// {@snippet :
/// MemLayout description = MemLayout.of(Point.class);
/// MemoryLayout layout = description.layout();
///
/// IO.println(layout);
/// IO.println("bytes per Point = " + layout.byteSize());
/// IO.println("alignment = " + layout.byteAlignment());
/// }
///
/// This module complements rather than replaces FFM. Code can move between
/// typed views and raw segments whenever lower-level access is required.
/// TypedMemory is experimental; its API may change as its schema model evolves.
///
/// @since 0.1

module com.mamba.typedmemory {
    /// Public API for typed off-heap memory views and derived memory layouts.
    exports com.mamba.typedmemory.api;
}
