# TypedMemory

TypedMemory is a Java library for working with strongly typed views over off-heap memory, built on top of the Java Foreign Function & Memory API. It lets you describe value-oriented data structures with Java records, while keeping layout, lifetime, and allocation scope explicit.

The idea is to access native or off-heap structured data ergonomically:

```java
record Point(float x, float y) {}

void main() {
    try (Arena arena = Arena.ofConfined()) {
        Mem<Point> points = Mem.of(Point.class, arena, 10);
        points.set(0, new Point(5, 3));

        Point point = points.get(0);
        IO.println(point);
    }
}
```

## Motivation

Java's object model is excellent for object-oriented programming built around object identity and, increasingly, for [data-oriented programming](https://www.infoq.com/articles/data-oriented-programming-java/). It is less ideal when you need:

* Data-oriented design (DOD)
* Flat, cache-friendly layouts
* Interop with native code
* Large numeric or geometric datasets
* Stack- or arena-scoped allocation

The FFM API gives access to raw memory, but it is intentionally low-level. TypedMemory bridges that gap by providing typed, layout-aware views over memory segments.

## Core Idea

This:

```java
record Point(int x, int y) {}

void main() {
    try (Arena arena = Arena.ofConfined()) {
        Mem<Point> points = Mem.of(Point.class, arena, 10);
        points.set(0, new Point(10, 20));
    }
}
```

is equivalent in spirit to manually defining a layout, allocating a segment, computing offsets, and reading/writing fields with `VarHandle`s:

```java
MemoryLayout POINT_LAYOUT = MemoryLayout.structLayout(
        ValueLayout.JAVA_INT.withName("x"),
        ValueLayout.JAVA_INT.withName("y"));

VarHandle X = POINT_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("x"));
VarHandle Y = POINT_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("y"));

void main() {
    try (Arena arena = Arena.ofConfined()) {
        long pointSize = POINT_LAYOUT.byteSize();
        MemorySegment segment = arena.allocate(pointSize * 10, POINT_LAYOUT.byteAlignment());

        int index = 0;
        long offset = index * pointSize;

        X.set(segment, offset, 10);
        Y.set(segment, offset, 20);
    }
}
```

The memory is still off-heap. The lifetime is still controlled by an `Arena`. The difference is that access is typed, structured, and easier to compose with modern Java record syntax and pattern matching.

```java
record Point(int x, int y) {}

try (Arena arena = Arena.ofConfined()) {
    Mem<Point> points = Mem.of(Point.class, arena, 10);
    points.set(0, new Point(10, 20));

    if (points.get(0) instanceof Point(var x, var y)) {
        IO.println("x: " + x + ", y: " + y);
    }
}
```

## Features

Implemented today:

* Record-based typed memory views with generated runtime implementations
* Primitive record fields: `boolean`, `byte`, `short`, `char`, `int`, `long`, `float`, and `double`
* Nested records
* Fixed-size primitive arrays using `@size`
* Fixed-size arrays of records using `@size`
* Nested records that themselves contain arrays
* Indexed `get` and `set` with bounds checking
* `fill`, `init`, and `initIndexed` helpers for initialization
* `forEach` and `forEachIndexed` traversal helpers
* Access to the backing `MemorySegment`, `MemoryLayout`, element count, element `type`, and native address
* Wrapping existing `MemorySegment` instances with `Mem.wrap`
* Reinterpreting raw native addresses with `Mem.reinterpret`
* Bulk `copyFrom`, `copyTo`, range copy, and `swap`
* Layout inspection helpers through `MemLayout`

Not implemented yet:

* Pointer-typed fields beyond using `long` addresses manually
* Unions
## Fixed-Size Arrays

Array fields must declare their element count with `@size`, because the count is part of the memory layout.

```java
record Pixel(short x, short y) {}
record Sample(int id, Pixel origin, @size(4) int[] scores) {}
record Palette(@size(3) Pixel[] colors) {}

void main() {
    try (Arena arena = Arena.ofConfined()) {
        Mem<Sample> samples = Mem.of(Sample.class, arena, 2);

        samples.set(0, new Sample(
                7,
                new Pixel((short) 10, (short) 20),
                new int[] {1, 2, 3, 4}));

        Sample sample = samples.get(0);
        IO.println(sample.origin());
        IO.println(Arrays.toString(sample.scores()));
    }
}
```

Array values are copied into memory on `set`, and arrays returned from `get` are fresh copies. Mutating a source array after writing it, or mutating an array returned by `get`, does not mutate the underlying memory.

## Existing Memory

Use `Mem.wrap` when you already own a `MemorySegment` and want a typed view over it:

```java
record Pixel(short x, short y) {}

try (Arena arena = Arena.ofConfined()) {
    var layout = MemLayout.of(Pixel.class);
    var segment = arena.allocate(layout.layout(), 4);

    Mem<Pixel> pixels = Mem.wrap(Pixel.class, segment, 4);
    pixels.set(0, new Pixel((short) 1, (short) 2));
}
```

For advanced interop, `Mem.reinterpret` can treat a raw address as typed memory:

```java
try (Arena arena = Arena.ofConfined()) {
    Mem<Pixel> pixels = Mem.of(Pixel.class, arena, 4);

    long address = pixels.nativeAddress();
    Mem<Pixel> view = Mem.reinterpret(Pixel.class, address, arena, 4);

    view.set(1, new Pixel((short) 3, (short) 4));
}
```

When using `reinterpret`, the caller is responsible for ensuring the address is valid, sufficiently large, correctly aligned, and alive for the arena scope.

## Bulk Operations

`Mem` provides convenience methods for common contiguous-memory operations:

```java
record Pixel(short x, short y) {}

try (Arena arena = Arena.ofConfined()) {
    Mem<Pixel> src = Mem.of(Pixel.class, arena, 4)
            .initIndexed(i -> new Pixel((short) i, (short) (i + 10)));

    Mem<Pixel> dst = Mem.of(Pixel.class, arena, 4)
            .fill(new Pixel((short) 0, (short) 0));

    dst.copyFrom(src);              // full copy; sizes must match
    src.copyTo(dst, 1, 2, 2);        // range copy
    dst.swap(0, 3);                 // swap two elements in place
}
```

## Layout Inspection

`MemLayout` derives deterministic FFM layouts from record types. You can inspect the generated layout or get a small memory summary:

```java
record Pixel(short x, short y) {}

MemLayout layout = MemLayout.of(Pixel.class);
IO.println(layout.describe());
IO.println(MemLayout.describe(Pixel.class));
```

## Current Requirements

This project currently targets Java 25 through Maven:

```xml
<maven.compiler.release>25</maven.compiler.release>
```

## Status

TypedMemory is experimental and focused on exploring ergonomic, record-shaped access to structured off-heap memory. The current implementation generates specialised hidden classes at runtime using ClassFile API and Method Handles and caches generated constructors for reusable record types.
