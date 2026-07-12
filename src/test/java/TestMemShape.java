import com.mamba.typedmemory.api.handle.path.MemShapes;
import com.mamba.typedmemory.api.handle.path.MemShapeCatalog;
import com.mamba.typedmemory.api.handle.path.TagAdapter;
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

        var catalog = MemShapeCatalog.of(DeviceRoot.class);
        IO.println(catalog);
        var firstPath = catalog.paths().iterator().next();
        var firstShape = catalog.shapesAt(firstPath).keySet().iterator().next();
        IO.println(catalog.contains(firstPath, firstShape));

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
                        TagAdapter.ofInt("tag", DeviceTag.class, DeviceTag::fromNative),
                        "payload",
                        Device.class,
                        devices -> devices
                            .caseOf(new DeviceTag(1), SmartDevice.class, smart -> smart
                                .field("profile", Profile.class, profile -> profile
                                    .field("settings", Settings.class, settings -> settings
                                        .union("mode", Mode.class, mode -> mode
                                            .variant(ModeX.class)))))
                            .caseOf(new DeviceTag(2), SimpleDevice.class))
                .shape();

        IO.println(taggedUnionArrayShape);

        var taggedUnionShape = MemShapes.of(Light.class)
                .taggedUnion(
                        TagAdapter.ofInt("type", LightTag.class, LightTag::fromNative),
                        "payload",
                        LightPayload.class,
                        light -> light
                            .caseOf(new LightTag(1), AreaLight.class)
                            .caseOf(new LightTag(2), IblLight.class)
                            .caseOf(new LightTag(3), SpotLight.class))
                .shape();

        IO.println(taggedUnionShape);

        var rangeTaggedUnionShape = MemShapes.of(RangePacket.class)
                .taggedUnion(
                        TagAdapter.ofInt("rawCode", RangeTag.class, RangeTag::fromNative),
                        "payload",
                        RangePayload.class,
                        packet -> packet
                            .caseOf(new RangeTag("small"), SmallRange.class)
                            .caseOf(new RangeTag("large"), LargeRange.class))
                .shape();

        IO.println(rangeTaggedUnionShape);

        var overlayUnionShape = MemShapes.of(InputMapData.class)
                .union("u", InputMapUnion.class, u -> u
                    .overlay()
                    .tagFrom(IntValues.class, TagAdapter.ofInt("type", InputMapTag.class, InputMapTag::fromNative))
                    .caseOf(new InputMapTag(0), FloatValue.class)
                    .caseOf(new InputMapTag(2), IntValues.class))
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
    record DeviceTag(int bucket) {
        static DeviceTag fromNative(int raw) {
            return new DeviceTag(raw > 1 ? 2 : 1);
        }
    }

    sealed interface LightPayload permits AreaLight, IblLight, SpotLight {}

    record Light(int type, LightPayload payload, float multiplier) {}
    record LightTag(int type) {
        static LightTag fromNative(int raw) {
            return new LightTag(raw);
        }
    }
    record AreaLight(int id, int shapeidx, int primidx, int padding) implements LightPayload {}
    record IblLight(int tex, int texReflection, int texRefraction, int texTransparency) implements LightPayload {}
    record SpotLight(float ia, float oa, float f, int padding) implements LightPayload {}

    sealed interface RangePayload permits SmallRange, LargeRange {}

    record RangePacket(int rawCode, RangePayload payload) {}
    record RangeTag(String bucket) {
        static RangeTag fromNative(int raw) {
            return new RangeTag(raw <= 22 ? "small" : "large");
        }
    }
    record SmallRange(int value) implements RangePayload {}
    record LargeRange(long value) implements RangePayload {}

    sealed interface InputMapUnion permits FloatValue, IntValues {}

    record InputMapData(InputMapUnion u) {}
    record Float3(float x, float y, float z) {}
    record FloatValue(Float3 value) implements InputMapUnion {}
    record IntValues(int idx, int placeholder0, int placeholder1, int type) implements InputMapUnion {}
    record InputMapTag(int type) {
        static InputMapTag fromNative(int raw) {
            return new InputMapTag(raw);
        }
    }

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
