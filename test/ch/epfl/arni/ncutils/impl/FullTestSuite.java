/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.epfl.arni.ncutils.impl;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author lokeller
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ch.epfl.arni.ncutils.impl.SparseFiniteFieldVectorTest.class,
                        ch.epfl.arni.ncutils.impl.DenseFiniteFieldVectorTest.class,
                        ch.epfl.arni.ncutils.impl.FiniteFieldTest.class,
                        ch.epfl.arni.ncutils.impl.ArrayBasedCodingVectorDecoderTest.class})
public class FullTestSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

}