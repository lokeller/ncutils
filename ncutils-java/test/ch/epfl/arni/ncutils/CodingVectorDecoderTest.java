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
import ch.epfl.arni.ncutils.Vector;
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

    private static void checkInverse(Vector[] vectors, Vector[] inverse, int size) {

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

        Vector[] vectors = new Vector[size];
        for (int i = 0; i < size; i++) {
            vectors[i] = new Vector(size, ff);
            vectors[i].setCoordinate(i, 1);
        }

        Vector[] inverse = new Vector[size];

        for (int i = 0; i < size; i++) {

            Map<Integer, Vector> dd = d.addVector(vectors[i]);

            if ( dd == null) {
            	fail();
            }
            
            assertTrue(dd.size() == 1 && dd.containsKey(i) == true);

            for ( Map.Entry<Integer, Vector> entry : dd.entrySet()) {
                inverse[entry.getKey()] = entry.getValue();
            }

        }

        checkInverse(vectors, inverse, size);
    }

    @Test
    public void testInstantlyDecodable() {
        Vector[] vectors = new Vector[size];
        for (int i = 0; i < size; i++) {
            vectors[i] = new Vector(size, ff);
            for ( int j = 0 ; j <= i ; j++) {
                vectors[i].setCoordinate(j, 1);
            }
        }

        Vector[] inverse = new Vector[size];

        CodingVectorDecoder d = new CodingVectorDecoder(size,ff);

        for (int i = 0; i < size; i++) {
        
            Map<Integer, Vector> dd = d.addVector(vectors[i]);
            
            if (dd == null) {
            	fail();
            }
            
            assertTrue (dd.size() == 1 && dd.containsKey(i) == true);

            for ( Map.Entry<Integer, Vector> entry : dd.entrySet()) {
                inverse[entry.getKey()] = entry.getValue();
            }

        }

        checkInverse(vectors, inverse, size);
    }

    @Test
    public void testNonDecodable() {
        Vector[] vectors = new Vector[size];

        for (int i = 0; i < size; i++) {
            vectors[i] = new Vector(size, ff);
            vectors[i].setCoordinate(i, 1);
            vectors[i].setCoordinate(size-1, 1);
        }

        Vector[] inverse = new Vector[size];
        CodingVectorDecoder d = new CodingVectorDecoder(size,ff);

        for (int i = 0; i < size; i++) {
            
            Map<Integer, Vector> dd = d.addVector(vectors[i]);
            
            if (dd == null) {
            	fail();
            }
            
            assertTrue (( i < size -1 && dd.size() == 0) || ( i == size -1 && dd.size() == size));


            for ( Map.Entry<Integer, Vector> entry : dd.entrySet()) {
                inverse[entry.getKey()] = entry.getValue();
            }

        }

        checkInverse(vectors, inverse, size);
    }

    @Test
    public void testLinearlyDependant() {
        Vector[] vectors = new Vector[size];

        Random r = new Random(2131231);

        vectors[0] = new Vector(size, ff);
        for (int i = 1; i < size; i++) {
            vectors[0].setCoordinate(i, r.nextInt(ff.getCardinality()));
        }

        CodingVectorDecoder d = new CodingVectorDecoder(size,ff);

        for (int i = 1; i < size; i++) {
            vectors[i] = new Vector(size, ff);

            int x = r.nextInt(ff.getCardinality());
            for (int j = 1; j < size; j++) {
                int p = vectors[0].getCoordinate(j);
                vectors[i].setCoordinate(j, ff.mul[x][p]);
            }

        }
        for (int i = 0; i < size; i++) {
        
            if ( d.addVector(vectors[i]) != null) {
            	assertTrue( i == 0 );
            }
        
        }
    }

     @Test
     public void testRandomMatrix() {
        Vector[] vectors = new Vector[size];

        Random r = new Random(2131231);

        for (int i = 0; i < size; i++) {
            vectors[i] = new Vector(size, ff);

            for (int j = 0; j < size; j++) {
                int x = r.nextInt(ff.getCardinality());
                vectors[i].setCoordinate(j, x);
            }

        }

        Vector[] inverse = new Vector[size];
        CodingVectorDecoder d = new CodingVectorDecoder(size,ff);

        for (int i = 0; i < size; i++) {
            
            Map<Integer, Vector> dd = d.addVector(vectors[i]);

            if ( dd == null ) {
            	fail();
            }
            
            for ( Map.Entry<Integer, Vector> entry : dd.entrySet()) {
                inverse[entry.getKey()] = entry.getValue();
            }
        }

        checkInverse(vectors, inverse, size);
    }
     
     @Test
     public void testSubspaceSize() {
      
      	CodingVectorDecoder d = new CodingVectorDecoder(size,ff);
      	       
         for (int i = 0; i < size; i++) {
             Vector v = new Vector(size, ff);
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
            Vector v = new Vector(size, ff);
            v.setCoordinate(i, 1);
            
            try {
            	d.addVector(v);
            } catch (Exception e) {
				fail();
			}
        }
     	
        Vector v = new Vector(size, ff);
        
    	if ( d.addVector(v) != null) {
    		fail();
    	}
             	
    }
     
    @Test
    public void testMaxPackets() {
    
    	CodingVectorDecoder d = new CodingVectorDecoder(size,ff);
    	
    	assertEquals(size, d.getMaxPackets());
    	
    }
     

}