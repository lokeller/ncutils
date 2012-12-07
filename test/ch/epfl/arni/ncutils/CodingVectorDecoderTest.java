/*******************************************************************************
 * Copyright (c) 2012, EPFL - ARNI
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the EPFL nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *  
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package ch.epfl.arni.ncutils;

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
public class CodingVectorDecoderTest {

    public CodingVectorDecoderTest() {
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
                    sum = ff.sum[sum][ff.mul[vectors[i].getCoordinate(k)][inverse[k].getCoordinate(j)]];
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
            vectors[i] = new FiniteFieldVector(size, ff);
            vectors[i].setCoordinate(i, 1);
        }

        FiniteFieldVector[] inverse = new FiniteFieldVector[size];

        for (int i = 0; i < size; i++) {
            try {

                Map<Integer, FiniteFieldVector> dd = d.addVector(vectors[i]);

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
            vectors[i] = new FiniteFieldVector(size, ff);
            for ( int j = 0 ; j <= i ; j++) {
                vectors[i].setCoordinate(j, 1);
            }
        }

        FiniteFieldVector[] inverse = new FiniteFieldVector[size];

        CodingVectorDecoder d = new CodingVectorDecoder(size,ff);

        for (int i = 0; i < size; i++) {
            try {
                Map<Integer, FiniteFieldVector> dd = d.addVector(vectors[i]);
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
            vectors[i] = new FiniteFieldVector(size, ff);
            vectors[i].setCoordinate(i, 1);
            vectors[i].setCoordinate(size-1, 1);
        }

        FiniteFieldVector[] inverse = new FiniteFieldVector[size];
        CodingVectorDecoder d = new CodingVectorDecoder(size,ff);

        for (int i = 0; i < size; i++) {
            try {
                Map<Integer, FiniteFieldVector> dd = d.addVector(vectors[i]);
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

        vectors[0] = new FiniteFieldVector(size, ff);
        for (int i = 1; i < size; i++) {
            vectors[0].setCoordinate(i, r.nextInt(ff.getCardinality()));
        }

        CodingVectorDecoder d = new CodingVectorDecoder(size,ff);

        for (int i = 1; i < size; i++) {
            vectors[i] = new FiniteFieldVector(size, ff);

            int x = r.nextInt(ff.getCardinality());
            for (int j = 1; j < size; j++) {
                int p = vectors[0].getCoordinate(j);
                vectors[i].setCoordinate(j, ff.mul[x][p]);
            }

        }
        for (int i = 0; i < size; i++) {
            try {
                d.addVector(vectors[i]);
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
            vectors[i] = new FiniteFieldVector(size, ff);

            for (int j = 0; j < size; j++) {
                int x = r.nextInt(ff.getCardinality());
                vectors[i].setCoordinate(j, x);
            }

        }

        FiniteFieldVector[] inverse = new FiniteFieldVector[size];
        CodingVectorDecoder d = new CodingVectorDecoder(size,ff);

        for (int i = 0; i < size; i++) {
            try {
                Map<Integer, FiniteFieldVector> dd = d.addVector(vectors[i]);

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
     public void testSubspaceSize() {
      
      	CodingVectorDecoder d = new CodingVectorDecoder(size,ff);
      	       
         for (int i = 0; i < size; i++) {
             FiniteFieldVector v = new FiniteFieldVector(size, ff);
             v.setCoordinate(i, 1);
             
             try {
             	d.addVector(v);
             } catch (Exception e) {
 				fail();
 			}
             
             assertEquals(i + 1, d.getSubspaceSize());
         }
      	        	
     }
    
    @Test
    public void testTooManyPackets() {
     
     	CodingVectorDecoder d = new CodingVectorDecoder(size,ff);
     	       
        for (int i = 0; i < size; i++) {
            FiniteFieldVector v = new FiniteFieldVector(size, ff);
            v.setCoordinate(i, 1);
            
            try {
            	d.addVector(v);
            } catch (Exception e) {
				fail();
			}
        }
     	
        FiniteFieldVector v = new FiniteFieldVector(size, ff);
        
        try {
        	d.addVector(v);
        	fail();
        } catch (LinearDependantException e) {}
             	
    }
     
    @Test
    public void testMaxPackets() {
    
    	CodingVectorDecoder d = new CodingVectorDecoder(size,ff);
    	
    	assertEquals(size, d.getMaxPackets());
    	
    }
     

}