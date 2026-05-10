

import com.mamba.typedmemory.api.Mem;
import com.mamba.typedmemory.api.MemLayout;
import com.mamba.typedmemory.api.size;
import java.lang.foreign.Arena;
import java.util.Arrays;

/**
 *
 * @author joemw
 */
public class TestMem {    
    record Pixel(int x, @size(2)double[] y){}
    
    void main(){
        try (var arena = Arena.ofConfined()) {
            var colors = Mem.of(Pixel.class, arena, 1000);
            var pixel = new Pixel(3, new double[]{3, 23});
            colors.set(5, pixel);
            
            if(colors.get(5) instanceof Pixel(int x, double[] y))
                IO.println(x+ " " +Arrays.toString(y));
            
            IO.println(MemLayout.memorySummary(colors));
        }
    }
}
