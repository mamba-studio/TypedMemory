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

import java.util.Objects;

/**
 * Experimental lambda-based path builder.
 */
public final class MemPaths2 {
    private MemPaths2() {
    }

    public static <T extends Record> Builder<T> from(Class<T> rootType) {
        return new Builder<>(rootType);
    }

    public static final class Builder<T extends Record> {
        private final Class<T> rootType;

        private Builder(Class<T> rootType) {
            this.rootType = Objects.requireNonNull(rootType);
        }

        public <U> PendingRegion<T, U> field(Accessor<T, U> accessor) {
            return new PendingRegion<>(rootType, CapturedAccessor.capture(accessor));
        }

        public <A, B> PendingPair<T, A, B> fields(
                Accessor<T, A> first,
                Accessor<T, B> second) {
            return new PendingPair<>(
                    rootType,
                    CapturedAccessor.capture(first, 0),
                    CapturedAccessor.capture(second, 1));
        }
    }

    public static final class PendingPair<T extends Record, A, B> {
        private final Class<T> rootType;
        private final CapturedAccessor<T, A> first;
        private final CapturedAccessor<T, B> second;

        private PendingPair(
                Class<T> rootType,
                CapturedAccessor<T, A> first,
                CapturedAccessor<T, B> second) {
            this.rootType = rootType;
            this.first = first;
            this.second = second;
        }

        public ResolvedAccessorPair build() {
            return new ResolvedAccessorPair(
                    LambdaAccessorResolver.resolve(rootType, first),
                    LambdaAccessorResolver.resolve(rootType, second));
        }
    }

    public static final class PendingRegion<T extends Record, U> {
        private final Class<T> rootType;
        private final CapturedAccessor<T, U> accessor;

        private PendingRegion(Class<T> rootType, CapturedAccessor<T, U> accessor) {
            this.rootType = rootType;
            this.accessor = accessor;
        }

        public RegionPath2<T, U> region() {
            var resolved = LambdaAccessorResolver.resolve(rootType, accessor);
            @SuppressWarnings("unchecked")
            var leafType = (Class<U>) resolved.leafType();
            return new RegionPath2<>(rootType, leafType, resolved.fields());
        }
    }
}
