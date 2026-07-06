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

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeElement;
import java.lang.classfile.MethodModel;
import java.lang.classfile.instruction.InvokeDynamicInstruction;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.classfile.instruction.LineNumber;
import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Resolves non-serializable record accessor lambdas by inspecting caller
 * bytecode. This is deliberately narrow and experimental.
 */
final class LambdaAccessorResolver {
    private LambdaAccessorResolver() {
    }

    static ResolvedAccessor resolve(Class<?> rootType, CapturedAccessor<?, ?> captured) {
        return resolve(rootType, captured, null);
    }

    static ResolvedAccessor resolve(
            Class<?> rootType,
            CapturedAccessor<?, ?> captured,
            Class<?> expectedLeafType) {
        Objects.requireNonNull(rootType);
        Objects.requireNonNull(captured);

        var caller = parse(captured.callerClass());
        var callerMethod = findMethod(caller.methods(), captured.callerMethod());
        var candidates = findInvokeDynamicNearLine(
                caller.methods(),
                callerMethod,
                captured.callerLine());

        var resolved = new ArrayList<CandidateAccessor>();
        IllegalArgumentException lastFailure = null;
        for (var candidate : candidates) {
            try {
                var candidateAccessor = resolveCandidate(rootType, captured.callerClass(), candidate.indy());
                if (expectedLeafType == null || candidateAccessor.leafType() == expectedLeafType) {
                    resolved.add(new CandidateAccessor(candidateAccessor, candidate.line()));
                }
            } catch (IllegalArgumentException ex) {
                lastFailure = ex;
            }
        }
        resolved.sort(Comparator.comparingInt(candidate -> Math.abs(candidate.line() - captured.callerLine())));

        if (resolved.isEmpty()) {
            if (lastFailure != null)
                throw lastFailure;
            throw new IllegalArgumentException("No accessor lambda call site starts at " + rootType.getName());
        }
        if (captured.accessorOrdinal() < 0 || captured.accessorOrdinal() >= resolved.size()) {
            throw new IllegalArgumentException(
                    "Accessor ordinal %d is outside %d accessor call sites for %s near line %d".formatted(
                            captured.accessorOrdinal(),
                            resolved.size(),
                            rootType.getName(),
                            captured.callerLine()));
        }
        return resolved.get(captured.accessorOrdinal()).resolved();
    }

    private static ResolvedAccessor resolveCandidate(
            Class<?> rootType,
            Class<?> callerClass,
            InvokeDynamicInstruction indy) {
        var impl = implementationMethod(indy);

        if (!impl.owner().equals(ClassDesc.ofDescriptor(callerClass.descriptorString()))) {
            return resolveDirectAccessor(rootType, impl);
        }

        return resolveAccessorChain(rootType, callerClass, impl);
    }

    private static ResolvedAccessor resolveDirectAccessor(Class<?> rootType, DirectMethodHandleDesc impl) {
        var owner = classFor(impl.owner());
        var name = impl.methodName();
        if (owner != rootType) {
            throw new IllegalArgumentException(
                    "Accessor starts at %s, not %s".formatted(owner.getName(), rootType.getName()));
        }

        var component = component(owner, name);
        var leaf = component.getType();
        return new ResolvedAccessor(rootType, leaf, List.of(name));
    }

    private static ResolvedAccessor resolveAccessorChain(
            Class<?> rootType,
            Class<?> callerClass,
            DirectMethodHandleDesc impl) {
        var caller = parse(callerClass);
        var lambdaMethod = findMethod(caller.methods(), impl.methodName());
        var fields = new ArrayList<String>();
        var currentType = rootType;

        for (var element : lambdaMethod.code().orElseThrow().elementList()) {
            if (!(element instanceof InvokeInstruction invoke))
                continue;

            var owner = classFor(invoke.owner().asSymbol());
            var name = invoke.name().stringValue();

            if (owner == Object.class)
                continue;
            if (currentType.isPrimitive() && isBoxingValueOf(currentType, owner, name))
                continue;
            if (owner != currentType) {
                throw new IllegalArgumentException(
                        "Unsupported accessor chain: expected owner %s but found %s.%s".formatted(
                                currentType.getName(), owner.getName(), name));
            }

            var component = component(owner, name);
            fields.add(name);
            currentType = component.getType();
        }

        if (fields.isEmpty())
            throw new IllegalArgumentException("Accessor lambda did not contain a record accessor chain");

        return new ResolvedAccessor(rootType, currentType, fields);
    }

    private static boolean isBoxingValueOf(Class<?> primitive, Class<?> owner, String name) {
        if (!name.equals("valueOf"))
            return false;
        return (primitive == int.class && owner == Integer.class)
                || (primitive == long.class && owner == Long.class)
                || (primitive == float.class && owner == Float.class)
                || (primitive == double.class && owner == Double.class)
                || (primitive == byte.class && owner == Byte.class)
                || (primitive == short.class && owner == Short.class)
                || (primitive == boolean.class && owner == Boolean.class)
                || (primitive == char.class && owner == Character.class);
    }

