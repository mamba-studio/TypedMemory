import com.mamba.typedmemory.api.handle.path.MemShapes;
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

/**
 *
 * @author joemw
 */
public class TestMemShape {
    void main() {
        var shape = MemShapes.of(DeviceRoot.class)
                .union("device", Device.class, device -> device
                    .variant(SmartDevice.class, smart -> smart
                        .field("profile", Profile.class, profile -> profile
                            .field("settings", Settings.class, settings -> settings
                                .union("mode", Mode.class, mode -> mode
                                    .variant(ModeX.class))))))
                .shape();

        IO.println(shape);

        var recordArrayShape = MemShapes.of(Fleet.class)
                .array("profiles", Profile.class, profile -> profile
                    .field("settings", Settings.class, settings -> settings
                        .union("mode", Mode.class, mode -> mode
                            .variant(ModeX.class))))
                .shape();

        IO.println(recordArrayShape);

        var unionArrayShape = MemShapes.of(DeviceArrayRoot.class)
                .unionArray("devices", Device.class, devices -> devices
                    .variant(SmartDevice.class, smart -> smart
                        .field("profile", Profile.class, profile -> profile
                            .field("settings", Settings.class, settings -> settings
                                .union("mode", Mode.class, mode -> mode
                                    .variant(ModeY.class))))))
                .shape();

        IO.println(unionArrayShape);

        var unionRootShape = MemShapes.union(Device.class)
                .variant(SmartDevice.class, smart -> smart
                    .field("profile", Profile.class, profile -> profile
                        .field("settings", Settings.class, settings -> settings
                            .union("mode", Mode.class, mode -> mode
                                .variant(ModeX.class)))))
                .shape();

        IO.println(unionRootShape);

        var siblingShape = MemShapes.of(Root.class)
                .union("left", Left.class, left -> left
                    .variant(A.class, a -> a
                        .union("child", AChild.class, child -> child
                            .variant(AChild1.class))))
                .union("right", Right.class, right -> right
                    .variant(D.class, d -> d
                        .union("mode", SiblingMode.class, mode -> mode
                            .variant(SiblingModeX.class))))
                .shape();

        IO.println(siblingShape);

        var taggedUnionArrayShape = MemShapes.of(TaggedBatch.class)
                .taggedUnionArray(
                        "devices",
                        TaggedDevice.class,
                        "tag",
                        "payload",
                        Device.class,
                        devices -> devices
                            .caseOf(1, SmartDevice.class, smart -> smart
                                .field("profile", Profile.class, profile -> profile
                                    .field("settings", Settings.class, settings -> settings
                                        .union("mode", Mode.class, mode -> mode
                                            .variant(ModeX.class)))))
                            .caseOf(2, SimpleDevice.class))
                .shape();

        IO.println(taggedUnionArrayShape);

        var taggedUnionShape = MemShapes.of(Light.class)
                .taggedUnion("type", "payload", LightPayload.class, light -> light
                    .caseOf(1, AreaLight.class)
                    .caseOf(2, IblLight.class)
                    .caseOf(3, SpotLight.class))
                .shape();

        IO.println(taggedUnionShape);

        var overlayUnionShape = MemShapes.of(InputMapData.class)
                .union("u", InputMapUnion.class, u -> u
                    .overlay()
                    .tagFrom(IntValues.class, "type")
                    .caseOf(0, FloatValue.class)
                    .caseOf(2, IntValues.class))
                .shape();

        IO.println(overlayUnionShape);
    }

    sealed interface Device permits SmartDevice, SimpleDevice {}
    sealed interface Mode permits ModeX, ModeY {}

    record DeviceRoot(Device device) {}
    record SmartDevice(Profile profile) implements Device {}
    record SimpleDevice(int value) implements Device {}
    record Profile(Settings settings) {}
    record Settings(Mode mode) {}
    record ModeX(int value) implements Mode {}
    record ModeY(int value) implements Mode {}
    record Fleet(Profile[] profiles) {}
    record DeviceArrayRoot(Device[] devices) {}
    record TaggedBatch(TaggedDevice[] devices) {}
    record TaggedDevice(int tag, Device payload) {}

    sealed interface LightPayload permits AreaLight, IblLight, SpotLight {}

    record Light(int type, LightPayload payload, float multiplier) {}
    record AreaLight(int id, int shapeidx, int primidx, int padding) implements LightPayload {}
    record IblLight(int tex, int texReflection, int texRefraction, int texTransparency) implements LightPayload {}
    record SpotLight(float ia, float oa, float f, int padding) implements LightPayload {}

    sealed interface InputMapUnion permits FloatValue, IntValues {}

    record InputMapData(InputMapUnion u) {}
    record Float3(float x, float y, float z) {}
    record FloatValue(Float3 value) implements InputMapUnion {}
    record IntValues(int idx, int placeholder0, int placeholder1, int type) implements InputMapUnion {}

    sealed interface Left permits A, B {}
    sealed interface Right permits C, D {}
    sealed interface AChild permits AChild1, AChild2 {}
    sealed interface SiblingMode permits SiblingModeX, SiblingModeY {}

    record Root(Left left, Right right) {}
    record A(AChild child) implements Left {}
    record B(int value) implements Left {}
    record C(int value) implements Right {}
    record D(SiblingMode mode) implements Right {}
    record AChild1(int value) implements AChild {}
    record AChild2(int value) implements AChild {}
    record SiblingModeX(int value) implements SiblingMode {}
    record SiblingModeY(int value) implements SiblingMode {}
}
