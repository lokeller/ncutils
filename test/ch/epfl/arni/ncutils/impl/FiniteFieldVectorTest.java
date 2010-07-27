/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.epfl.arni.ncutils.impl;

import ch.epfl.arni.ncutils.FiniteFieldVector;
import ch.epfl.arni.ncutils.FiniteField;
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
public class FiniteFieldVectorTest {

    public FiniteFieldVectorTest() {
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

    int size = 10;

    @Test
    public void testSetToZero() {        
        FiniteFieldVector instance = new FiniteFieldVector(size, FiniteField.getDefaultFiniteField());

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
        FiniteFieldVector instance = new FiniteFieldVector(size, FiniteField.getDefaultFiniteField());
        

        for (int i = 0 ; i < 10; i++) {
            instance.setCoefficient(i, 1);
        }

        FiniteFieldVector instance2 = instance.copy();

        for (int i = 0 ; i < 10; i++) {
            assertTrue(instance.getCoefficient(i) == 1);
        }
       
    }

    @Test
    public void testSetGetCoefficient() {
        FiniteFieldVector instance = new FiniteFieldVector(size, FiniteField.getDefaultFiniteField());

        for (int j = 0 ; j < 10; j++) {
            for (int i = 0 ; i < instance.getFiniteField().getCardinality(); i++) {
                instance.setCoefficient(j, i);
                assertTrue(instance.getCoefficient(j) == i);
            }
        }

    }

    @Test
    public void testGetFiniteField() {

        FiniteField ff = new FiniteField(17);
        FiniteFieldVector instance = new FiniteFieldVector(size, ff);

        assertTrue(ff == instance.getFiniteField());
        
    }

    @Test
    public void testAdd() {

        FiniteFieldVector instance = new FiniteFieldVector(size, FiniteField.getDefaultFiniteField());
        FiniteFieldVector instance2 = new FiniteFieldVector(size, FiniteField.getDefaultFiniteField());

        for (int i = 0 ; i < 10; i++) {
            instance.setCoefficient(i, 1);
            instance2.setCoefficient(i, 7);
        }

        FiniteFieldVector instance3 = instance.add(instance2);

        for (int i = 0 ; i < 10; i++) {
            assertTrue(instance3.getCoefficient(i) == instance.getFiniteField().sum[1][7]);
        }

    }

    @Test
    public void testScalarMultiply() {

        FiniteFieldVector instance = new FiniteFieldVector(size, FiniteField.getDefaultFiniteField());

        for (int i = 0 ; i < 10; i++) {
            instance.setCoefficient(i, 3);
        }

        FiniteFieldVector instance2 = instance.scalarMultiply(3);

        for (int i = 0 ; i < 10; i++) {
            assertTrue(instance2.getCoefficient(i) == instance.getFiniteField().mul[3][3]);
        }

    }

}