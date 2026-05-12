# TypedMemory

**Typed off-heap memory for Java 25 and greater.**

TypedMemory is a Java library for working with **contiguous off-heap memory** through **strongly typed views**.  It builds on the Java Foreign Function & Memory (FFM) API and lets you map Java record types onto native memory with a simple, expressive API.

Instead of manually managing layouts, offsets, and low-level access patterns for every structure, TypedMemory gives you a type-safe abstraction over memory while still preserving the low-level control needed for systems, interop, graphics, simulation, and data-oriented programming.

```java
import module com.mamba.typedmemory;

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

---

## Why TypedMemory?

Working directly with raw memory in Java is powerful, but often verbose and repetitive.

TypedMemory aims to make off-heap programming feel more natural by providing:

- **Strongly typed views** over contiguous memory
- **Record-based schemas** for describing structured data
- **Explicit control** over allocation and lifetime
- **Low-level layout preservation** for native interop
- **Bulk operations** for fast initialization and copying
- A design that stays close to the **FFM model**, without hiding memory concepts entirely

This makes it useful for:

- Native interop
- Data-oriented programming
- High-performance memory layouts
- Simulation and game/graphics workloads
- Large structured datasets stored off-heap

---

## Features

- Map Java record types to contiguous off-heap memory
- Allocate memory using `Arena`
- Read and write elements with `get(index)` / `set(index, value)`
- Inspect the generated `MemoryLayout`
- Wrap existing `MemorySegment`s
- Reinterpret memory at a given size or address
- Fill, initialize, swap, and copy memory regions
- Support for nested structured data
- Support for fixed-size array fields

---

## Supported Record Fields

TypedMemory derives a memory layout from the components of a Java record. The supported field model is intentionally small and layout-friendly: fields are primitives, fixed-size arrays, or other records.

```java
import module com.mamba.typedmemory;

record Pixel(short x, short y) {}

record Particle(
        int id,                         // primitive field
        float x,
        float y,
        Pixel origin,                   // nested record field
        @size(4) float[] weights,       // fixed-size primitive array
        @size(3) Pixel[] trail          // fixed-size record array
) {}
```

Supported field shapes:

- **Primitive fields**: `boolean`, `byte`, `short`, `char`, `int`, `long`, `float`, and `double`
- **Record fields**: nested records whose own components follow the same rules
- **Array fields**: arrays of primitives or records, annotated with `@size(n)`

Array sizes must be known at layout-generation time, so every array component needs `@size` with a positive value:

```java
record Samples(@size(8) int[] values) {}
```

Unsupported field shapes currently include object references such as `String`, collection types such as `List<T>`, nested arrays such as `int[][]`, pointer fields as first-class typed fields, and unions. If you need an address today, use a `long` field and manage the referenced memory yourself.

---

## Status

TypedMemory is currently **experimental**.

The core API is already usable, but the project is still evolving and may introduce breaking changes as the design is refined.

### Current state

Implemented:
- typed memory allocation
- record layout derivation
- typed get/set access
- wrapping existing segments
- reinterpretation support
- basic bulk operations

### Future features

Planned features to implement:
- Pointer-typed fields beyond using `long` addresses manually
- Unions

---

## Requirements

- **Java 25 or greater** because of the ClassFile API.
- Reinterpret calls, your application requires command flags  to have it work.
    - For a jar: `java --enable-native-access=ALL-UNNAMED -jar app.jar`
    - For a named module: `java --enable-native-access=your.module.name -m your.module.name/com.example.Main`

---

## Build

TypedMemory is built with Maven and targets Java 25.

```xml
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.release>25</maven.compiler.release>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
        </plugin>

        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.2.5</version>
        </plugin>
    </plugins>
</build>
```

Compile the library:

```bash
mvn clean compile
```

Run tests:

```bash
mvn test
```

Build the jar:

```bash
mvn clean package
```

Install TypedMemory into your local Maven repository:

```bash
mvn clean install
```

---

## Use in a Maven Project

After installing TypedMemory locally, add it to your application's `pom.xml`:

```xml
<dependency>
    <groupId>com.mamba</groupId>
    <artifactId>typedmemory</artifactId>
    <version>0.1</version>
