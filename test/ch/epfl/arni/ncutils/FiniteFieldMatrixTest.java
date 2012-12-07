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

import ch.epfl.arni.ncutils.FiniteField;
import ch.epfl.arni.ncutils.FiniteFieldMatrix;

public class FiniteFieldMatrixTest {

	@Test
	public void testFiniteFieldMatrixIntIntFiniteField() {

		try {
			new FiniteFieldMatrix(10, 20, null);
			fail();
		} catch (Exception e) {}
		
		FiniteFieldMatrix m = new FiniteFieldMatrix(10, 20, FiniteField.getDefaultFiniteField());
		
		assertEquals(10, m.getRowCount());
		assertEquals(20, m.getColumnCount());

		assertEquals(FiniteField.getDefaultFiniteField(), m.getFiniteField());
		
	}

	@Test
	public void testFiniteFieldMatrixIntArrayArrayFiniteField() {
		
		int [][] data = new int[10][20];
		
		data[1][1] = 1 ;
		
		FiniteFieldMatrix m = new FiniteFieldMatrix(data, FiniteField.getDefaultFiniteField());
		
		assertEquals(10, m.getRowCount());
		assertEquals(20, m.getColumnCount());

		assertEquals(FiniteField.getDefaultFiniteField(), m.getFiniteField());
		
		assertEquals(1, m.getEntry(1,1));
		
		data[1][2] = 1 ;
		
		assertEquals(1, m.getEntry(1,2));		
		
	}

	@Test
	public void testWrap() {
		int [][] data = new int[10][20];
		
		data[1][1] = 1 ;
		
		FiniteFieldMatrix m = FiniteFieldMatrix.wrap(data, FiniteField.getDefaultFiniteField());
		
		assertEquals(10, m.getRowCount());
		assertEquals(20, m.getColumnCount());

		assertEquals(FiniteField.getDefaultFiniteField(), m.getFiniteField());
		
		assertEquals(1, m.getEntry(1,1));
		
		data[1][2] = 1 ;
		
		assertEquals(1, m.getEntry(1,2));				
		
		m.setEntry(2,2,3);
		
		assertEquals(3, data[2][2]);
		
		FiniteFieldMatrix m2 = FiniteFieldMatrix.wrap(new int[0][0], FiniteField.getDefaultFiniteField());
		
		assertEquals(0, m2.getColumnCount());
		assertEquals(0, m2.getRowCount());
		
	}

	@Test
	public void testSetGetRow() {
		
		FiniteFieldMatrix m = new FiniteFieldMatrix(10, 20, FiniteField.getDefaultFiniteField());
		
		m.setEntry(3, 1, 1);
		
		FiniteFieldVector v = new FiniteFieldVector(20, FiniteField.getDefaultFiniteField());
		
		v.setCoordinate(3, 10);
		
		m.setRow(3, v);
		
		assertTrue(m.copyRow(3).equals(v));
		assertEquals(10, m.getEntry(3, 3));
	}

	@Test
	public void testSetGetColumn() {
		FiniteFieldMatrix m = new FiniteFieldMatrix(10, 20, FiniteField.getDefaultFiniteField());
		
		m.setEntry(3, 1, 1);
		
		FiniteFieldVector v = new FiniteFieldVector(10, FiniteField.getDefaultFiniteField());
		
		v.setCoordinate(3, 10);
		
		m.setColumn(1, v);
		
		assertTrue(m.copyColumn(1).equals(v));
		assertEquals(10, m.getEntry(3, 1));
		
	}

	@Test
	public void testAppendRow() {
	
		FiniteFieldMatrix m = new FiniteFieldMatrix(10, 20, FiniteField.getDefaultFiniteField());
				
		FiniteFieldVector v = new FiniteFieldVector(20, FiniteField.getDefaultFiniteField());
		
		v.setCoordinate(3, 10);
		
		m.appendRow(v);
				
		assertEquals(10, m.getEntry(10, 3));		
		
	}

	@Test
	public void testAppendColumn() {
		FiniteFieldMatrix m = new FiniteFieldMatrix(10, 20, FiniteField.getDefaultFiniteField());
				
		FiniteFieldVector v = new FiniteFieldVector(10, FiniteField.getDefaultFiniteField());
		
		v.setCoordinate(3, 10);
		
		m.appendColumn(v);
				
		assertEquals(10, m.getEntry(3, 20));		
	}

