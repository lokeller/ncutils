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

import ch.epfl.arni.ncutils.Vector;
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
public class VectorTest {

    public VectorTest() {
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
        Vector instance = new Vector(size, FiniteField.getDefaultFiniteField());

        for (int i = 0 ; i < 10; i++) {
            instance.setCoordinate(i, 1);
        }
        instance.setToZero();

        for (int i = 0 ; i < 10; i++) {
            assertTrue(instance.getCoordinate(i) == 0);
        }
        
    }

    @Test
    public void testCopyTo() {
        Vector instance = new Vector(size, FiniteField.getDefaultFiniteField());
        

        for (int i = 0 ; i < 10; i++) {
            instance.setCoordinate(i, 1);
        }

        Vector instance2 = instance.copy();

        for (int i = 0 ; i < 10; i++) {
            assertTrue(instance2.getCoordinate(i) == 1);
        }
       
    }

    @Test
    public void testSetGetCoordinates() {
        Vector instance = new Vector(size, FiniteField.getDefaultFiniteField());

        for (int j = 0 ; j < 10; j++) {
            for (int i = 0 ; i < instance.getFiniteField().getCardinality(); i++) {
                instance.setCoordinate(j, i);
                assertTrue(instance.getCoordinate(j) == i);
            }
        }

    }

    @Test
    public void testGetFiniteField() {

        FiniteField ff = new FiniteField(17);
        Vector instance = new Vector(size, ff);

        assertTrue(ff == instance.getFiniteField());
        
    }

    @Test
    public void testAdd() {

        Vector instance = new Vector(size, FiniteField.getDefaultFiniteField());
        Vector instance2 = new Vector(size, FiniteField.getDefaultFiniteField());

        for (int i = 0 ; i < 10; i++) {
            instance.setCoordinate(i, 1);
            instance2.setCoordinate(i, 7);
        }

        Vector instance3 = instance.add(instance2);

        for (int i = 0 ; i < 10; i++) {
            assertTrue(instance3.getCoordinate(i) == instance.getFiniteField().sum[1][7]);
        }

    }

    @Test
    public void testScalarMultiply() {

        Vector instance = new Vector(size, FiniteField.getDefaultFiniteField());

        for (int i = 0 ; i < 10; i++) {
            instance.setCoordinate(i, 3);
        }

        Vector instance2 = instance.scalarMultiply(3);

        for (int i = 0 ; i < 10; i++) {
            assertTrue(instance2.getCoordinate(i) == instance.getFiniteField().mul[3][3]);
        }

    }

    @Test
    public void testHashCode() {
    	
    	Vector v1 = new Vector(10, FiniteField.getDefaultFiniteField());    	
    	v1.setCoordinate(1, 5);
    	
    	Vector v2 = new Vector(10, FiniteField.getDefaultFiniteField());    	
    	v2.setCoordinate(1, 3);

    	Vector v3 = new Vector(10, FiniteField.getDefaultFiniteField());    	
    	v3.setCoordinate(1, 5);

    	Vector v4 = new Vector(10, FiniteField.getDefaultFiniteField());
     	
    	assertEquals(v1.hashCode(), v1.hashCode());
    	
    	assertFalse(v1.hashCode() == v2.hashCode());
    	
    	assertEquals(v1.hashCode(), v3.hashCode());
    	
    	assertFalse(v1.hashCode() == v4.hashCode());
    	
    	
    	Vector v5 = new Vector(10, new FiniteField(2, 8));
    	v5.setCoordinate(1, 5);
    	
    	assertFalse(v1.hashCode() == v5.hashCode());
    	
    	assertTrue(new Vector(10, null).hashCode() == new Vector(10, null).hashCode());
    	
    }
    

    @Test
    public void testEquals() {
    	
		 
		Vector v = new Vector(10, FiniteField.getDefaultFiniteField());
	
		v.setCoordinate(9, 1);
		
		assertFalse(v.equals(new Object()));
		assertFalse(v.equals(null));
		assertFalse(v.equals(new Vector(10, FiniteField.getDefaultFiniteField())));
		assertFalse(v.equals(new Vector(15, FiniteField.getDefaultFiniteField())));
		assertFalse(v.equals(new Vector(5, FiniteField.getDefaultFiniteField())));
	
		Vector v2 = new Vector(10, FiniteField.getDefaultFiniteField());
		
		v2.setCoordinate(9, 1);
	
		assertTrue(v.equals(v2));
		
		Vector v5 = new Vector(10, new FiniteField(2, 8));
		v5.setCoordinate(9, 1);
    	
    	assertFalse(v.equals(v5));
    	
    	assertTrue(v.equals(v));
		
    	assertFalse( new Vector(10, null).equals(new Vector(10, FiniteField.getDefaultFiniteField())));
    	assertTrue( new Vector(10, null).equals(new Vector(10, null)));
    	
    }
    
}