/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import com.mamba.typedmemory.api.Mem;
import java.lang.foreign.Arena;
import java.lang.invoke.MethodHandles;
import java.time.Duration;

/**
 *
 * @author joemw
 */
public class TestPipelines {
    record Student(int id, int score, boolean active){}
    void main(){
        test1();
    }
    
    public void test1(){
        
        var start = System.nanoTime();
        try (Arena arena = Arena.ofConfined()) {
            var students = Mem.of(Student.class, MethodHandles.lookup(), arena, 80_000_000).init(()-> new Student(0, nextInt(0, 100), true));
            IO.println(students
                    .query()
                    .map(Student::score)
                    .filter(score -> score >= 50)
                    .count());     
        }     
        var end = System.nanoTime();
        
        var duration = Duration.ofNanos(end - start);                
        IO.println(human(duration));
    }
    
    //Random class has inefficient random functions, hence just use Math.random here
    private int nextInt(int min, int max){
        return (int) ((Math.random() * (max - min)) + min);
    }
    
    private String human(Duration d) {
        long days = d.toDays();
        d = d.minusDays(days);

        long hours = d.toHours();
        d = d.minusHours(hours);

        long minutes = d.toMinutes();
        d = d.minusMinutes(minutes);

        long seconds = d.toSeconds();
        d = d.minusSeconds(seconds);

        long millis = d.toMillis();

        StringBuilder sb = new StringBuilder();

        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0) sb.append(seconds).append("s ");
        if (millis > 0 || sb.length() == 0) sb.append(millis).append("ms");

        return sb.toString().trim();
    }
}
