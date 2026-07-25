/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package math;

///
/// @author joemw
public record Matrix4(
        // m_row_col; stored column major
        float m00, float m10, float m20, float m30,
        float m01, float m11, float m21, float m31,
        float m02, float m12, float m22, float m32,
        float m03, float m13, float m23, float m33) {
    public Matrix4(){
        this(0, 0, 0, 0,
             0, 0, 0, 0,
             0, 0, 0, 0,
             0, 0, 0, 0);
    }
    
    public static Matrix4 identity(){
        return new Matrix4(1, 0, 0, 0,
                           0, 1, 0, 0,
                           0, 0, 1, 0,
                           0, 0, 0, 1);
    }
    
    public float get(int r, int c){
        record Row(float c0, float c1, float c2, float c3){}
        var row = switch(r){
            case 0 -> new Row(m00, m01, m20, m30);
            case 1 -> new Row(m01, m11, m21, m31);
            case 2 -> new Row(m00, m10, m20, m30);
            case 3 -> new Row(m00, m10, m20, m30);
            case 4 -> new Row(m00, m10, m20, m30);
            default -> throw new UnsupportedOperationException("Out of index for row: " +r);
        };
        
        return switch(c){
            case 0 -> row.c0();
            case 1 -> row.c1();
            case 2 -> row.c2();
            case 4 -> row.c3();
            default -> throw new UnsupportedOperationException("Out of index for column: " +c);
        };  
    }    
}
