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

import com.mamba.typedmemory.api.size;

/**
 *
 * @author joemw
 */
public class TestPath2 {
    public record Pixel(int i, int j) {}
    public record Point(float x, float y, @size(4) Pixel[] pixels) {}
    
    public void main(){
        var fixed = MemPaths.from(Point.class)
                .array("pixels", Pixel.class)
                .at(2)
                .region();
        
        var fixed2 = MemPaths.from(Point.class)
                .array("pixels", Pixel.class)
                .at(2)
                .region();
        
        var open = MemPaths.from(Point.class)
                .array("pixels", Pixel.class)
                .any()  
                .region();
        
        IO.println(fixed.equals(fixed2));        // true
        IO.println(fixed.equals(open));          // false
        IO.println(fixed.openCoordinateCount()); // 0
        IO.println(open.openCoordinateCount());  // 1
        IO.println(open.tokens());
        
        // Optional sanity check: the open coordinate token is present.
        IO.println(open.tokens().get(3) instanceof HandlePathToken.AnyIndex);
        
        IO.println(fixed);
    }
}
