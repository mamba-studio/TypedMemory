/*
 * Copyright 2026 joemw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/// Public API for creating strongly-typed views over contiguous off-heap memory.
///
///
/// The package centers on {@link com.mamba.typedmemory.api.Mem}, which maps
/// record types to structured {@link java.lang.foreign.MemoryLayout}s and
/// stores record values in {@link java.lang.foreign.MemorySegment}s. It also
/// provides helpers for deriving and inspecting layouts, and
/// {@link com.mamba.typedmemory.api.MemTransforms primitive-specialized
/// transformations} for bulk initialization. Record schemas may
/// use {@link com.mamba.typedmemory.api.size @size} to declare fixed array
/// lengths and {@link com.mamba.typedmemory.api.align @align} to request an
/// explicit struct alignment.
///
/// @since 0.1
package com.mamba.typedmemory.api;
