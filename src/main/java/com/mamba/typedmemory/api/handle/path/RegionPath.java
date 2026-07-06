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
package com.mamba.typedmemory.api.handle.path;

import java.util.List;

/**
 * A path from a root record to a nested record memory region.
 *
 * @param <R> the root record type
 * @param <T> the nested record region type
 */
public sealed interface RegionPath<R extends Record, T extends Record>
        permits RegionPathImpl {

    /**
     * Returns the root element type.
     *
     * @return the root type
     */
    Class<R> rootType();

    /**
     * Returns the focused record-region type.
     *
     * @return the leaf region type
     */
    Class<T> leafType();

    /**
     * Returns the structural path tokens.
     *
     * @return immutable path tokens
     */
    List<HandlePathToken> tokens();

    /**
     * Returns how many array coordinates are open and must be provided when a
     * handle is accessed.
     *
     * @return open coordinate count
     */
    int openCoordinateCount();
}
