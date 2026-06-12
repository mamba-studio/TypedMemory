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
package com.mamba.typedmemory.api.path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author joemw
 */
public class MemPathValidation {

    public static boolean validate(Class<?> clazz) {
        var visiting = Collections.newSetFromMap(new IdentityHashMap<Class<?>, Boolean>());
        var validated = Collections.newSetFromMap(new IdentityHashMap<Class<?>, Boolean>());
        return validate(clazz, visiting, validated);
    }
    
    private static boolean validate(Class<?> clazz, Set<Class<?>> visiting, Set<Class<?>> validated) {
        if (validated.contains(clazz))
            return true;
        
        if (!visiting.add(clazz))
            throw new UnsupportedOperationException("Recursive type cycle detected at: " + clazz.getSimpleName() + ".class");
        
        try {
        if (clazz.isInterface() && clazz.isSealed()) {
            for (var permittedClass : clazz.getPermittedSubclasses()) {
                if (!(permittedClass.isInterface() && permittedClass.isSealed() || permittedClass.isRecord()))
                    return false;                

                if (!validate(permittedClass, visiting, validated)) 
                    return false;                
            }

            validated.add(clazz);
            return true;
        }

        if (clazz.isRecord()) {
            for (var recordComponent : clazz.getRecordComponents()) {
                var type = recordComponent.getType();

                var valid = switch (type) {
                    case Class<?> primitive when primitive.isPrimitive() -> true;
                    case Class<?> record when record.isRecord() -> validate(record, visiting, validated);
                    case Class<?> interfaceType when interfaceType.isInterface() && interfaceType.isSealed() -> validate(interfaceType, visiting, validated);
                    case Class<?> array when array.isArray() && array.getComponentType().isPrimitive() -> true;
                    case Class<?> array when array.isArray() -> validate(array.getComponentType(), visiting, validated);
                    default -> throw new UnsupportedOperationException("This type: " + type.getSimpleName() + ".class, is invalid and is located in parent type: " +clazz.getSimpleName()+ ".class");
                };
                //we do this to validate all children first not first depth and return true which would result to a super hidden bug
                if (!valid) 
                    return false;                
            }

            validated.add(clazz);
            return true;
        }

        throw new UnsupportedOperationException("This type: " + clazz.getSimpleName() + ".class, is not supported.");
        }
        finally {
            visiting.remove(clazz);
        }
    }
    
    public static List<MemPath> discover(Class<?> clazz) {
        return discoverResolvablePaths(clazz);
    }
    
    public static List<MemPath> discoverResolvablePaths(Class<?> clazz) {
        validate(clazz);
        
        var paths = new LinkedHashSet<MemPath>();
        discoverResolvablePaths(clazz, new ArrayList<>(), paths, true);
        return List.copyOf(paths);
    }
    
    private static void discoverResolvablePaths(Class<?> clazz, ArrayList<Object> tokens, LinkedHashSet<MemPath> paths, boolean includeType) {
        if (includeType)
            tokens.add(clazz);

        if (clazz.isInterface() && clazz.isSealed()) {
            for (var permittedClass : clazz.getPermittedSubclasses())
                discoverResolvablePaths(permittedClass, tokens, paths, true);
        }
        else if (clazz.isRecord()) {
            if (!requiresDisambiguation(clazz)) {
                paths.add(MemPath.of(tokens.toArray()));
            }
            else {
            for (var recordComponent : clazz.getRecordComponents()) {
                var type = pathType(recordComponent.getType());
                var duplicateSiblingType = requiresSiblingDisambiguation(clazz, recordComponent.getType());
                var childNeedsDisambiguation = requiresDisambiguation(type);
                
                if (duplicateSiblingType || childNeedsDisambiguation) {
                    if (duplicateSiblingType)
                        tokens.add(recordComponent.getName());
                    
                    discoverResolvablePaths(type, tokens, paths, true);
                    
                    if (duplicateSiblingType)
                        tokens.removeLast();
                }
            }
            }
        }
        else if (clazz.isPrimitive()) {
            paths.add(MemPath.of(tokens.toArray()));
        }
        
        if (includeType)
            tokens.removeLast();
    }
    
    private static Class<?> pathType(Class<?> type) {
        if (type.isArray())
            return type.getComponentType();
        return type;
    }
    
    private static boolean requiresDisambiguation(Class<?> clazz) {
        return switch (clazz) {
            case Class<?> primitive when primitive.isPrimitive() -> false;
            case Class<?> array when array.isArray() -> requiresDisambiguation(array.getComponentType());
            case Class<?> sealedInterface when sealedInterface.isInterface() && sealedInterface.isSealed() -> true;
            case Class<?> record when record.isRecord() -> {
                for (var recordComponent : record.getRecordComponents()) {
                    if (requiresSiblingDisambiguation(record, recordComponent.getType()))
                        yield true;
                    if (requiresDisambiguation(pathType(recordComponent.getType())))
                        yield true;
                }
                yield false;
            }
            default -> false;
        };
    }
    
    private static boolean requiresSiblingDisambiguation(Class<?> recordType, Class<?> type) {
        return !pathType(type).isPrimitive() && !isUniqueComponentType(recordType, type);
    }
    
    private static boolean isUniqueComponentType(Class<?> recordType, Class<?> type) {
        var count = 0;        
        for (var recordComponent : recordType.getRecordComponents()) {
            if (recordComponent.getType() == type)
                count++;
        }        
        return count == 1;
    }
    
}
