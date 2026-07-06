import com.mamba.typedmemory.api.handle.path2.MemPaths2;

public class TestLambdaPath2 {
    void main() {
       
        var direct = MemPaths2.from(Point.class)
                .field(Point::p)
                .region();

        IO.println(direct);

        var chain = MemPaths2.from(Point.class)
                .field(point -> point.p().i())
                .region();

        IO.println(chain);

        var pair = MemPaths2.from(Point.class)
                .fields(Point::x, Point::p)
                .build();

        IO.println(pair);
    }

    record Pixel(int i, int j) {}
    record Point(float x, float y, Pixel p) {}
}
