
import com.mamba.typedmemory.api.Mem;
import com.mamba.typedmemory.api.MemLayout;
import com.mamba.typedmemory.api.Ptr;
import com.mamba.typedmemory.api.RawMem;
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
public class TestNull {
    record IntValue(int val){}
    
    void main(){
        Ptr ptr = RawMem.of(IntValue.class);
        IO.println(ptr.equals(Ptr.NULL));
        
        record Point(@size(4)RawMem<IntValue>[] mem){}
        
        MemLayout.printTypeSummary(Point.class);
    }
}
