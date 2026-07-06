import com.mamba.typedmemory.api.handle.path2.MemShapes2;

public class TestMemShape2 {
    void main() {
        var siblingUnions = MemShapes2.of(Root.class)
                .union(Root::left, Left.class, left -> left
                    .variant(A.class, a -> a
                        .union(A::child, AChild.class, child -> child
                            .variant(AChild1.class))))
                .union(Root::right, Right.class, right -> right
                    .variant(D.class, d -> d
                        .union(D::mode, Mode.class, mode -> mode
                            .variant(ModeX.class))))
                .shape();

        IO.println(siblingUnions);

        var rootUnion = MemShapes2.union(Device.class)
                .variant(SmartDevice.class, smart -> smart
                    .field(SmartDevice::profile, Profile.class))
                .shape();

        IO.println(rootUnion);

        var tagged = MemShapes2.of(Light.class)
                .taggedUnion(Light::kind, Light::payload, LightPayload.class, cases -> cases
                    .caseOf(1, AreaLight.class)
                    .caseOf(2, IblLight.class))
                .shape();

        IO.println(tagged);

        var taggedArray = MemShapes2.of(DeviceBatch.class)
                .taggedUnionArray(DeviceBatch::devices, TaggedDevice.class,
                        TaggedDevice::tag, TaggedDevice::payload, Device.class,
                        cases -> cases
                            .caseOf(1, SmartDevice.class, smart -> smart
                                .field(SmartDevice::profile, Profile.class))
                            .caseOf(2, SimpleDevice.class))
                .shape();

        IO.println(taggedArray);

        var overlay = MemShapes2.of(InputMapData.class)
                .union(InputMapData::u, InputMapUnion.class, u -> u
                    .overlay()
                    .tagFrom(IntValues.class, IntValues::type)
                    .caseOf(0, FloatValue.class)
                    .caseOf(2, IntValues.class))
                .shape();

        IO.println(overlay);
    }

    sealed interface Left permits A, B {}
    sealed interface Right permits C, D {}
    sealed interface AChild permits AChild1, AChild2 {}
    sealed interface Mode permits ModeX, ModeY {}

    record Root(Left left, Right right) {}
    record A(AChild child) implements Left {}
    record B(int value) implements Left {}
    record C(int value) implements Right {}
    record D(Mode mode) implements Right {}
    record AChild1(int value) implements AChild {}
    record AChild2(int value) implements AChild {}
    record ModeX(int value) implements Mode {}
    record ModeY(int value) implements Mode {}

    sealed interface Device permits SmartDevice, SimpleDevice {}
    record SmartDevice(Profile profile) implements Device {}
    record SimpleDevice(int id) implements Device {}
    record Profile(int score) {}
    record TaggedDevice(int tag, Device payload) {}
    record DeviceBatch(TaggedDevice[] devices) {}

    sealed interface LightPayload permits AreaLight, IblLight {}
    record Light(int kind, LightPayload payload) {}
    record AreaLight(float size) implements LightPayload {}
    record IblLight(int texture) implements LightPayload {}

    sealed interface InputMapUnion permits FloatValue, IntValues {}
    record InputMapData(InputMapUnion u) {}
    record Float3(float x, float y, float z) {}
    record FloatValue(Float3 value) implements InputMapUnion {}
    record IntValues(int idx, int[] placeholder, int type) implements InputMapUnion {}
}
