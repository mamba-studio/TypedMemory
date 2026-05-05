/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import com.mamba.typedmemory.api.Mem;
import java.lang.foreign.Arena;
import java.util.Random;

/**
 *
 * @author joemw
 */
public class TestIR {
    public void main() throws Exception{
        test2();
       
    }
        
    public void test1(){    
        try (Arena arena = Arena.ofConfined()) {
            long size = 10;
            record Color(float r, float g, float b){}
            
            Random random = new Random();
            
            var colors = Mem.of(Color.class, arena, size)
                    .init(()-> new Color(random.nextFloat(0, 1),
                                         random.nextFloat(0, 1),
                                         random.nextFloat(0, 1)));
            colors.query().forEach(IO::println);
            
            IO.println("FILTERED: ");
            
            colors.query()
                    .filter(c -> c.r > 0.5f)
                    .forEach(IO::println);
            
        }        
    } 
  
    public void test2(){
        record Student(int id, int score, boolean active){}
        long size = 10;
        Random random = new Random();
        
        try (Arena arena = Arena.ofConfined()) {
            var students = Mem.of(Student.class, arena, size)
                    .init(() -> new Student(
                            random.nextInt(0, 100),
                            random.nextInt(0, 100),
                            random.nextBoolean()));
            
            students.query()
                    .forEach(IO::println);
            IO.println("COUNT:");
            long count = students.query()
                    .map(Student::score)
                    .filter(score -> score>50)
                    .count();
            
            IO.println(count);
        }
    }
}
