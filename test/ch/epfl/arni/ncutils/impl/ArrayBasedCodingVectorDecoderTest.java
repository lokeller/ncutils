/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.epfl.arni.ncutils.impl;

import ch.epfl.arni.ncutils.CodingVectorDecoder;
import ch.epfl.arni.ncutils.CodingVectorDecoder;
import ch.epfl.arni.ncutils.FiniteField;
import ch.epfl.arni.ncutils.FiniteFieldVector;
import ch.epfl.arni.ncutils.LinearDependantException;
import java.util.Map;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author lokeller
 */
public class ArrayBasedCodingVectorDecoderTest {

    public ArrayBasedCodingVectorDecoderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    private static void checkInverse(FiniteFieldVector[] vectors, FiniteFieldVector[] inverse, int size) {

       FiniteField ff = vectors[0].getFiniteField();
       for (int i= 0 ; i < size ; i++) {
            for (int j = 0 ; j < size ; j++) {

                int sum = 0;
                for (int k = 0 ; k < size ; k++) {
                    sum = ff.sum[sum][ff.mul[vectors[i].getCoefficient(k)][inverse[k].getCoefficient(j)]];
                }

                assertFalse(i == j && sum != 1);
                assertFalse(i != j && sum != 0);
            }            
        }


    }


    int size = 10;
    FiniteField ff = FiniteField.getDefaultFiniteField();

    @Test
    public void testIdentity() {

        CodingVectorDecoder d = new CodingVectorDecoder(size,ff);

        FiniteFieldVector[] vectors = new FiniteFieldVector[size];
        for (int i = 0; i < size; i++) {
            vectors[i] = new SparseFiniteFieldVector(ff);
            vectors[i].setCoefficient(i, 1);
        }

        FiniteFieldVector[] inverse = new FiniteFieldVector[size];

        for (int i = 0; i < size; i++) {
            try {

                Map<Integer, FiniteFieldVector> dd = d.decode(vectors[i]);

                assertTrue(dd.size() == 1 && dd.containsKey(i) == true);

                for ( Map.Entry<Integer, FiniteFieldVector> entry : dd.entrySet()) {
                    inverse[entry.getKey()] = entry.getValue();
                }

            } catch (LinearDependantException ex) {
                fail();
            }
        }

        checkInverse(vectors, inverse, size);
    }

    @Test
    public void testInstantlyDecodable() {
        FiniteFieldVector[] vectors = new FiniteFieldVector[size];
        for (int i = 0; i < size; i++) {
            vectors[i] = new SparseFiniteFieldVector(ff);
            for ( int j = 0 ; j <= i ; j++) {
                vectors[i].setCoefficient(j, 1);
            }
        }

        FiniteFieldVector[] inverse = new FiniteFieldVector[size];

        CodingVectorDecoder d = new CodingVectorDecoder(size,ff);

        for (int i = 0; i < size; i++) {
            try {
                Map<Integer, FiniteFieldVector> dd = d.decode(vectors[i]);
                assertTrue (dd.size() == 1 && dd.containsKey(i) == true);

                for ( Map.Entry<Integer, FiniteFieldVector> entry : dd.entrySet()) {
                    inverse[entry.getKey()] = entry.getValue();
                }
            } catch (LinearDependantException ex) {
                fail();
            }
        }

        checkInverse(vectors, inverse, size);
    }

    @Test
    public void testNonDecodable() {
        FiniteFieldVector[] vectors = new FiniteFieldVector[size];

        for (int i = 0; i < size; i++) {
            vectors[i] = new SparseFiniteFieldVector(ff);
            vectors[i].setCoefficient(i, 1);
            vectors[i].setCoefficient(size-1, 1);
        }

        FiniteFieldVector[] inverse = new FiniteFieldVector[size];
        CodingVectorDecoder d = new CodingVectorDecoder(size,ff);

        for (int i = 0; i < size; i++) {
            try {
                Map<Integer, FiniteFieldVector> dd = d.decode(vectors[i]);
                assertTrue (( i < size -1 && dd.size() == 0) || ( i == size -1 && dd.size() == size));


                for ( Map.Entry<Integer, FiniteFieldVector> entry : dd.entrySet()) {
                    inverse[entry.getKey()] = entry.getValue();
                }

            } catch (LinearDependantException ex) {
                fail();
            }
        }

        checkInverse(vectors, inverse, size);
    }

    @Test
    public void testLinearlyDependant() {
        FiniteFieldVector[] vectors = new FiniteFieldVector[size];

        Random r = new Random(2131231);

        vectors[0] = new SparseFiniteFieldVector(ff);
        for (int i = 1; i < size; i++) {
            vectors[0].setCoefficient(i, r.nextInt(ff.getCardinality()));
        }

        CodingVectorDecoder d = new CodingVectorDecoder(size,ff);

        for (int i = 1; i < size; i++) {
            vectors[i] = new SparseFiniteFieldVector(ff);

            int x = r.nextInt(ff.getCardinality());
            for (int j = 1; j < size; j++) {
                int p = vectors[0].getCoefficient(j);
                vectors[i].setCoefficient(j, ff.mul[x][p]);
            }

        }
        for (int i = 0; i < size; i++) {
            try {
                Map<Integer, FiniteFieldVector> dd = d.decode(vectors[i]);
                assertTrue( i == 0 );
            } catch (LinearDependantException ex) {
            }
        }
    }

     @Test
     public void testRandomMatrix() {
        FiniteFieldVector[] vectors = new FiniteFieldVector[size];

        Random r = new Random(2131231);

        for (int i = 0; i < size; i++) {
            vectors[i] = new SparseFiniteFieldVector(ff);

            for (int j = 0; j < size; j++) {
                int x = r.nextInt(ff.getCardinality());
                vectors[i].setCoefficient(j, x);
            }

        }

        FiniteFieldVector[] inverse = new FiniteFieldVector[size];
        CodingVectorDecoder d = new CodingVectorDecoder(size,ff);

        for (int i = 0; i < size; i++) {
            try {
                Map<Integer, FiniteFieldVector> dd = d.decode(vectors[i]);

                for ( Map.Entry<Integer, FiniteFieldVector> entry : dd.entrySet()) {
                    inverse[entry.getKey()] = entry.getValue();
                }
            } catch (LinearDependantException ex) {
                fail();
            }
        }

        checkInverse(vectors, inverse, size);
    }

}