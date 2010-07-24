/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.epfl.arni.ncutils.examples;

import ch.epfl.arni.ncutils.FiniteField;

/**
 *
 * @author lokeller
 */
public class TestFiniteField {

    public static void check(boolean val) {
        if (val == false) throw new RuntimeException();
    }

    public static void main(String [] args) {

        for (int i = 1 ; i < 9 ; i++) {
            System.out.println("Testing 2^" + i);
            testField(new FiniteField(2,i));
        }

        testField(new FiniteField(5));
        testField(new FiniteField(17));

        System.out.println("All tests passed for q=2 and m < 9, and for q=5 and q=17");

    }

    private static void testField(FiniteField f) {
        for (int i = 0; i < f.getCardinality(); i++) {
            /* identity */
            check (f.sum[i][0] == i);
            check (f.mul[i][1] == i);
            /* inverse */
            check (i == 0 || f.mul[i][f.inverse[i]] == 1);
            for (int j = 0; j < f.getCardinality(); j++) {
                /* commutativity */
                check (f.sum[i][j] == f.sum[j][i]);
                check (f.mul[i][j] == f.mul[j][i]);
                /* opposite operations */
                check (j == 0 || f.div[f.mul[i][j]][j] == i);
                check (f.sub[f.sum[i][j]][j] == i);
                for (int k = 0; k < f.getCardinality(); k++) {
                    /* associativity */
                    check (f.sum[f.sum[i][j]][k] == f.sum[i][f.sum[j][k]]);
                    check (f.mul[f.mul[i][j]][k] == f.mul[i][f.mul[j][k]]);
                    /* distributivity */
                    check (f.mul[f.sum[i][j]][k] == f.sum[f.mul[i][k]][f.mul[j][k]]);
                }
            }
        }
    }

}
