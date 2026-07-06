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
package com.mamba.typedmemory.api.handle.path;

/**
 *
 * @author joemw
 */
public class TestUnion {
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

    public void main() {
        MemShape<Root> shape = MemShapes.of(Root.class)
                .union("left", Left.class, left -> left
                    .variant(A.class, a -> a
                        .union("child", AChild.class, child -> child
                            .variant(AChild1.class))))
                .union("right", Right.class, right -> right
                    .variant(D.class, d -> d
                        .union("mode", Mode.class, mode -> mode
                            .variant(ModeX.class))))
                .shape();

        IO.println(shape);
    }
}