	@Test
	public void testGetInverse() {
	
		try {
			new FiniteFieldMatrix(2,3, FiniteField.getDefaultFiniteField()).getInverse();
			fail();
		} catch (Exception e) {}
	
		
		assertEquals(null, new FiniteFieldMatrix(3,3, FiniteField.getDefaultFiniteField()).getInverse());		
		
		FiniteFieldMatrix A = FiniteFieldMatrix.createRandomMatrix(10, 10, FiniteField.getDefaultFiniteField(), 12132341);
		
		FiniteFieldMatrix m = FiniteFieldMatrix.createRandomMatrix(10, 10, FiniteField.getDefaultFiniteField(), 12131);
		
		FiniteFieldMatrix m1 = m.getInverse();
		
		FiniteFieldMatrix m3 = A.multiply(m).multiply(m1);
		
		assertEquals(A, m3);		
		
		assertTrue(m.multiply(m1).isIdentity());
		
	}

	@Test
	public void testGetTranspose() {
		
		FiniteFieldMatrix m = new FiniteFieldMatrix(10, 20, FiniteField.getDefaultFiniteField());		
		m.setEntry(3, 1, 1);
		
		assertEquals(1, m.getTranspose().getEntry(1, 3));
	}
	
	@Test
	public void testGetRowSpace() {
		
		FiniteFieldMatrix m = FiniteFieldMatrix.createRandomMatrix(5, 10, FiniteField.getDefaultFiniteField(), 2342);
		
		assertEquals(5, m.getRank());
		
		VectorSpace s = m.copyRowSpace();		
		
		assertEquals(5, s.getDimension());
		
		FiniteFieldVector v = m.copyRow(3).add(m.copyRow(4));
		
		assertTrue(s.contains(v));
		
		FiniteFieldVector v2 = m.copyRow(3);
		v2.setCoordinate(0, 5);
		
		assertFalse(s.contains(v2));		
		
	}

	@Test
	public void testGetColumnSpace() {
		FiniteFieldMatrix m = FiniteFieldMatrix.createRandomMatrix(5, 10, FiniteField.getDefaultFiniteField(), 23423);
		
		assertEquals(5, m.getRank());
		
		VectorSpace s = m.copyColumnSpace();
		
		assertEquals(5, s.getDimension());		
		
		FiniteFieldVector v = m.copyColumn(3).add(m.copyColumn(4));
		
		assertTrue(s.contains(v));
		
	}


	@Test
	public void testScalarMultiply() {
		
		FiniteFieldMatrix m = FiniteFieldMatrix.createRandomMatrix(5, 10, FiniteField.getDefaultFiniteField(), 23423);
		
		FiniteFieldMatrix m2 = m.scalarMultiply(3); 
				
		for ( int i = 0 ; i < 5 ; i++) {			
			assertEquals(m.copyRow(i).scalarMultiply(3), m2.copyRow(i));
		}
		
	}

	@Test
	public void testAdd() {
		FiniteFieldMatrix m = FiniteFieldMatrix.createRandomMatrix(5, 10, FiniteField.getDefaultFiniteField(), 23423);
		FiniteFieldMatrix m2 = FiniteFieldMatrix.createRandomMatrix(5, 10, FiniteField.getDefaultFiniteField(), 232323);
			
		FiniteFieldMatrix m3 = m.add(m2); 
		
		for ( int i = 0 ; i < 5 ; i++) {			
			assertEquals(m.copyRow(i).add(m2.copyRow(i)), m3.copyRow(i));
		}
		
		assertTrue(m.add( m.scalarMultiply( FiniteField.getDefaultFiniteField().sub[0][1])).isZero());
	}

	@Test
	public void testCreateIdentityMatrix() {
		FiniteFieldMatrix matrix = FiniteFieldMatrix.createIdentityMatrix(5, FiniteField.getDefaultFiniteField());
		
		for (int i = 0 ; i < 5 ;i++) {
			for (int j = 0 ; j < 5 ; j++) {
				if ( i == j) {
					assertEquals(1, matrix.getEntry(i, j));
				} else {
					assertEquals(0, matrix.getEntry(i, j));
				}
				
			}
		}
	}

	@Test
	public void testCreateRandomMatrix() {
		FiniteFieldMatrix matrix = FiniteFieldMatrix.createRandomMatrix(10, 20, FiniteField.getDefaultFiniteField());
		
		assertEquals(matrix.getFiniteField(), FiniteField.getDefaultFiniteField());
		assertEquals(20, matrix.getColumnCount());
		assertEquals(10, matrix.getRowCount());
	}

