# TypedMemory
Typed Memory is a Java library for working with strongly-typed views over off-heap memory, built on top of the Java Foreign Function & Memory (FFM) API. It lets you describe value-oriented data structures using Java types (records for now), while controlling layout, lifetime, and allocation scope explicitly, without giving up safety or readability.

The idea is to access native/offheap structural data ergonomically as follows:

~~~java
record Color(float r, float g, float b, float a) {
    Color(float r, float g, float b){
        this(r, g, b, 1);
    }
}

void main(){
    try (Arena arena = Arena.ofConfined()) {
        Mem<Color> colors = Mem.of(Color.class, arena, 10);
        colors.set(0, new Color(0.5f, 0.5f, 0.5f));
    }
}
~~~

## Motivation

Java’s object model is excellent for identity-based programming and also [data oriented programming](https://www.infoq.com/articles/data-oriented-programming-java/), but it is not ideal for:
* Data-oriented design (DOD)
* Flat, cache-friendly layouts
* Interop with native code
* Large numeric or geometric datasets
* Stack- or arena-scoped allocation

The FFM API gives access to raw memory, but it is untyped and low-level. Typed Memory bridges that gap by providing typed, layout-aware views over memory segments.

## Core Idea
This code:
~~~java
record Point(int x, int y) {}

void main(){
    try (Arena arena = Arena.ofConfined()) {
        Mem<Point> points = Mem.of(Point.class, arena, 10);
        points.set(0, new Point(10, 20));
    }
}
~~~
is equivalent to:
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

The memory is still off-heap. The lifetime is still explicit. But the access is typed, structured, and safe. And now you can play around with even pattern matching including deconstruction. 

For example:
~~~java
record Point(int x, int y) {}

try (Arena arena = Arena.ofConfined()) {
    Mem<Point> points = Mem.of(Point.class, arena, 10);
    points.set(0, new Point(10, 20));

    if(points.get(0) instanceof Point(var x, var y){
         IO.println("x: " +x+ "y: " +y);
    }   
}
~~~
Future possibilities is to use the proposed [enhanced local variable declarations](https://openjdk.org/jeps/8357464) that uses deconstruction assignment.
~~~java
Point(var x, var y) = points.get(0);
~~~

## Features
Status of currently implemented features:
* [x] Records with primitives as fields
* [ ] Inner records
* [ ] Records with arrays as fields
* [ ] Pointers as fields
* [ ] Unions

