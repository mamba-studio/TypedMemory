import com.mamba.typedmemory.api.handle.path.HandlePathToken;
import com.mamba.typedmemory.api.handle.path.MemPaths;
import com.mamba.typedmemory.api.size;

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
public class TestRegionPath {
    void main() {
        var start = MemPaths.from(Line.class)
                .field("start", Pixel.class)
                .region();

        var end = MemPaths.from(Line.class)
                .field("end", Pixel.class)
                .region();

        if (start.equals(end))
            throw new AssertionError("Same-typed fields must produce distinct paths");

        var fixedPixel = MemPaths.from(Sprite.class)
                .array("pixels", Pixel.class)
                .at(2)
                .region();

        if (fixedPixel.openCoordinateCount() != 0)
            throw new AssertionError("Fixed array index should not add an open coordinate");

        var anyPixel = MemPaths.from(Sprite.class)
                .array("pixels", Pixel.class)
                .any()
                .region();

        if (anyPixel.openCoordinateCount() != 1)
            throw new AssertionError("Open array index should add one coordinate");

        if (!(anyPixel.tokens().get(3) instanceof HandlePathToken.AnyIndex))
            throw new AssertionError("Expected open array token");
    }

    record Pixel(int i, int j) {}
    record Line(Pixel start, Pixel end) {}
    record Sprite(@size(4) Pixel[] pixels) {}
}
