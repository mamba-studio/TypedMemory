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
package com.mamba.typedmemory.api.handle.path2;

/**
 * Captures the lambda object and the source location that created it.
 *
 * @param <T> owner type
 * @param <R> result type
 */
record CapturedAccessor<T, R>(
        Accessor<T, R> accessor,
        Class<?> callerClass,
        String callerMethod,
        int callerLine,
        int accessorOrdinal) {

    static <T, R> CapturedAccessor<T, R> capture(Accessor<T, R> accessor) {
        return capture(accessor, 0);
    }

    static <T, R> CapturedAccessor<T, R> capture(Accessor<T, R> accessor, int accessorOrdinal) {
        var frame = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(frames -> frames
                .dropWhile(f -> isInternalFrame(f.getDeclaringClass()))
                .findFirst()
                .orElseThrow());

        return new CapturedAccessor<>(
                accessor,
                frame.getDeclaringClass(),
                frame.getMethodName(),
                frame.getLineNumber(),
                accessorOrdinal);
    }

    private static boolean isInternalFrame(Class<?> type) {
        var name = type.getName();
        return type == CapturedAccessor.class
                || name.equals(MemPaths2.class.getName())
                || name.startsWith(MemPaths2.class.getName() + "$")
                || name.equals("com.mamba.typedmemory.api.handle.path2.MemShapes2")
                || name.startsWith("com.mamba.typedmemory.api.handle.path2.MemShapes2$");
    }
}
