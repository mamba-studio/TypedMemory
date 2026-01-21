# TypedMemory
Typed Memory is a Java library for working with strongly-typed views over off-heap memory, built on top of the Java Foreign Function & Memory (FFM) API. It lets you describe value-oriented data structures using Java types (records for now), while controlling layout, lifetime, and allocation scope explicitly — without giving up safety or readability.

## Motivation

Java’s object model is excellent for identity-based programming, but it is not ideal for:
* Data-oriented design (DOD)
* Flat, cache-friendly layouts
* Interop with native code
* Large numeric or geometric datasets
* Stack- or arena-scoped allocation

The FFM API gives access to raw memory, but it is untyped and low-level. Typed Memory bridges that gap by providing typed, layout-aware views over memory segments.

## Core Idea

Instead of this:
~~~java
MemoryLayout POINT_LAYOUT = MemoryLayout.structLayout(
                    ValueLayout.JAVA_INT.withName("x"),
                    ValueLayout.JAVA_INT.withName("y"));

VarHandle X = POINT_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("x"));
VarHandle Y = POINT_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("y"));

void main(){   
    try (Arena arena = Arena.ofConfined()) {
        long POINT_SIZE = POINT_LAYOUT.byteSize(); // 8 bytes

        MemorySegment segment = arena.allocate(POINT_SIZE * 10, POINT_LAYOUT.byteAlignment());

        int index = 0;
        long offset = index * POINT_SIZE;

        X.set(segment, offset, 10);
        Y.set(segment, offset, 20);
    }   
}
~~~

You write this:
~~~java
record Point(int x, int y) {}

void main(){
    try (Arena arena = Arena.ofConfined()) {
        Mem<Point> points = Mem.of(Point.class, arena, 10);
        points.set(0, new Point(10, 20));
    }
}
~~~

The memory is still off-heap.
The lifetime is still explicit.
But the access is typed, structured, and safe.

Example
~~~java
record Point(int x, int y) {}

try (Arena arena = Arena.ofConfined()) {
    Mem<Point> points = Mem.of(Point.class, arena, 10);
    points.set(0, new Point(10, 20));

    if(get(0) instanceof Point(var x, var y){
         IO.println("x: " +x+ "y: " +y);
    }
}
~~~

Point is treated as a value description. Memory layout is derived from the type. No Java objects are allocated per element. Access is bounds-checked and type-checked
