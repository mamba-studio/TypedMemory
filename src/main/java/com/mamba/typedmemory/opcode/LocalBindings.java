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
