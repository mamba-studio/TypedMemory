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


