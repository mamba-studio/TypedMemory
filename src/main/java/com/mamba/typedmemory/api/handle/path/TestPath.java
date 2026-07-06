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
public class TestPath {
    public record Pixel(int i, int j){}
    public record Point(float x, float y, Pixel p){}
    
    public void main(){
        var start = MemPaths.from(Point.class)
                .field("p", Pixel.class).region();
        
        var start2 = MemPaths.from(Point.class)
                .field("p", Pixel.class).region();
        
        IO.println(start.equals(start2));
    }
}
