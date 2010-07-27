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
        
        if (q < 1 || m < 0) throw new RuntimeException("Invalid field size");

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

        if (q < 1) throw new RuntimeException("Invalid field size");

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
                    mul[b][i] = (b * i) % Q;
                    div[b][i] = (b * inverse[i]) % Q;
                }
        }

    }

    public FiniteFieldVector byteToVector(byte [] bytes) {

       FiniteFieldVector output = new FiniteFieldVector(coordinatesCount(bytes.length), this);

       switch (Q) {
            case 256:

                for (int i = 0 ; i < bytes.length; i++) {
                    output.setCoordinate(i, 0xFF & ((int) bytes[i]));
                }

                return output ;
            case 16:

                for (int i = 0 ; i < bytes.length; i++) {
                    output.setCoordinate(2*i, 0x0F & ((int) bytes[i]));
                    output.setCoordinate(2*i+1, (0xF0 & ((int) bytes[i])) >> 4);
                }
                
                return output ;

            default:
                throw new RuntimeException("The only field size supported is 2^8 and 2^4 ( Q was " +Q + ")" );
        }


    }

    public byte[] vectorToBytes(FiniteFieldVector vector) {

        byte[] output = new byte[bytesLength(vector.getLength())];

        switch (Q) {
            case 256:

                for (int i = 0 ; i < output.length; i++) {
                    output[i] = (byte) vector.getCoordinate(i);
                }

                return output;
                
            case 16:

                for (int i = 0 ; i < output.length; i++) {
                    output[i] = (byte) ( (vector.getCoordinate(2*i+1) << 4) + vector.getCoordinate(2*i )) ;
                }
                
                return output;

            default:
                throw new RuntimeException("The only field size supported is 2^8 and 2^4 ( Q was " + Q + ")" );
        }
    }

    public int bytesLength(int coordinatesCount) {

         switch (Q) {
            case 256:

                return coordinatesCount;

            case 16:

                return (coordinatesCount + 1) / 2;

            default:
                throw new RuntimeException("The only field size supported is 2^8 and 2^4 ( Q was " + Q + ")" );
        }
    }

    public int coordinatesCount(int bytesLength) {
         switch (Q) {
            case 256:

                return bytesLength;

            case 16:

                return bytesLength * 2;

            default:
                throw new RuntimeException("The only field size supported is 2^8 and 2^4 ( Q was " + Q + ")" );
        }

    }

    public int getCardinality() {
        return Q;
    }

    @Override
    public boolean equals(Object obj) {
        if ( obj instanceof FiniteField) {
            return ((FiniteField) obj).getCardinality() == Q;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + this.Q;
        return hash;
    }



}
