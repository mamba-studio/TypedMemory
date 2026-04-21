/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package test.op;

import java.lang.constant.ClassDesc;
import static java.lang.constant.ConstantDescs.INIT_NAME;
import java.lang.constant.MethodTypeDesc;

/**
 *
 * @author joemw
 */
public sealed interface MemberRef {
    public record FieldRef(ClassDesc owner, String name, ClassDesc type) implements MemberRef {}
    public record MethodRef(ClassDesc owner, String name, MethodTypeDesc type) implements MemberRef {}
    public record ConstructorRef(ClassDesc owner, MethodTypeDesc type) implements MemberRef {
        public String name() {return INIT_NAME;}
    }

    public ClassDesc owner();
}
