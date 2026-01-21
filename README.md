# TypedMemory
Typed Memory is a Java library for working with strongly-typed views over off-heap memory, built on top of the Java Foreign Function & Memory (FFM) API. It lets you describe value-oriented data structures using Java types (records for now), while controlling layout, lifetime, and allocation scope explicitly — without giving up safety or readability.

# Motivation

Java’s object model is excellent for identity-based programming, but it is not ideal for:
* Data-oriented design (DOD)
* Flat, cache-friendly layouts
* Interop with native code
* Large numeric or geometric datasets
* Stack- or arena-scoped allocation

The FFM API gives access to raw memory, but it is untyped and low-level. Typed Memory bridges that gap by providing typed, layout-aware views over memory segments.

# Core Idea

Instead of this:
~~~java
MemorySegment segment = arena.allocate(24);
int x = segment.get(ValueLayout.JAVA_INT, 0);
~~~

You write this:

try (Arena arena = Arena.ofConfined()) {
    Mem<Point> points = Mem.of(Point.class, arena, 10);

    points.get(0).x(10);
    points.get(0).y(20);
}


The memory is still off-heap.
The lifetime is still explicit.
But the access is typed, structured, and safe.

Example
record Point(int x, int y) {}

try (Arena arena = Arena.ofConfined()) {
    Mem<Point> points = Mem.of(Point.class, arena, 10);

    Point p0 = points.get(0);
    p0.x(3);
    p0.y(4);
}


Point is treated as a value description

Memory layout is derived from the type

No Java objects are allocated per element

Access is bounds-checked and type-checked
