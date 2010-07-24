/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.epfl.arni.ncutils;

/**
 *
 * @author lokeller
 */
public class FiniteField {

    private static FiniteField finiteField = new FiniteField(2,4);

    public static FiniteField getDefaultFiniteField() {
        return finiteField;
    }

    public int[] inverse;
    public int[][] sum;
    public int[][] div;
    public int[][] sub;
    public int[][] mul;

    private int Q;

    public FiniteField(int q, int m) {

        if (q != 2 || m > 16) throw new UnsupportedOperationException("Finite field not supported");

        this.Q = (int) Math.pow(q,m);

        inverse = new int[Q];
        sum = new int[Q][Q];
        mul = new int[Q][Q];
        div = new int[Q][Q];
        sub = new int[Q][Q];

        int [] primitive_polynomial = { 3, 7, 11, 19, 37, 67, 137,
                                        285, 529,1033,2053,4179,
                                        8219,17475, 32771, 69643 };      

        int c = primitive_polynomial[m - 1] - ( 1 << m );

        for (int i = 0 ; i < Q ; i++) {
            for (int j = 0 ; j < Q ; j++) {

                sum[i][j] = i ^ j;
                sub[i][j] = i ^ j;

                int a = i;
                int b = j;
                int p = 0;
                
                /* paesant's algorithm*/
                for ( int k = 0 ; k < m ; k++) {
                    if ( (b & 0x1) == 1) {
                        p = p ^ a;
                    }

                    boolean r = (a & (0x1 << (m-1))) > 0;
                    a = (a << 1) % Q;
                    if (r) {
                        a = a ^ c;
                    }
                    b = b >> 1;
                }

                mul[i][j] = p;

            }
        }

        for (int i = 0 ; i < Q ; i++) {
            for (int j = 0 ; j < Q ; j++) {

                div[mul[i][j]][i] = j;
                div[mul[i][j]][j] = i;

            }
        }

        for (int i = 1 ; i < Q ; i++) {
            inverse[i] = div[1][i];
        }

    }

    public FiniteField(int q) {
        this.Q = q;

        inverse = new int[Q];
        sum = new int[Q][Q];
        mul = new int[Q][Q];
        div = new int[Q][Q];
        sub = new int[Q][Q];
        
        /* build inverse table */
        for (int b = 1 ; b < Q ; b++) {
                for (int i = 1 ; i < Q ; i++) {
                        if ((i *  b) % Q == 1) {
                                inverse[b] = i;
                                break;
                        }
                }
        }

        /* build tables */
        for (int b = 0 ; b < Q ; b++) {
                for (int i = 0 ; i < Q ; i++) {
                    sum[b][i] = (b+i) % Q;
                    sub[b][i] = (b-i+Q) % Q;
                    assert(sum[i][sub[b][i]] == b);
                    mul[b][i] = (b * i) % Q;
                    div[b][i] = (b * inverse[i]) % Q;
                }
        }

    }


    public int getCardinality() {
        return Q;
    }

}