</dependency>
```

If your application uses the Java module system, add this to `module-info.java`:

```java
requires com.mamba.typedmemory;
```

---

## Quick Example

```java
import module com.mamba.typedmemory;

record Color(float r, float g, float b, float a) {
    Color(float r, float g, float b) {
        this(r, g, b, 1.0f);
    }
}

void main(){
    try (Arena arena = Arena.ofConfined()) {
        Mem<Color> colors = Mem.of(Color.class, arena, 3);

        colors.set(0, new Color(1f, 0f, 0f));
        colors.set(1, new Color(0f, 1f, 0f));
        colors.set(2, new Color(0f, 0f, 1f));

        Color c = colors.get(1);
        IO.println(c); // Color[r=0.0, g=1.0, b=0.0, a=1.0]
    }
}
```

---

## Example with Structured Records

```java
import module com.mamba.typedmemory;

record Pixel(int i, int j) {}
record Point(byte x, @size(3) Pixel[] y, @size(3) int[] z) {}

void main(){
    try (Arena arena = Arena.ofConfined()) {
        Mem<Point> points = Mem.of(Point.class, arena, 10);

        points.set(0, new Point(
                            (byte) 7,
                            new Pixel[] { new Pixel(1, 2), new Pixel(3, 4), new Pixel(5, 6) },
                            new int[] { 10, 20, 30 }
                        ));

        Point p = points.get(0);
        IO.println(p);
    }
}
```

---

## Layout Introspection

TypedMemory preserves the underlying memory layout, making it easier to inspect and reason about the actual structure stored off-heap.

```java
try (Arena arena = Arena.ofConfined()) {
    Mem<Color> colors = Mem.of(Color.class, arena, 4);
    IO.println(colors.layout());
}
```

This is especially useful when:
- verifying native interop layouts
- checking alignment/padding
- debugging structured off-heap data

---

## Wrapping Existing Memory

TypedMemory can also create typed views over an existing `MemorySegment`.

```java
MemorySegment segment = ...;
long count = ...;

Mem<Color> colors = Mem.wrap(Color.class, segment, count);
```

This is useful when memory comes from:
- native libraries
- external allocators
- pre-existing FFM workflows

---

## Core API

Typical operations include:

```java
Mem<T> mem = Mem.of(MyRecord.class, arena, count);

mem.get(index);
mem.set(index, value);

mem.fill(value);
mem.init(() -> value);
mem.initIndexed(i -> valueFor(i));

mem.copyTo(other);
mem.copyFrom(other);
mem.swap(i, j);

mem.segment();
mem.layout();
mem.size();
mem.type();
```

---

## Design Philosophy

TypedMemory is not trying to replace the FFM API.

Instead, it sits one level above it:

- keeping memory explicit
- keeping layout meaningful
- reducing boilerplate
- improving readability for structured off-heap data

The goal is to make low-level Java memory programming feel **typed, direct, and practical**.

---

## Why Records?

Records provide a natural schema-like model for structured memory.

They offer:

- explicit state description
- stable component ordering
- concise syntax
- strong fit for generated layout/access code

TypedMemory uses this to bridge Java data definitions and low-level memory representation.

---

## Benchmarks

Coming soon.

---

## Use Cases

TypedMemory is especially relevant for:

- graphics and rendering pipelines
- simulation systems
- native interop layers
- binary protocol structures
- high-performance data containers
- experimental data-oriented Java programming

---

## Project Goals

- Make structured off-heap memory easier to use in Java
- Preserve layout-level reasoning and native compatibility
- Offer a clean API without sacrificing control
- Explore how far modern Java can go in low-level programming

---

## Limitations

The following are the limitations
- Java 25 or greater is required.
- No union types yet (any idea on how to implement them?).
- Not all schema shapes may be supported yet. ([let's see how carrier classes will progress](https://openjdk.org/projects/amber/design-notes/beyond-records))
- Arrays in java are mostly heap allocated hence performance will be impacted for arrays as fields in records.

---

## Contributing

Feedback, issues, and suggestions are welcome.

If you are interested in:
- Java FFM
- off-heap data structures
- data-oriented programming
- native interop
- low-level Java performance

then contributions and discussion are highly appreciated.

---

## Repository

GitHub: `mamba-studio/TypedMemory`

---

## License

TypedMemory is licensed under Apache License 2.0
