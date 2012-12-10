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
import ch.epfl.arni.ncutils.Matrix;

public class MatrixTest {

	@Test
	public void testFiniteFieldMatrixIntIntFiniteField() {

		try {
			new Matrix(10, 20, null);
			fail();
		} catch (Exception e) {}
		
		Matrix m = new Matrix(10, 20, FiniteField.getDefaultFiniteField());
		
		assertEquals(10, m.getRowCount());
		assertEquals(20, m.getColumnCount());

		assertEquals(FiniteField.getDefaultFiniteField(), m.getFiniteField());
		
	}

	@Test
	public void testFiniteFieldMatrixIntArrayArrayFiniteField() {
		
		int [][] data = new int[10][20];
		
		data[1][1] = 1 ;
		
		Matrix m = Matrix.wrap(data, FiniteField.getDefaultFiniteField());
		
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
		
		Matrix m = Matrix.wrap(data, FiniteField.getDefaultFiniteField());
		
		assertEquals(10, m.getRowCount());
		assertEquals(20, m.getColumnCount());

		assertEquals(FiniteField.getDefaultFiniteField(), m.getFiniteField());
		
		assertEquals(1, m.getEntry(1,1));
		
		data[1][2] = 1 ;
		
		assertEquals(1, m.getEntry(1,2));				
		
		m.setEntry(2,2,3);
		
		assertEquals(3, data[2][2]);
		
		Matrix m2 = Matrix.wrap(new int[0][0], FiniteField.getDefaultFiniteField());
		
		assertEquals(0, m2.getColumnCount());
		assertEquals(0, m2.getRowCount());
		
	}

	@Test
	public void testSetGetRow() {
		
		Matrix m = new Matrix(10, 20, FiniteField.getDefaultFiniteField());
		
		m.setEntry(3, 1, 1);
		
		Vector v = new Vector(20, FiniteField.getDefaultFiniteField());
		
		v.setCoordinate(3, 10);
		
		m.setRow(3, v);
		
		assertTrue(m.copyRow(3).equals(v));
		assertEquals(10, m.getEntry(3, 3));
	}

	@Test
	public void testSetGetColumn() {
		Matrix m = new Matrix(10, 20, FiniteField.getDefaultFiniteField());
		
		m.setEntry(3, 1, 1);
		
		Vector v = new Vector(10, FiniteField.getDefaultFiniteField());
		
		v.setCoordinate(3, 10);
		
		m.setColumn(1, v);
		
		assertTrue(m.copyColumn(1).equals(v));
		assertEquals(10, m.getEntry(3, 1));
		
	}

	@Test
	public void testAppendRow() {
	
		Matrix m = new Matrix(10, 20, FiniteField.getDefaultFiniteField());
				
		Vector v = new Vector(20, FiniteField.getDefaultFiniteField());
		
		v.setCoordinate(3, 10);
		
		m.appendRow(v);
				
		assertEquals(10, m.getEntry(10, 3));		
		
	}

	@Test
	public void testAppendColumn() {
		Matrix m = new Matrix(10, 20, FiniteField.getDefaultFiniteField());
				
		Vector v = new Vector(10, FiniteField.getDefaultFiniteField());
		
		v.setCoordinate(3, 10);
		
		m.appendColumn(v);
				
		assertEquals(10, m.getEntry(3, 20));		
	}

	@Test
	public void testGetInverse() {
	
		try {
			new Matrix(2,3, FiniteField.getDefaultFiniteField()).toInverse();
			fail();
		} catch (Exception e) {}
	
		
		assertEquals(null, new Matrix(3,3, FiniteField.getDefaultFiniteField()).toInverse());		
		
		Matrix A = Matrix.createRandomMatrix(10, 10, FiniteField.getDefaultFiniteField(), 12132341);
		
		Matrix m = Matrix.createRandomMatrix(10, 10, FiniteField.getDefaultFiniteField(), 12131);
		
		Matrix m1 = m.toInverse();
		
		Matrix m3 = A.multiply(m).multiply(m1);
		
		assertEquals(A, m3);		
		
		assertTrue(m.multiply(m1).isIdentity());
		
	}

	@Test
	public void testGetTranspose() {
		
		Matrix m = new Matrix(10, 20, FiniteField.getDefaultFiniteField());		
		m.setEntry(3, 1, 1);
		
		assertEquals(1, m.toTranspose().getEntry(1, 3));
	}
	
	@Test
	public void testGetRowSpace() {
		
		Matrix m = Matrix.createRandomMatrix(5, 10, FiniteField.getDefaultFiniteField(), 2342);
		
		assertEquals(5, m.getRank());
		
		VectorSpace s = m.copyRowSpace();		
		
		assertEquals(5, s.getDimension());
		
		Vector v = m.copyRow(3).add(m.copyRow(4));
		
		assertTrue(s.contains(v));
		
		Vector v2 = m.copyRow(3);
		v2.setCoordinate(0, 5);
		
		assertFalse(s.contains(v2));		
		
	}

	@Test
	public void testGetColumnSpace() {
		Matrix m = Matrix.createRandomMatrix(5, 10, FiniteField.getDefaultFiniteField(), 23423);
		
		assertEquals(5, m.getRank());
		
		VectorSpace s = m.copyColumnSpace();
		
		assertEquals(5, s.getDimension());		
		
		Vector v = m.copyColumn(3).add(m.copyColumn(4));
		
		assertTrue(s.contains(v));
		
	}


