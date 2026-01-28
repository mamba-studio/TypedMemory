/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.nir;

import static java.lang.constant.ConstantDescs.CD_Integer;

/**
 *
 * @author jmburu
 */
public class Test {
    void main(){
        //return a = new int[3];
        var expr = new AReturn(
                        new StoreLocalExpr(
                            1,
                            new ANewArray(CD_Integer, new IConst(3))
                        )
                    );
        
        var out = new DebugEmitter();
        var lowering = new Lowering(out);
        lowering.lower(expr);
    }
}
