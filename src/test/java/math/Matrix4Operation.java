package math;

/**
 *
 * @author joemw
 */
public class Matrix4Operation {
    public Matrix4 add(Matrix4 m1, Matrix4 m2){
        return new Matrix4(
                m1.m00() + m2.m00(), m1.m10() + m2.m10(), m1.m20() + m2.m20(), m1.m30() + m2.m30(),
                m1.m01() + m2.m01(), m1.m11() + m2.m11(), m1.m21() + m2.m21(), m1.m31() + m2.m31(),
                m1.m02() + m2.m02(), m1.m12() + m2.m12(), m1.m22() + m2.m22(), m1.m32() + m2.m32(),
                m1.m03() + m2.m03(), m1.m13() + m2.m13(), m1.m23() + m2.m23(), m1.m33() + m2.m33());
    }
    
    public Matrix4 mul(Matrix4 m1, Matrix4 m2){
        return new Matrix4(
                m1.m00() + m2.m00(), m1.m10() + m2.m10(), m1.m20() + m2.m20(), m1.m30() + m2.m30(),
                m1.m01() + m2.m01(), m1.m11() + m2.m11(), m1.m21() + m2.m21(), m1.m31() + m2.m31(),
                m1.m02() + m2.m02(), m1.m12() + m2.m12(), m1.m22() + m2.m22(), m1.m32() + m2.m32(),
                m1.m03() + m2.m03(), m1.m13() + m2.m13(), m1.m23() + m2.m23(), m1.m33() + m2.m33());
    }
}
