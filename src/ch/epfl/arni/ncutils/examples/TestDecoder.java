/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.epfl.arni.ncutils.examples;

import ch.epfl.arni.ncutils.ArrayBasedCodingVectorDecoder;
import ch.epfl.arni.ncutils.CodingVectorDecoder;
import ch.epfl.arni.ncutils.FiniteField;
import ch.epfl.arni.ncutils.FiniteFieldVector;
import ch.epfl.arni.ncutils.LinearDependantException;
import ch.epfl.arni.ncutils.SparseFiniteFieldVector;
import ch.epfl.arni.ncutils.VectorDecoder;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lokeller
 */
public class TestDecoder {

    public static void check(boolean val) {
        if (val == false) throw new RuntimeException();
    }

    public static void checkInverse(FiniteFieldVector[] vectors, FiniteFieldVector[] inverse, int size) {
        
       FiniteField ff = FiniteField.getDefaultFiniteField();

       for (int i= 0 ; i < size ; i++) {
            for (int j = 0 ; j < size ; j++) {

                int sum = 0;
                for (int k = 0 ; k < size ; k++) {
                    sum = ff.sum[sum][ff.mul[vectors[i].getCoefficient(k)][inverse[k].getCoefficient(j)]];
                }

                System.out.print(" " + sum);

                if (i == j && sum != 1) throw new RuntimeException();
                if (i != j && sum != 0) throw new RuntimeException();
            }
             System.out.println();
        }


    }


    public static final void main(String [] args) {

        int size = 10;
        CodingVectorDecoder d = new ArrayBasedCodingVectorDecoder(size);
        testInstantlyDecodable(size, d);

        d = new ArrayBasedCodingVectorDecoder(size);
        testIdentity(size, d);
        
        d = new ArrayBasedCodingVectorDecoder(size);
        testLinearlyDependant(size,d);

        d = new ArrayBasedCodingVectorDecoder(size);
        testNonDecodable(size, d);

        d = new ArrayBasedCodingVectorDecoder(size);
        testRandomMatrix(size, d);

        System.out.println("All tests completed succesfully");

    }

    private static void testIdentity(int size, CodingVectorDecoder d) {
        FiniteFieldVector[] vectors = new FiniteFieldVector[size];
        for (int i = 0; i < size; i++) {
            vectors[i] = new SparseFiniteFieldVector();
            vectors[i].setCoefficient(i, 1);
        }

        FiniteFieldVector[] inverse = new FiniteFieldVector[size];

        for (int i = 0; i < size; i++) {
            try {

                Map<Integer, FiniteFieldVector> dd = d.decode(vectors[i]);
                
                check (dd.size() == 1 && dd.containsKey(i) == true);

                for ( Map.Entry<Integer, FiniteFieldVector> entry : dd.entrySet()) {
                    inverse[entry.getKey()] = entry.getValue();
                }

            } catch (LinearDependantException ex) {
                check (false);
            }
        }

        checkInverse(vectors, inverse, size);
    }

    private static void testInstantlyDecodable(int size, CodingVectorDecoder d) {
        FiniteFieldVector[] vectors = new FiniteFieldVector[size];
        for (int i = 0; i < size; i++) {
            vectors[i] = new SparseFiniteFieldVector();
            for ( int j = 0 ; j <= i ; j++) {
                vectors[i].setCoefficient(j, 1);
            }
        }

        FiniteFieldVector[] inverse = new FiniteFieldVector[size];

        for (int i = 0; i < size; i++) {
            try {
                Map<Integer, FiniteFieldVector> dd = d.decode(vectors[i]);
                check (dd.size() == 1 && dd.containsKey(i) == true);

                for ( Map.Entry<Integer, FiniteFieldVector> entry : dd.entrySet()) {
                    inverse[entry.getKey()] = entry.getValue();
                }
            } catch (LinearDependantException ex) {
                check (false);
            }
        }

        checkInverse(vectors, inverse, size);
    }

    private static void testNonDecodable(int size, CodingVectorDecoder d) {
        FiniteFieldVector[] vectors = new FiniteFieldVector[size];
        
        for (int i = 0; i < size; i++) {
            vectors[i] = new SparseFiniteFieldVector();            
            vectors[i].setCoefficient(i, 1);
            vectors[i].setCoefficient(size-1, 1);
        }

        FiniteFieldVector[] inverse = new FiniteFieldVector[size];
        
        for (int i = 0; i < size; i++) {
            try {
                Map<Integer, FiniteFieldVector> dd = d.decode(vectors[i]);
                check (( i < size -1 && dd.size() == 0) || ( i == size -1 && dd.size() == size));


                for ( Map.Entry<Integer, FiniteFieldVector> entry : dd.entrySet()) {
                    inverse[entry.getKey()] = entry.getValue();
                }

            } catch (LinearDependantException ex) {
                check (false);
            }
        }

        checkInverse(vectors, inverse, size);
    }


    private static void testLinearlyDependant(int size, CodingVectorDecoder d) {
        FiniteFieldVector[] vectors = new FiniteFieldVector[size];
        
        FiniteField f = FiniteField.getDefaultFiniteField();
                
        Random r = new Random(2131231);
        
        vectors[0] = new SparseFiniteFieldVector();            
        for (int i = 1; i < size; i++) {
            vectors[0].setCoefficient(i, r.nextInt(f.getCardinality()));            
        }
        
        for (int i = 1; i < size; i++) {
            vectors[i] = new SparseFiniteFieldVector();            

            int x = r.nextInt(f.getCardinality());
            for (int j = 1; j < size; j++) {                
                int p = vectors[0].getCoefficient(j);
                vectors[i].setCoefficient(j, f.mul[x][p]);
            }
            
        }
        for (int i = 0; i < size; i++) {
            try {
                Map<Integer, FiniteFieldVector> dd = d.decode(vectors[i]);
                check ( i == 0 );
            } catch (LinearDependantException ex) {                
            }
        }
    }

     private static void testRandomMatrix(int size, CodingVectorDecoder d) {
        FiniteFieldVector[] vectors = new FiniteFieldVector[size];

        FiniteField f = FiniteField.getDefaultFiniteField();

        Random r = new Random(2131231);

        for (int i = 0; i < size; i++) {
            vectors[i] = new SparseFiniteFieldVector();

            for (int j = 0; j < size; j++) {
                int x = r.nextInt(f.getCardinality());
                vectors[i].setCoefficient(j, x);
            }

        }

        FiniteFieldVector[] inverse = new FiniteFieldVector[size];

        for (int i = 0; i < size; i++) {
            try {
                Map<Integer, FiniteFieldVector> dd = d.decode(vectors[i]);

                for ( Map.Entry<Integer, FiniteFieldVector> entry : dd.entrySet()) {
                    inverse[entry.getKey()] = entry.getValue();
                }
            } catch (LinearDependantException ex) {
                check ( false );
            }
        }

        checkInverse(vectors, inverse, size);
    }


}