	@Test
	public void testScalarMultiply() {
		
		Matrix m = Matrix.createRandomMatrix(5, 10, FiniteField.getDefaultFiniteField(), 23423);
		
		Matrix m2 = m.scalarMultiply(3); 
				
		for ( int i = 0 ; i < 5 ; i++) {			
			assertEquals(m.copyRow(i).scalarMultiply(3), m2.copyRow(i));
		}
		
	}

	@Test
	public void testAdd() {
		Matrix m = Matrix.createRandomMatrix(5, 10, FiniteField.getDefaultFiniteField(), 23423);
		Matrix m2 = Matrix.createRandomMatrix(5, 10, FiniteField.getDefaultFiniteField(), 232323);
			
		Matrix m3 = m.add(m2); 
		
		for ( int i = 0 ; i < 5 ; i++) {			
			assertEquals(m.copyRow(i).add(m2.copyRow(i)), m3.copyRow(i));
		}
		
		assertTrue(m.add( m.scalarMultiply( FiniteField.getDefaultFiniteField().sub[0][1])).isZero());
	}

	@Test
	public void testCreateIdentityMatrix() {
		Matrix matrix = Matrix.createIdentityMatrix(5, FiniteField.getDefaultFiniteField());
		
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
		Matrix matrix = Matrix.createRandomMatrix(10, 20, FiniteField.getDefaultFiniteField());
		
		assertEquals(matrix.getFiniteField(), FiniteField.getDefaultFiniteField());
		assertEquals(20, matrix.getColumnCount());
		assertEquals(10, matrix.getRowCount());
	}

	@Test
	public void testUtilityMethods() {
		Matrix matrix = Matrix.createRandomMatrix(10, 10, FiniteField.getDefaultFiniteField(), 1234);
		
		Matrix copy = matrix.copy();
		
		assertEquals(matrix, copy);
		assertEquals(copy.hashCode(), matrix.hashCode());
		
		matrix.setEntry(2, 2, 3);
		
		assertFalse(matrix.equals(copy));
		assertFalse(copy.hashCode() == matrix.hashCode());
		
		assertFalse(matrix.equals(new Matrix(10, 2, FiniteField.getDefaultFiniteField())));
		assertFalse(matrix.equals(new Matrix(2, 10, FiniteField.getDefaultFiniteField())));
		
		assertFalse(matrix.equals(null));
		assertTrue(matrix.equals(matrix));
		assertFalse(matrix.equals(new Object()));
	}

	@Test
	public void testCopySubmatrix() {

		Matrix matrix = Matrix.createIdentityMatrix(10, FiniteField.getDefaultFiniteField());
		
		Matrix m1 = matrix.copySubMatrix(5, 0, 9, 3);
		Matrix m2 = matrix.copySubMatrix(0, 0, 3, 3);
		
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
		
		Matrix m = new Matrix(2,2, FiniteField.getDefaultFiniteField());
	
		assertEquals("0\t0\n0\t0\n", m.toString());		
	}
	
	
	
	@Test
	public void testToRowEchelonForm() {

		Matrix matrix2 = Matrix.createRandomMatrix(10, 20, FiniteField.getDefaultFiniteField(), 1234);
		
		assertTrue(matrix2.toRowEchelonForm().copyRowSpace().equals(matrix2.copyRowSpace()));		
		
		Matrix matrix3 = Matrix.createRandomMatrix(20, 10, FiniteField.getDefaultFiniteField(), 1234);		
		
		assertTrue(matrix3.toRowEchelonForm().copyRowSpace().equals(matrix3.copyRowSpace()));				
		
		Matrix matrix = Matrix.createRandomMatrix(10, 10, FiniteField.getDefaultFiniteField(), 1234);
				
		for ( int i = 0 ; i < 10 ; i++) {
			matrix.setEntry(i, 3, 0);
		}
		
		Matrix rowEchelon = matrix.toRowEchelonForm();		
		
		assertTrue(rowEchelon.isUpperTriangular());
		
		assertTrue(matrix.toRowEchelonForm().copyRowSpace().equals(matrix.copyRowSpace()));		
		
	}

	@Test
	public void testToReducedRowEchelonForm() {

		Matrix matrix2 = Matrix.createRandomMatrix(10, 20, FiniteField.getDefaultFiniteField(), 1234);
				
		assertTrue(matrix2.toReducedRowEchelonForm().copySubMatrix(0, 0, 9, 9).isIdentity());		
		
		Matrix matrix3 = Matrix.createRandomMatrix(20, 10, FiniteField.getDefaultFiniteField(), 1234);		
		
		assertTrue(matrix3.toReducedRowEchelonForm().copySubMatrix(0, 0, 9, 9).isIdentity());
		assertTrue(matrix3.toReducedRowEchelonForm().copySubMatrix(10, 0, 19, 9).isZero());
		
		Matrix matrix = Matrix.createRandomMatrix(10, 10, FiniteField.getDefaultFiniteField(), 1234);
		
		assertTrue(!matrix.isUpperTriangular());
			
		for ( int i = 0 ; i < 10 ; i++) {
			matrix.setEntry(i, 3, 0);
		}
		
		Matrix rowEchelon = matrix.toReducedRowEchelonForm();		
		
		assertTrue(rowEchelon.isUpperTriangular());
		
		assertEquals(0, rowEchelon.getEntry(3, 3));		
				
	}
	

}
