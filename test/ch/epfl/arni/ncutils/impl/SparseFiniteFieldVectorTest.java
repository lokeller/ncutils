/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.epfl.arni.ncutils.impl;

import ch.epfl.arni.ncutils.FiniteField;
import ch.epfl.arni.ncutils.FiniteFieldVector;
import java.util.HashSet;
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
public class SparseFiniteFieldVectorTest {

    public SparseFiniteFieldVectorTest() {
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


    @Test
    public void testSetToZero() {        
        SparseFiniteFieldVector instance = new SparseFiniteFieldVector(FiniteField.getDefaultFiniteField());

        for (int i = 0 ; i < 10; i++) {
            instance.setCoefficient(i, 1);
        }
        instance.setToZero();

        for (int i = 0 ; i < 10; i++) {
            assertTrue(instance.getCoefficient(i) == 0);
        }
        
    }

    @Test
    public void testCopyTo() {
        SparseFiniteFieldVector instance = new SparseFiniteFieldVector(FiniteField.getDefaultFiniteField());
        SparseFiniteFieldVector instance2 = new SparseFiniteFieldVector(FiniteField.getDefaultFiniteField());

        for (int i = 0 ; i < 10; i++) {
            instance.setCoefficient(i, 1);
        }

        instance.copyTo(instance2);

        for (int i = 0 ; i < 10; i++) {
            assertTrue(instance.getCoefficient(i) == 1);
        }
       
    }

    @Test
    public void testSetGetCoefficient() {
        SparseFiniteFieldVector instance = new SparseFiniteFieldVector(FiniteField.getDefaultFiniteField());

        for (int j = 0 ; j < 10; j++) {
            for (int i = 0 ; i < instance.getFiniteField().getCardinality(); i++) {
                instance.setCoefficient(j, i);
                assertTrue(instance.getCoefficient(j) == i);
            }
        }

    }

    @Test
    public void testGetHammingWeight() {
        SparseFiniteFieldVector instance = new SparseFiniteFieldVector(FiniteField.getDefaultFiniteField());

        for (int i = 0 ; i < 10; i++) {
            instance.setCoefficient(i, 1);
        }

        assertTrue(instance.getHammingWeight() == 10);
    }

    @Test
    public void testGetNonZeroCoefficients() {

        HashSet<Integer> ids = new HashSet<Integer>();
        SparseFiniteFieldVector instance = new SparseFiniteFieldVector(FiniteField.getDefaultFiniteField());

        for (int i = 0 ; i < 10; i++) {
            instance.setCoefficient(i, 1);
            ids.add(i);
        }

        HashSet<Integer> others = new HashSet<Integer>();

        for (Integer i : instance.getNonZeroCoefficients()) {
            others.add(i);
        }

        assertTrue(others.equals(ids));

    }

    @Test
    public void testGetFiniteField() {

        FiniteField ff = new FiniteField(17);
        SparseFiniteFieldVector instance = new SparseFiniteFieldVector(ff);

        assertTrue(ff == instance.getFiniteField());
        
    }

    @Test
    public void testAdd() {

        SparseFiniteFieldVector instance = new SparseFiniteFieldVector(FiniteField.getDefaultFiniteField());
        SparseFiniteFieldVector instance2 = new SparseFiniteFieldVector(FiniteField.getDefaultFiniteField());

        for (int i = 0 ; i < 10; i++) {
            instance.setCoefficient(i, 1);
            instance2.setCoefficient(i, 7);
        }

        instance.add(instance2);

        for (int i = 0 ; i < 10; i++) {
            assertTrue(instance.getCoefficient(i) == instance.getFiniteField().sum[1][7]);
        }

    }

    @Test
    public void testScalarMultiply() {

        SparseFiniteFieldVector instance = new SparseFiniteFieldVector(FiniteField.getDefaultFiniteField());

        for (int i = 0 ; i < 10; i++) {
            instance.setCoefficient(i, 3);
        }

        instance.scalarMultiply(3);

        for (int i = 0 ; i < 10; i++) {
            assertTrue(instance.getCoefficient(i) == instance.getFiniteField().mul[3][3]);
        }

    }

}