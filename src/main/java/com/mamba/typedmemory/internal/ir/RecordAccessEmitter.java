/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mamba.typedmemory.internal.ir;

import com.mamba.typedmemory.api.MemLayout;
import static com.mamba.typedmemory.opcode.OpcodeHelper.CD_MemorySegment;
import com.mamba.typedmemory.api.layout.MemLayoutString;
import java.lang.constant.ClassDesc;
import static java.lang.constant.ConstantDescs.CD_long;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author joemw
 */
public interface RecordAccessEmitter {
    public record FlattenedField(
        List<java.lang.reflect.RecordComponent> path,
        String varHandleName,
        Class<?> type,
        MethodTypeDesc vhGetType,
        MethodTypeDesc vhSetType
        ) {}
    
    
    default List<RecordVarHandlePlan> buildPlans(Class<? extends Record> recordType, MemLayout memLayout) {
        var memLayoutString = MemLayoutString.of(memLayout);
        var varHandleNames = memLayoutString.varHandleNames();

        var plans = new ArrayList<RecordVarHandlePlan>();
        var vhNames = varHandleNames.iterator();

        buildPlansRecursive(recordType, vhNames, plans);

        return plans;
    }
    
    private void buildPlansRecursive(Class<? extends Record> type, Iterator<String> vhNames, List<RecordVarHandlePlan> out) {
        for (var component : type.getRecordComponents()) {
            var fieldType = component.getType();

            if (fieldType.isRecord()) {
                // recursive call
                buildPlansRecursive((Class<? extends Record>) fieldType, vhNames, out);
            } else {
                // leaf: consume next varhandle name
                String vhName = vhNames.next();
                var returnDesc = ClassDesc.ofDescriptor(fieldType.descriptorString());
                var vhType = MethodTypeDesc.of(returnDesc, CD_MemorySegment, CD_long);

                out.add(new RecordVarHandlePlan(vhName, vhType));
            }
        }
    }                     
}
