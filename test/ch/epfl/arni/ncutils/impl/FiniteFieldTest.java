/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.epfl.arni.ncutils.impl;

import ch.epfl.arni.ncutils.FiniteField;
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
public class FiniteFieldTest {

    public FiniteFieldTest() {
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
    public void testExtendedField() {

        for (int i = 1 ; i < 9 ; i++) {            
            testField(new FiniteField(2,i));
        }

    }

    @Test
    public void testPrimeField() {

        testField(new FiniteField(5));
        testField(new FiniteField(17));

    }

    private static void testField(FiniteField f) {
        for (int i = 0; i < f.getCardinality(); i++) {
            /* identity */
            assertTrue (f.sum[i][0] == i);
            assertTrue (f.mul[i][1] == i);
            /* inverse */
            assertTrue (i == 0 || f.mul[i][f.inverse[i]] == 1);
            for (int j = 0; j < f.getCardinality(); j++) {
                /* commutativity */
                assertTrue (f.sum[i][j] == f.sum[j][i]);
                assertTrue (f.mul[i][j] == f.mul[j][i]);
                /* opposite operations */
                assertTrue (j == 0 || f.div[f.mul[i][j]][j] == i);
                assertTrue (f.sub[f.sum[i][j]][j] == i);
                for (int k = 0; k < f.getCardinality(); k++) {
                    /* associativity */
                    assertTrue (f.sum[f.sum[i][j]][k] == f.sum[i][f.sum[j][k]]);
                    assertTrue (f.mul[f.mul[i][j]][k] == f.mul[i][f.mul[j][k]]);
                    /* distributivity */
                    assertTrue (f.mul[f.sum[i][j]][k] == f.sum[f.mul[i][k]][f.mul[j][k]]);
                }
            }
        }
    }

}