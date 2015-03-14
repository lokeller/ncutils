/*
 * Copyright (c) 2010, EPFL - ARNI
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the EPFL nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package ch.epfl.arni.ncutils.impl;


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