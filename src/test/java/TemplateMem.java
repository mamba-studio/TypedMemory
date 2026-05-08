

import com.mamba.typedmemory.api.size;
import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author joemw
 */
public class TemplateMem {
    public record Pixel(int i, int j){}
    public record Point(byte x, @size(3)Pixel[] y, @size(3) int[] z){} 
      
    public static final MemoryLayout layout = MemoryLayout.structLayout(
                ValueLayout.JAVA_BYTE.withName("x"),
                MemoryLayout.paddingLayout(3),
                MemoryLayout.sequenceLayout(3,
                    MemoryLayout.structLayout(
                        ValueLayout.JAVA_INT.withName("i"),
                        ValueLayout.JAVA_INT.withName("j")
                    ).withName("Pixel")
                ).withName("y"),
                MemoryLayout.sequenceLayout(3,
                    ValueLayout.JAVA_INT
                ).withName("z")
            ).withName("Point");
    
    public static final long STRIDE = layout.byteSize();
    
    public static final VarHandle xPointStructLayoutImplHandle = layout.varHandle(PathElement.groupElement("x"));
    public static final VarHandle iPixelYPointStructLayoutImplHandle = layout.varHandle(PathElement.groupElement("y"),PathElement.sequenceElement(),PathElement.groupElement("i"));
    public static final VarHandle jPixelYPointStructLayoutImplHandle = layout.varHandle(PathElement.groupElement("y"),PathElement.sequenceElement(),PathElement.groupElement("j"));
    public static final VarHandle zPointStructLayoutImplHandle = layout.varHandle(PathElement.groupElement("z"),PathElement.sequenceElement());

    
    private final MemorySegment segment;
    
    public TemplateMem(MemorySegment segment){
        this.segment = segment;
    }
    
    public static void main(String... args){
        try(Arena arena = Arena.ofConfined()){
            var mem = arena.allocate(layout, 10);
            var point = new Point(
                    (byte)10,
                    new Pixel[]{new Pixel(0, 0), new Pixel(0, 0), new Pixel(0, 0)},
                    new int[]{3, 1, 2});
            var structMem = new TemplateMem(mem);
            structMem.set(2, point);
            
            Point p = structMem.get(2);
            
            IO.println(Arrays.toString(p.z));
        }
    }
    
    public void set(long index, Point t){
        Objects.requireNonNull(t);
        Objects.requireNonNull(t.y());
        Objects.requireNonNull(t.z());
        
        if (t.y().length != 3) throw new IllegalArgumentException("Point.y length must be 3");
        if (t.z().length != 3) throw new IllegalArgumentException("Point.z length must be 3");

        byte x = t.x();
        Pixel[] y = t.y();
        int[] z = t.z();
        
        xPointStructLayoutImplHandle.set(this.segment, index * STRIDE, x);

        for (long span0 = 0; span0 < 3; span0++) {
            Pixel p = y[(int) span0];
            Objects.requireNonNull(p);
            
            int i = p.i();
            int j = p.j();

            iPixelYPointStructLayoutImplHandle.set(this.segment, index * STRIDE, span0, i);
            jPixelYPointStructLayoutImplHandle.set(this.segment, index * STRIDE, span0, j);
        }

        for (long span0 = 0; span0 < 3; span0++) {
            int value0 = z[(int) span0];
            zPointStructLayoutImplHandle.set(this.segment, index * STRIDE, span0, value0);
        }
    }
    
    public Point get(long index){
        byte x = (byte) xPointStructLayoutImplHandle.get(this.segment, index * STRIDE);
        Pixel[] y = new Pixel[3];
        for(long span0 = 0; span0<y.length; span0++){
            y[(int) span0] = new Pixel(
                (int) iPixelYPointStructLayoutImplHandle.get(this.segment, index * STRIDE, span0),
                (int) jPixelYPointStructLayoutImplHandle.get(this.segment, index * STRIDE, span0)
            );
        }
        
        int[] z = new int[3];
        for(long span0 = 0; span0<z.length; span0++){
            z[(int) span0] = (int)zPointStructLayoutImplHandle.get(this.segment, index * STRIDE, span0);
        }
        return new Point(x, y, z);
    }
    
}