	@Test
	public void testUtilityMethods() {
		FiniteFieldMatrix matrix = FiniteFieldMatrix.createRandomMatrix(10, 10, FiniteField.getDefaultFiniteField(), 1234);
		
		FiniteFieldMatrix copy = matrix.copy();
		
		assertEquals(matrix, copy);
		assertEquals(copy.hashCode(), matrix.hashCode());
		
		matrix.setEntry(2, 2, 3);
		
		assertFalse(matrix.equals(copy));
		assertFalse(copy.hashCode() == matrix.hashCode());
		
		assertFalse(matrix.equals(new FiniteFieldMatrix(10, 2, FiniteField.getDefaultFiniteField())));
		assertFalse(matrix.equals(new FiniteFieldMatrix(2, 10, FiniteField.getDefaultFiniteField())));
		
		assertFalse(matrix.equals(null));
		assertTrue(matrix.equals(matrix));
		assertFalse(matrix.equals(new Object()));
	}

	@Test
	public void testCopySubmatrix() {

		FiniteFieldMatrix matrix = FiniteFieldMatrix.createIdentityMatrix(10, FiniteField.getDefaultFiniteField());
		
		FiniteFieldMatrix m1 = matrix.copySubMatrix(5, 0, 9, 3);
		FiniteFieldMatrix m2 = matrix.copySubMatrix(0, 0, 3, 3);
		
		assertEquals(5, m1.getRowCount());
		assertEquals(4, m1.getColumnCount());
		
		matrix.setEntry(1, 1, 3);
		matrix.setEntry(9, 0, 3);
				
		assertTrue(m1.isZero());
		assertFalse(m1.isIdentity());
		
		assertTrue(m2.isIdentity());
		assertFalse(m2.isZero());
		
		assertFalse(matrix.isIdentity());
		
		m2.setEntry(1,2, 1);
		
		assertFalse(m2.isIdentity());
		
		
	}

	
	@Test
	public void testToString() {
		
		FiniteFieldMatrix m = new FiniteFieldMatrix(2,2, FiniteField.getDefaultFiniteField());
	
		assertEquals("0\t0\n0\t0\n", m.toString());		
	}
	
	
	
	@Test
	public void testToRowEchelonForm() {

		FiniteFieldMatrix matrix2 = FiniteFieldMatrix.createRandomMatrix(10, 20, FiniteField.getDefaultFiniteField(), 1234);
		
		assertTrue(matrix2.toRowEchelonForm().copyRowSpace().equals(matrix2.copyRowSpace()));		
		
		FiniteFieldMatrix matrix3 = FiniteFieldMatrix.createRandomMatrix(20, 10, FiniteField.getDefaultFiniteField(), 1234);		
		
		assertTrue(matrix3.toRowEchelonForm().copyRowSpace().equals(matrix3.copyRowSpace()));				
		
		FiniteFieldMatrix matrix = FiniteFieldMatrix.createRandomMatrix(10, 10, FiniteField.getDefaultFiniteField(), 1234);
				
		for ( int i = 0 ; i < 10 ; i++) {
			matrix.setEntry(i, 3, 0);
		}
		
		FiniteFieldMatrix rowEchelon = matrix.toRowEchelonForm();		
		
		assertTrue(rowEchelon.isUpperTriangular());
		
		assertTrue(matrix.toRowEchelonForm().copyRowSpace().equals(matrix.copyRowSpace()));		
		
	}

	@Test
	public void testToReducedRowEchelonForm() {

		FiniteFieldMatrix matrix2 = FiniteFieldMatrix.createRandomMatrix(10, 20, FiniteField.getDefaultFiniteField(), 1234);
				
		assertTrue(matrix2.toReducedRowEchelonForm().copySubMatrix(0, 0, 9, 9).isIdentity());		
		
		FiniteFieldMatrix matrix3 = FiniteFieldMatrix.createRandomMatrix(20, 10, FiniteField.getDefaultFiniteField(), 1234);		
		
		assertTrue(matrix3.toReducedRowEchelonForm().copySubMatrix(0, 0, 9, 9).isIdentity());
		assertTrue(matrix3.toReducedRowEchelonForm().copySubMatrix(10, 0, 19, 9).isZero());
		
		FiniteFieldMatrix matrix = FiniteFieldMatrix.createRandomMatrix(10, 10, FiniteField.getDefaultFiniteField(), 1234);
		
		assertTrue(!matrix.isUpperTriangular());
			
		for ( int i = 0 ; i < 10 ; i++) {
			matrix.setEntry(i, 3, 0);
		}
		
		FiniteFieldMatrix rowEchelon = matrix.toReducedRowEchelonForm();		
		
		assertTrue(rowEchelon.isUpperTriangular());
		
		assertEquals(0, rowEchelon.getEntry(3, 3));		
				
	}
	

}
