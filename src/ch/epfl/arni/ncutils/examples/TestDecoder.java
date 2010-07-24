/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.epfl.arni.ncutils.examples;

import ch.epfl.arni.ncutils.ArrayDecoder;
import ch.epfl.arni.ncutils.Decoder;
import ch.epfl.arni.ncutils.FiniteField;
import ch.epfl.arni.ncutils.FiniteFieldVector;
import ch.epfl.arni.ncutils.LinearDependantException;
import ch.epfl.arni.ncutils.SparseFiniteFieldVector;
import ch.epfl.arni.ncutils.VectorDecoder;
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

    public static final void main(String [] args) {

        int size = 100;
        Decoder d = new ArrayDecoder(size);     
        testInstantlyDecodable(size, d);

        d = new ArrayDecoder(size);
        testIdentity(size, d);

        d = new ArrayDecoder(size);
        testLinearlyDependant(size,d);

        d = new ArrayDecoder(size);
        testNonDecodable(size, d);

        d = new VectorDecoder();
        testInstantlyDecodable(size, d);

        d = new VectorDecoder();
        testIdentity(size, d);

        d = new VectorDecoder();
        testLinearlyDependant(size,d);

        d = new VectorDecoder();     
        testNonDecodable(size, d);


        System.out.println("All tests completed succesfully");

    }

    private static void testIdentity(int size, Decoder d) {
        FiniteFieldVector[] vectors = new FiniteFieldVector[size];
        for (int i = 0; i < size; i++) {
            vectors[i] = new SparseFiniteFieldVector();
            vectors[i].setCoefficient(i, 1);
        }
        for (int i = 0; i < size; i++) {
            try {

                Set<Integer> dd = d.decode(vectors[i]);
                
                check (dd.size() == 1 && dd.contains(i) == true);
            } catch (LinearDependantException ex) {
                check (false);
            }
        }
    }

    private static void testInstantlyDecodable(int size, Decoder d) {
        FiniteFieldVector[] vectors = new FiniteFieldVector[size];
        for (int i = 0; i < size; i++) {
            vectors[i] = new SparseFiniteFieldVector();
            for ( int j = 0 ; j <= i ; j++) {
                vectors[i].setCoefficient(j, 1);
            }
        }
        for (int i = 0; i < size; i++) {
            try {
                Set<Integer> dd = d.decode(vectors[i]);
                check (dd.size() == 1 && dd.contains(i) == true);
            } catch (LinearDependantException ex) {
                check (false);
            }
        }
    }

    private static void testNonDecodable(int size, Decoder d) {
        FiniteFieldVector[] vectors = new FiniteFieldVector[size];
        
        for (int i = 0; i < size; i++) {
            vectors[i] = new SparseFiniteFieldVector();            
            vectors[i].setCoefficient(i, 1);
            vectors[i].setCoefficient(size-1, 1);
        }
        for (int i = 0; i < size; i++) {
            try {
                Set<Integer> dd = d.decode(vectors[i]);
                check (( i < size -1 && dd.size() == 0) || ( i == size -1 && dd.size() == size));
            } catch (LinearDependantException ex) {
                check (false);
            }
        }
    }


    private static void testLinearlyDependant(int size, Decoder d) {
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
                Set<Integer> dd = d.decode(vectors[i]);                
                check ( i == 0 );
            } catch (LinearDependantException ex) {                
            }
        }
    }


}
