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
package com.mamba.typedmemory.opcode;

import com.mamba.typedmemory.opcode.LocalAllocator.LocalBinding;
import com.mamba.typedmemory.opcode.expr.Expr;
import com.mamba.typedmemory.opcode.expr.values.LocalExpr;
import java.util.LinkedHashMap;
import java.util.Map;

public final class LocalBindings {
    private final Map<String, LocalBinding> bindings = new LinkedHashMap<>();

    public void put(LocalBinding binding) {
        bindings.put(binding.name(), binding);
    }

    public LocalBinding get(String name) {
        var binding = bindings.get(name);
        if (binding == null) {
            throw new IllegalStateException("Unbound local: " + name);
        }
        return binding;
    }

    public Expr expr(String name) {
        return new LocalExpr(get(name));
    }
}
