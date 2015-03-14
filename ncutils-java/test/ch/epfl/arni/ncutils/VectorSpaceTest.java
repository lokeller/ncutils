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

import static org.junit.Assert.*;

import org.junit.Test;

public class VectorSpaceTest {

	@Test
	public void testEquals() {

		try {
			new VectorSpace(null, 10, new Vector[0]);
			fail();
		} catch (Exception ex) {}
		
		Matrix matrix = Matrix.createRandomMatrix(10, 20, FiniteField.getDefaultFiniteField(), 21321);
	
		VectorSpace space = matrix.copyRowSpace();
				
		assertTrue(space.equals(space));
		assertFalse(space.equals(new Object()));		
		assertFalse(space.equals(null));
		
		assertFalse(space.equals(Matrix.createIdentityMatrix(3, FiniteField.getDefaultFiniteField()).copyRowSpace()));
	
		Matrix matrix3 = Matrix.wrap(matrix.entries, new FiniteField(2,5));
		assertFalse(matrix3.copyRowSpace().equals(space));
		
		Matrix matrix2 = matrix.copy();		
		matrix2.setColumn(3, new Vector(10, FiniteField.getDefaultFiniteField()));
		assertFalse(space.equals(matrix2.copyRowSpace()));
		
		assertTrue(space.equals(matrix.scalarMultiply(3).copyRowSpace()));
		
		assertTrue(Matrix.createRandomMatrix(10, 10, FiniteField.getDefaultFiniteField(), 323423).copyRowSpace()
					.equals(Matrix.createRandomMatrix(10, 10, FiniteField.getDefaultFiniteField(), 232423).copyRowSpace()));
		
	}

	@Test
	public void testHashCode() {

		Matrix matrix = Matrix.createRandomMatrix(10, 20, FiniteField.getDefaultFiniteField(), 21321);
		
		VectorSpace space = matrix.copyRowSpace();		
		
		assertEquals(space.hashCode(), matrix.scalarMultiply(3).copyRowSpace().hashCode());
		
		assertEquals(Matrix.createRandomMatrix(20, 20, FiniteField.getDefaultFiniteField(), 23423).copyRowSpace().hashCode(),
				Matrix.createRandomMatrix(20, 20, FiniteField.getDefaultFiniteField(), 23423).copyRowSpace().hashCode());
		
	}
	
	@Test
	public void test() {

		Matrix matrix = Matrix.createRandomMatrix(10, 20, FiniteField.getDefaultFiniteField(), 21321);
		
		VectorSpace space = matrix.copyRowSpace();
				
		assertTrue(space.equals(space));
		assertFalse(space.equals(new Object()));		
		assertFalse(space.equals(null));
		
		assertTrue(space.equals(matrix.scalarMultiply(3).copyRowSpace()));
		
	}
	
	@Test
	public void testComplement() {

		Matrix matrix = Matrix.createRandomMatrix(15, 20, FiniteField.getDefaultFiniteField(), 21321);
		
		VectorSpace space = matrix.copyRowSpace();
		
		VectorSpace complementSpace = space.getOrthogonalComplement();
				
		assertEquals(15, space.getDimension());
		assertEquals(5, complementSpace.getDimension());
		
		assertEquals(Matrix.createIdentityMatrix(20, FiniteField.getDefaultFiniteField()).copyRowSpace(), space.getSum(complementSpace));
		
		for ( Vector v : space.getBase()) {			
			assertFalse(complementSpace.contains(v));
		}
		
		for ( Vector v : complementSpace.getBase()) {			
			assertFalse(space.contains(v));
		}
		
	}
	
	@Test
	public void testIntersection() {
		
		FiniteField ff = FiniteField.getDefaultFiniteField();
		
		VectorSpace space = new VectorSpace(ff, 3, new Vector[] { new Vector(new int[] { 1 , 0 , 0 }, ff) });
		VectorSpace space2 = new VectorSpace(ff, 3, new Vector[] { new Vector(new int[] { 1 , 1 , 0 }, ff) });
		VectorSpace space3 = new VectorSpace(ff, 3, new Vector[] { new Vector(new int[] { 1 , 1 , 1 }, ff) });
		
		assertTrue(space.getIntersection(space2).isZero());						
		assertTrue(space.getIntersection(space2.getSum(space3)).isZero());		
		assertTrue(space.getSum(space3).getIntersection(space2.getSum(space3)).equals(space3));
		
		VectorSpace space4 = Matrix.createRandomMatrix(10, 10, FiniteField.getDefaultFiniteField(),213).copyRowSpace();		
		VectorSpace space5 = Matrix.createRandomMatrix(10, 10, FiniteField.getDefaultFiniteField(),42365).copyRowSpace();
		
		assertTrue(space4.getIntersection(space5).equals(space4));									
		
		VectorSpace space6 = Matrix.createRandomMatrix(3, 10, FiniteField.getDefaultFiniteField(),213).copyRowSpace();		
		VectorSpace space7 = Matrix.createRandomMatrix(2, 10, FiniteField.getDefaultFiniteField(),42365).copyRowSpace();
		VectorSpace space8 = Matrix.createRandomMatrix(3, 10, FiniteField.getDefaultFiniteField(),42365).copyRowSpace();
				
		assertTrue(space6.getSum(space7).getIntersection(space8.getSum(space7)).equals(space7));
		
	}
	
	@Test
	public void testContains() {
		VectorSpace space = Matrix.createRandomMatrix(5, 10, FiniteField.getDefaultFiniteField(),213).copyRowSpace();
		
		VectorSpace space2 = Matrix.createRandomMatrix(3, 10, FiniteField.getDefaultFiniteField(),42365).copyRowSpace();
		
		assertFalse(space2.contains(space));
	}
	
	@Test
	public void testToString() {
		
		VectorSpace space = Matrix.createRandomMatrix(2, 2, FiniteField.getDefaultFiniteField(), 123).copyRowSpace();
		
		assertEquals("<01  00 ,00  01 >", space.toString());
		
	}
	
}