    private static DirectMethodHandleDesc implementationMethod(InvokeDynamicInstruction indy) {
        var args = indy.bootstrapArgs();
        if (args.size() < 2 || !(args.get(1) instanceof DirectMethodHandleDesc impl)) {
            throw new IllegalArgumentException("invokedynamic is not a LambdaMetafactory accessor call site");
        }
        return impl;
    }

    private static List<CandidateInvokeDynamic> findInvokeDynamicNearLine(
            List<MethodModel> methods,
            MethodModel method,
            int line) {
        var matches = collectInvokeDynamicNearLine(method, line);
        if (matches.isEmpty()) {
            matches = collectInvokeDynamic(method);
        }
        if (matches.isEmpty()) {
            matches = new ArrayList<>();
            for (var candidate : methods) {
                matches.addAll(collectInvokeDynamicNearLine(candidate, line));
            }
        }
        if (matches.isEmpty()) {
            for (var candidate : methods) {
                matches.addAll(collectInvokeDynamic(candidate));
            }
        }

        if (matches.isEmpty()) {
            throw new IllegalArgumentException(
                    "No accessor lambda call site found near %s:%d".formatted(
                            method.methodName().stringValue(), line));
        }
        return matches;
    }

    private static List<CandidateInvokeDynamic> collectInvokeDynamic(MethodModel method) {
        var code = method.code().orElseThrow();
        var matches = new ArrayList<CandidateInvokeDynamic>();
        var currentLine = -1;
        var accessorDesc = ClassDesc.ofDescriptor(Accessor.class.descriptorString());

        for (CodeElement element : code.elementList()) {
            if (element instanceof LineNumber lineNumber) {
                currentLine = lineNumber.line();
                continue;
            }
            if (element instanceof InvokeDynamicInstruction indy
                    && returns(indy.typeSymbol(), accessorDesc)) {
                matches.add(new CandidateInvokeDynamic(indy, currentLine));
            }
        }
        return matches;
    }

    private static List<CandidateInvokeDynamic> collectInvokeDynamicNearLine(
            MethodModel method,
            int line) {
        var code = method.code().orElseThrow();
        var matches = new ArrayList<CandidateInvokeDynamic>();
        var currentLine = -1;
        var accessorDesc = ClassDesc.ofDescriptor(Accessor.class.descriptorString());

        for (CodeElement element : code.elementList()) {
            if (element instanceof LineNumber lineNumber) {
                currentLine = lineNumber.line();
                continue;
            }
            if (Math.abs(currentLine - line) <= 2
                    && element instanceof InvokeDynamicInstruction indy
                    && returns(indy.typeSymbol(), accessorDesc)) {
                matches.add(new CandidateInvokeDynamic(indy, currentLine));
            }
        }
        return matches;
    }

    private record CandidateInvokeDynamic(InvokeDynamicInstruction indy, int line) {
    }

    private record CandidateAccessor(ResolvedAccessor resolved, int line) {
    }

    private static boolean returns(MethodTypeDesc methodType, ClassDesc returnType) {
        return methodType.returnType().equals(returnType);
    }

    private static MethodModel findMethod(List<MethodModel> methods, String name) {
        for (var method : methods) {
            if (method.methodName().stringValue().equals(name))
                return method;
        }
        throw new IllegalArgumentException("Cannot find method in class file: " + name);
    }

    private static java.lang.classfile.ClassModel parse(Class<?> type) {
        var resource = type.getSimpleName() + ".class";
        try (var in = type.getResourceAsStream(resource)) {
            if (in == null)
                throw new IllegalArgumentException("Cannot read class bytes for " + type.getName());
            return ClassFile.of().parse(in.readAllBytes());
        } catch (IOException ex) {
            throw new IllegalArgumentException("Cannot read class bytes for " + type.getName(), ex);
        }
    }

    private static RecordComponent component(Class<?> recordType, String name) {
        if (!recordType.isRecord())
            throw new IllegalArgumentException("Accessor owner is not a record: " + recordType.getName());
        for (var component : recordType.getRecordComponents()) {
            if (component.getName().equals(name))
                return component;
        }
        throw new IllegalArgumentException(
                "%s.%s is not a record component accessor".formatted(recordType.getSimpleName(), name));
    }

    private static Class<?> classFor(ClassDesc desc) {
        try {
            var descriptor = desc.descriptorString();
            if (descriptor.startsWith("L") && descriptor.endsWith(";")) {
                return Class.forName(descriptor.substring(1, descriptor.length() - 1).replace('/', '.'));
            }
            return Class.forName(descriptor.replace('/', '.'));
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Cannot resolve class descriptor: " + desc, ex);
        }
    }
}
