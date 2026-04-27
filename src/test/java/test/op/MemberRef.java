/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package test.op;

import java.lang.constant.ClassDesc;
import static java.lang.constant.ConstantDescs.INIT_NAME;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.RecordComponent;

/**
 *
 * @author joemw
 */
public sealed interface MemberRef {
    public record FieldRef(ClassDesc owner, String name, ClassDesc type) implements MemberRef {}
    public record MethodRef(ClassDesc owner, String name, MethodTypeDesc type) implements MemberRef {
        public static MethodRef recordAccessor(RecordComponent component) {
            return new MethodRef(
                    ClassDesc.ofDescriptor(component.getDeclaringRecord().descriptorString()),
                    component.getName(),
                    MethodTypeDesc.of(ClassDesc.ofDescriptor(component.getType().descriptorString()))
            );
        }
    }
    public record ConstructorRef(ClassDesc owner, MethodTypeDesc type) implements MemberRef {
        public String name() {return INIT_NAME;}
    }

    public ClassDesc owner();
}
