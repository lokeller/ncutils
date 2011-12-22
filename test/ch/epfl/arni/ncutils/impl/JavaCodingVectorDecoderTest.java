/*
 * Copyright (c) 2011, EPFL - ARNI
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

import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


import ch.epfl.arni.ncutils.impl.CodingVectorDecoder;
import ch.epfl.arni.ncutils.impl.FiniteField;
import static org.junit.Assert.*;

/**
 *
 * @author lokeller
 */
public class JavaCodingVectorDecoderTest {

    public JavaCodingVectorDecoderTest() {
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

    private static void checkInverse(byte[][] vectors, byte[][] inverse, int size, int offset) {

       FiniteField ff = FiniteField.getF256();
       for (int i= 0 ; i < size ; i++) {
            for (int j = 0 ; j < size ; j++) {

                int sum = 0;
                for (int k = 0 ; k < size ; k++) {
                    sum = ff.sum[sum][ff.mul[vectors[i][k] & 0xFF][inverse[k][j+offset] & 0xFF]];
                }

                assertFalse(i == j && sum != 1);
                assertFalse(i != j && sum != 0);
            }            
        }


    }


    int size = 10;
    FiniteField ff = FiniteField.getF256();

    @Test
    public void testIdentity() {

    	CodingVectorDecoder d = new CodingVectorDecoder(size);

    	byte[][] vectors = new byte[size][size];
        for (int i = 0; i < size; i++) {            
            vectors[i][i]= (byte) 1;
        }
        
        for (int i = 0; i < size; i++) {
            	
                d.addCodingVector(vectors[i], 0);

                assertEquals(i+1, d.getSubspaceSize());
                assertEquals(i+1, d.getDecodingVectorsCount());               
                assertEquals(i, d.getDecodingVectoPacketId(i));

        }

        byte[][] inverse = new byte[size][];
        int inverseOffset = d.getDecodingVectorOffset();
        for ( int i = 0 ; i < size; i++) {
        	inverse[i] = d.getDecodingVectorBuffer(i);        	
        }
        
        checkInverse(vectors, inverse, size, inverseOffset);
    }

    @Test
    public void testInstantlyDecodable() {
    	byte[][] vectors = new byte[size][size];
        for (int i = 0; i < size; i++) {
            for ( int j = 0 ; j <= i ; j++) {
                vectors[i][j] =  (byte) 1;
            }
        }


        CodingVectorDecoder d = new CodingVectorDecoder(size);

        for (int i = 0; i < size; i++) {
            
            d.addCodingVector(vectors[i], 0);
            
            assertEquals(i+1, d.getSubspaceSize());
            assertEquals(i+1, d.getDecodingVectorsCount());               
            assertEquals(i, d.getDecodingVectoPacketId(i));
                
        }

        byte[][] inverse = new byte[size][];
        int inverseOffset = d.getDecodingVectorOffset();
        for ( int i = 0 ; i < size; i++) {
        	inverse[i] = d.getDecodingVectorBuffer(i);        	
        }
        
        checkInverse(vectors, inverse, size, inverseOffset);
    }

    @Test
    public void testNonDecodable() {
    	byte[][] vectors = new byte[size][size];

        for (int i = 0; i < size; i++) {
            
            vectors[i][i] = (byte) 1;
            vectors[i][size-1] = (byte) 1;
        }

        CodingVectorDecoder d = new CodingVectorDecoder(size);

        for (int i = 0; i < size; i++) {

            d.addCodingVector(vectors[i], 0);
            
            if ( i < size - 1) {
            	assertEquals(0, d.getDecodingVectorsCount());
            } else {
            	assertEquals(size, d.getDecodingVectorsCount());
            }
             
            assertEquals(Math.min(i+1, size), d.getSubspaceSize());

        }

        byte[][] inverse = new byte[size][];
        int inverseOffset = d.getDecodingVectorOffset();
        for ( int i = 0 ; i < size; i++) {
        	inverse[i] = d.getDecodingVectorBuffer(i);        	
        }
        
        checkInverse(vectors, inverse, size, inverseOffset);
    }

    @Test
    public void testLinearlyDependant() {
    	byte[][] vectors = new byte[size][size];

        Random r = new Random(2131231);

        for (int i = 1; i < size - 1; i++) {
            vectors[0][i] = (byte) r.nextInt(ff.getCardinality());
        }

        CodingVectorDecoder d = new CodingVectorDecoder(size);

        for (int i = 1; i < size; i++) {            
            
            int x = r.nextInt(ff.getCardinality());
            
            for (int j = 0; j < size; j++) {
                int p = vectors[0][j] & 0xFF;
                vectors[i][j]= (byte) ff.mul[x][p];
            }

        }
        for (int i = 0; i < size; i++) {            
            d.addCodingVector(vectors[i], 0);        
            assertEquals(1, d.getSubspaceSize());            
            assertEquals(0, d.getDecodingVectorsCount());
        }
    }

     @Test
     public void testRandomMatrix() {
    	 byte[][] vectors = new byte[size][size];

        Random r = new Random(2131231);

        for (int i = 0; i < size; i++) {

            for (int j = 0; j < size; j++) {
                int x = r.nextInt(ff.getCardinality());
                vectors[i][j] =  (byte) x;
            }

        }
        
        CodingVectorDecoder d = new CodingVectorDecoder(size);

        for (int i = 0; i < size; i++) {           
             d.addCodingVector(vectors[i], 0);
        }
        
        byte[][] inverse = new byte[size][];
        int inverseOffset = d.getDecodingVectorOffset();
        for ( int i = 0 ; i < size; i++) {
        	inverse[i] = d.getDecodingVectorBuffer(i);        	
        }
        
        checkInverse(vectors, inverse, size, inverseOffset);
    }

}