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
 * A concrete shape plan for a record or union tree containing union fields.
 *
 * @param <R> the declared root type
 */
public sealed interface MemShape<R> permits MemShapeImpl {

    /**
     * Returns the declared root type this shape concretizes.
     *
     * @return the root type
     */
    Class<R> rootType();

    /**
     * Returns the concrete record type that starts this shape.
     *
     * @return the concrete root record type
     */
    Class<? extends Record> concreteType();

    /**
     * Returns top-level choices for this shape.
     *
     * @return immutable choices
     */
    List<ShapeChoice> choices();
}
