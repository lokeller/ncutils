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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

/**
 * 
 * This class represents a matrix with elements taken from a given finite field. 
 * The matrix can be modified. Row and column indexes start at 0.
 * 
 * @author lokeller
 *
 */

public class Matrix {

	/** the finite field to which matrix entries belongs */
	FiniteField ff;
	
	/** the entries of the matrix, indexed as [row][column] */
	int[][] entries;
	
	/** number of columns of the matrix, notice that for 0 rows matrices the number of columns can be different than 0 */
	int columns;
	
	/** the number of rows of the matrix, it is always equal to entries.length , notice that for 0 columns matrices 
	 *  the number of rows can be different than 0 */
	int rows;
	
	/**
	 * Creates a new matrix with zero entries of the specified size
	 * 
	 * @param rows number of rows
	 * @param columns number of columns
	 * @param ff the finite field to which matrix entries belong 
	 */
	public Matrix(int rows, int columns, FiniteField ff) {
		
		if (ff == null) throw new IllegalArgumentException("Finite field cannot be null");
		
		this.ff = ff;
		this.columns = columns;
		this.rows = rows;
		this.entries = new int[rows][columns];
	}
		
	private Matrix(int[][] entries, FiniteField ff) {
		this.ff = ff;
		this.entries = entries;
		this.rows = entries.length;
		if (entries.length == 0) {
			this.columns = 0;
		} else {
			this.columns = entries[0].length;
		}
	}
	
	/**
	 * Returns the number of columns of the matrix, notice that a zero rows
	 * matrix can have a non zero number of columns
	 *  
	 * @return the number of columns of this matrix
	 */
	public int getColumnCount() {
		return columns;
	}

	/**
	 * Returns the number of rows of the matrix, notice that a zero columns
	 * matrix can have a non zero number of rows
	 *  
	 * @return the number of rows of this matrix
	 */
	public int getRowCount() {
		return rows;
	}

	/**
	 * Returns the finite field from which the entries of this matrix are picked
	 * 
	 * @return a finite field
	 */
	public FiniteField getFiniteField() {
		return ff;
	}

	/**
	 * Sets the content of an entry of the matrix
	 * 
	 * @param row the row of the entry, indexes starts at 0
	 * @param column the column of the entry, indexes starts at 0
	 * @param value the value ( must be a valid value for the finite field of this matrix)
	 */
	public void setEntry(int row, int column, int value) {
		entries[row][column] = value;
	}
	
	/**
	 * Returns the current value of an entry of the matrix
	 * 
	 * @param row the row (indexes start at 0) 
	 * @param column the column (indexes start at 0)
	 * @return the current value at position (row, column)
	 */
	public int getEntry(int row, int column) {
		return entries[row][column];
	}

	/**
	 * Sets the content of a row to be the same as the content of the specified
	 * vector
	 * 
	 * @param row the row that will be set, indexes starts at 0
	 * @param v a vector containing the entries that should be set
	 */
	public void setRow(int row, Vector v) {
		System.arraycopy(v.coordinates, 0, entries[row], 0, columns);
	}

	/**
	 * Sets the content of a column to be the same as the content of the specified
	 * vector
	 * 
	 * @param column the column that will be set, indexes starts at 0
	 * @param v a vector containing the entries that should be set
	 */
	public void setColumn(int column, Vector v) {
		for ( int i = 0 ; i < rows; i++) {
			entries[i][column] = v.coordinates[i];
		}
	}

	/**
	 * 
	 * Appends the specified vector as a new row at the bottom of the matrix
	 * 
	 * @param v a vector from the finite field of this matrix
	 */
	public void appendRow(Vector v) {
		int[][] newEntries = new int[rows+1][];
		System.arraycopy(entries, 0, newEntries, 0, rows);
		newEntries[rows] = new int[columns];
		entries = newEntries;
		setRow(rows, v);
		rows++;
	}
	
	/**
	 * 
	 * Appends the specified matrix as a new rows at the bottom of the matrix
	 * 
	 * @param m a matrix over the same finite field as this matrix
	 */
	public void appendMatrixBelow(Matrix m) {
		for ( int i = 0 ; i < m.rows; i++) {
			appendRow(Vector.wrap(m.entries[i], m.ff));
		}
	}
	
	/**
	 * 
	 * Appends the specified matrix as a new columns at the left of the matrix
	 * 
	 * @param m a matrix over the same finite field as this matrix
	 */
	public void appendMatrixRight(Matrix m) {
		for ( int i = 0 ; i < m.columns; i++) {
			appendColumn(m.copyColumn(i));
		}
	}
	

	/**
	 * 
	 * Appends the specified vector as a new column at the right of the matrix
	 * 
	 * @param v a vector from the finite field of this matrix
	 */
	public void appendColumn(Vector v) {		
		
		if (rows == 0) {
			entries = new int[v.getLength()][0];
			rows = v.getLength();
		}
		
		int [][] newEntries = new int[rows][columns+1];
		
		for ( int i = 0 ; i < entries.length ; i++) {
			System.arraycopy(entries[i], 0, newEntries[i], 0, columns);
		}
		
		entries = newEntries;
		
		for ( int i = 0 ; i < entries.length ; i++) {
			entries[i][columns] = v.coordinates[i];
		}
		
		columns++;
		
		
	}
	
	/**
	 * Returns the current rank of the matrix
	 * 
	 * @return the number of dimensions of the row space (and of the column space)
	 */
	public int getRank() {
		return copyRowSpace().getDimension();
	}
	
	/**
	 * Returns a copy of the matrix where every entry of the matrix 
	 * has been multiplied with the specified value
	 * 
	 * @param c a value from the matrix finite field
	 * 
	 * @return a copy of this matrix multiplied by c
	 */
	public Matrix scalarMultiply(int c) {
		Matrix output = new Matrix(rows, columns, ff);
		
		int [][] mul = ff.mul;
		
		for ( int i = 0 ; i < output.rows ; i ++ ) {
			for ( int j = 0 ; j < output.columns ; j++) {
				output.entries[i][j] = mul[entries[i][j]][c];				
			}
		}
		
		return output;
		
		
	}
	
	/**
	 * Returns a matrix that is the sum of this matrix 
	 * with a second matrix.
	 * 
	 * @param other the matrix to be added
	 * 
	 * @return a matrix that is the sum of the two matrices
	 */
	public Matrix add(Matrix other) {
		Matrix output = new Matrix(rows, columns, ff);
		
		int [][] sum = ff.sum;
		
		for ( int i = 0 ; i < output.rows ; i ++ ) {
			for ( int j = 0 ; j < output.columns ; j++) {
				output.entries[i][j] = sum[entries[i][j]][other.entries[i][j]];				
			}
		}
		
		return output;
		
		
	}
	
	/**
	 * Returns a matrix that is the product of this matrix 
	 * with a second matrix.
	 * 
	 * @param other the matrix to be multiplied
	 * 
	 * @return a matrix that is equal to this matrix times other matrix
	 */
	public Matrix multiply(Matrix other) {
		
		if ( other.rows != columns) throw new IllegalArgumentException("Invalid matrix size, expected " + columns + " rows, found " + other.rows);
		
		Matrix output = new Matrix(this.rows, other.columns, ff);
		
		int [][] mul = ff.mul;
		int [][] sum = ff.sum;
		
		for ( int i = 0 ; i < output.rows ; i ++ ) {
			for ( int j = 0 ; j < output.columns ; j++) {
				for ( int k = 0 ; k < columns ; k++) {
					output.entries[i][j] = sum[output.entries[i][j]][mul[this.entries[i][k]][other.entries[k][j]]];
				}
			}
		}
		
		return output;
		
	}
	
	/**
	 * Computes the inverse of this matrix. The matrix must be square.
	 * 
	 * @return the inverse of this matrix.
	 */
	public Matrix toInverse() {
		
		if (rows != columns) {
			throw new RuntimeException("Matrix is not square");
		}
		
		CodingVectorDecoder decoder = new CodingVectorDecoder(columns, ff);
		
		Matrix output = new Matrix(this.rows, this.columns, ff);
		
		for (int i = 0 ; i < rows ; i++ ) {
			
			Map<Integer, Vector> decoded = decoder.addVector(new Vector(entries[i], ff));
			
			if ( decoded != null) {
				for ( Map.Entry<Integer, Vector> entry : decoded.entrySet()) {
					output.setRow(entry.getKey(), entry.getValue());
				}
			} else {
				return null;
			}
					
		}
		
		return output;
	}

	/**
	 * Returns the transpose of this matrix
	 * 
	 * @return the transposed version of this matrix
	 */	
	public Matrix toTranspose() {
		Matrix output = new Matrix(columns, rows, ff);
		
		for ( int i = 0 ; i < output.rows ; i ++ ) {
			for ( int j = 0 ; j < output.columns ; j++) {
					output.entries[i][j] = entries[j][i];
			}
		}
		
		return output;
	}

	/**
	 * 
	 * Returns a copy of the matrix reduced to its row echelon form. The row echelon form
	 * is a transformation of the matrix using the gaussian elimination. In this form the
	 * entries below each pivot are set to zero, while the entries above are not changed.
	 * 
	 * @return a copy of the matrix reduced to its row echelon form
	 */
	public Matrix toRowEchelonForm() {
		
		Matrix output = copy();
		
		int nextPivot = 0;
		int nextLine = 0;
		
		while ( nextPivot < columns && nextLine < rows ) {
			
			// move at the right position a row that has a 
			// non zero entry at the current pivot position			
			for ( int i = nextLine ; i < rows ; i++) {
				if ( output.entries[i][nextPivot] != 0) {
					int [] tmp = output.entries[nextLine];
					output.entries[nextLine] = output.entries[i];
					output.entries[i] = tmp;
					break;
				}
			}
			
			// no line has a pivot, move to next column
			if ( output.entries[nextLine][nextPivot] == 0) {
				nextPivot++;
				continue;
			}
			
			
			// make sure we have a 1 at the pivot
			Vector v = Vector.wrap(output.entries[nextLine], ff);			
			v.scalarMultiplyInPlace(ff.inverse[output.entries[nextLine][nextPivot]]);
			
			// zero out all entries below the pivot
			for ( int i = nextLine+1; i < rows; i++) {
				Vector v2 = Vector.wrap(output.entries[i], ff);
				
				v2.multiplyAndAddInPlace(ff.sub[0][v2.getCoordinate(nextPivot)], v);
			}
			
			nextLine++;
			nextPivot++;
			
		}
		
		return output;
		
	}
	
	/**
	 * 
	 * Returns a copy of the matrix reduced to its reduced row echelon form. The reduced row 
	 * echelon form is a transformation of the matrix using the gauss-jordan elimination. In 
	 * this form the entries below and above each pivot are set to zero.
	 * 
	 * @return a copy of the matrix reduced to its reduced row echelon form
	 */
	public Matrix toReducedRowEchelonForm() {
	
		Matrix output = copy();
		
		int nextPivot = 0;
		int nextLine = 0;
		
		while ( nextPivot < columns && nextLine < rows ) {
			
			// move at the right position a row that has a 
			// non zero entry at the current pivot position			
			for ( int i = nextLine ; i < rows ; i++) {
				if ( output.entries[i][nextPivot] != 0) {
					int [] tmp = output.entries[nextLine];
					output.entries[nextLine] = output.entries[i];
					output.entries[i] = tmp;
					break;
				}
			}
			
			// no line has a pivot, move to next column
			if ( output.entries[nextLine][nextPivot] == 0) {
				nextPivot++;
				continue;
			}
			
			
			// make sure we have a 1 at the pivot
			Vector v = Vector.wrap(output.entries[nextLine], ff);			
			v.scalarMultiplyInPlace(ff.inverse[output.entries[nextLine][nextPivot]]);
			
			// zero out all entries below and above the pivot
			for ( int i = 0; i < rows; i++) {
				
				if ( i == nextLine) continue;
				
				Vector v2 = Vector.wrap(output.entries[i], ff);
				
				v2.multiplyAndAddInPlace(ff.sub[0][v2.getCoordinate(nextPivot)], v);
			}
			
			nextLine++;
			nextPivot++;
			
		}
		
		return output;
		
		
	}
	
	/**
	 * Returns true if all the entries of the matrix are zero
	 * 
	 * @return true if all the entries of the matrix are zero, false otherwise
	 */
	public boolean isZero() {
		
		for ( int i = 0 ; i < rows; i++ ) {
			for ( int j = 0 ; j < columns; j++) {
				if ( entries[i][j] != 0) return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 
	 * Returns true if the matrix is an identity matrix.
	 * 
	 * @return true if the matrix is an identity matrix, false otherwise
	 */
	public boolean isIdentity() {
		
		if ( columns != rows) return false;
		
		for ( int i = 0 ; i < rows; i++ ) {
			for ( int j = 0 ; j < columns; j++) {
				if ( i==j ) {
					if (entries[i][j] != 1) {				
						return false;
					}
				} else { 
					if ( entries[i][j] != 0) {
						return false;
					}
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Returns true if the matrix is an upper triangular matrix
	 * 
	 * @return true if all the entries below the diagonal are zero, false otherwise.
	 */
	public boolean isUpperTriangular() {
		for ( int i = 0 ; i < rows; i++ ) {
			for ( int j = 0 ; j < columns; j++) {
				if ( i > j && entries[i][j] != 0) {				
					return false;					
				}
			}
		}		
		return true;
	}
	
	/**
	 * 
	 * Returns a copy of the matrix
	 * 
	 * @return returns a copy of the matrix
	 */
	public Matrix copy() {
		
		int [][] newEntries = new int[rows][columns];
		
		for ( int i = 0 ; i < entries.length ; i++) {
			System.arraycopy(entries[i], 0, newEntries[i], 0, columns);
		}
		
		Matrix matrix = new Matrix(newEntries, ff);
		matrix.columns = columns;
		matrix.rows = rows;
		
		return matrix;
		
	}

	/**
	 * 
	 * Returns the row space of this matrix. The returned space will not change when the matrix
	 * is changed.
	 * 
	 * @return returns a vector space representing the current row space of the matrix
	 */
	public VectorSpace copyRowSpace() {
		
		Vector [] base = new Vector[rows]; 
		
		for ( int i = 0 ; i < rows ; i ++ ) {
			base[i] = new Vector(entries[i], ff);
		}
		
		return new VectorSpace(ff, columns, base);
	}

	/**
	 * Computes the null space of the matrix
	 * 
	 * @return a the null space of the matrix
	 */
	public VectorSpace copyNullSpace() {
		
		Matrix m2 = copy();		
		m2.appendMatrixBelow(createIdentityMatrix(columns, ff));
		
		Matrix m3 = m2.toTranspose().toRowEchelonForm().toTranspose();
		
		ArrayList<Vector> base = new ArrayList<Vector>(); 
		
		for ( int i = 0 ; i < columns; i++) {
			if ( m3.copySubMatrix(0, i, rows - 1, i).isZero()) {
				base.add(m3.copySubMatrix(rows, i, rows + columns - 1, i).copyColumn(0));
			}
		}
		
		return new VectorSpace(ff, columns, base.toArray(new Vector[0]));
		
	}
	
	/**
	 * 
	 * Returns the column space of this matrix. The returned space will not change when the matrix
	 * is changed.
	 * 
	 * @return returns a vector space representing the current column space of the matrix
	 */
	public VectorSpace copyColumnSpace() {
		
		Matrix transpose = toTranspose();
		
		return transpose.copyRowSpace();
	}

	/**
	 * 
	 * Returns a vector containing the current values of the specified row
	 * 
	 * @param row the index of the row (indexes start at 0) 
	 * @return a vector containing the current value of the specified row.
	 */
	public Vector copyRow(int row) {
		return Vector.wrap(entries[row], ff).copy();
	}

	/**
	 * Returns a vector containing the current values of the specified column
	 * 
	 * @param column the index of the column (indexes start at 0) 
	 * @return a vector containing the current value of the specified column.
	 */
	public Vector copyColumn(int column) {
		return Vector.wrap(toTranspose().entries[column], ff).copy();
	}
	
	/**
	 * Returns a slice of this matrix.
	 * 
	 * @param firstRow the first row of the submatrix (indexes start at 0) 
	 * @param firstColumn the first column of the submatrix (indexes start at 0)
	 * @param lastRow the last row of the submatrix (indexes start at 0)
	 * @param lastColumn the last column of the submatrix (indexes start at 0)
	 * 
	 * @return a new matrix with the specified content
	 */
	
	public Matrix copySubMatrix(int firstRow, int firstColumn, int lastRow, int lastColumn) {
		
		Matrix output = new Matrix(lastRow - firstRow + 1, lastColumn - firstColumn + 1, ff);
		
		for (int i = 0 ; i < output.rows ; i++) {
			for (int j = 0 ; j < output.columns ; j++) {
				output.entries[i][j] = entries[i+firstRow][j+firstColumn];
			}
		}
		
		return output;
		
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + columns;
		for (int i = 0 ; i < rows ; i++) {
			result = prime * result + Arrays.hashCode(entries[i]);
		}
		result = prime * result + ff.hashCode();
		result = prime * result + rows;
		return result;
	}

	@Override
	public boolean equals(Object other) {
		
		if ( other == this) return true;
		
		if ( ! (other instanceof Matrix) ) return false;
		
		Matrix otherMatrix = (Matrix) other;
		
		if ( otherMatrix.columns != columns || 
			  otherMatrix.rows != rows || 
			 !otherMatrix.ff.equals(ff)) return false;
		
		for ( int i = 0 ; i < rows; i++ ) {
			for ( int j = 0 ; j < columns; j++) {
				if ( otherMatrix.entries[i][j] != entries[i][j])
					return false;				
			}
		}
		
		
		
		return true;
	}

	@Override
	public String toString() {
		
		StringBuilder b = new StringBuilder();
		
		for (int i = 0 ; i < rows ; i++) {
			for (int j = 0 ; j < columns ; j++) {
				if ( j > 0 ) b.append("\t");
				b.append(entries[i][j]);
			}
			b.append("\n");
		}
		
		return b.toString();
	}

	/**
	 * 
	 * Creates a new matrix that uses the specified array as storage for the 
	 * entries. The number of rows is equal to entries.lenght, and the number
	 * of columns is equal to entries[0].length if there is at least one row
	 * and 0 otherwise. The content of entries is not cleared.
	 * 
	 * @param entries the array that will be used as storage for the matrix entries
	 * @param ff the finite field that the entries belong to
	 * 
	 * @return a matrix with the specified properties
	 */
	public static Matrix wrap(int [][] entries, FiniteField ff) {
		return new Matrix(entries, ff);
	}

	/**
	 * Creates a matrix with entries chose uniformly from the finite field.  
	 * 
	 * @param rows number of rows of the matrix
	 * @param columns number of columns of the matrix
	 * @param ff the finite field to which the matrix entries will belong
	 * @return a random matrix of size rows x columns with entries randomly chosen from ff
	 */
	public static Matrix createRandomMatrix(int rows, int columns, FiniteField ff) {
		return createRandomMatrix(rows, columns, ff, new Random().nextLong());
	}

	/**
	 * Creates a matrix with entries chose uniformly from the finite field.  
	 * 
	 * @param rows number of rows of the matrix
	 * @param columns number of columns of the matrix
	 * @param ff the finite field to which the matrix entries will belong
	 * @param seed a seed used to initialize the random number generator 
	 * @return a random matrix of size rows x columns with entries randomly chosen from ff
	 */
	public static Matrix createRandomMatrix(int rows, int columns, FiniteField ff, long seed) {
		Matrix matrix = new Matrix(rows, columns, ff);
		
		Random rand = new Random(seed);
		
		for ( int i = 0 ; i < rows; i++ ) {
			for ( int j = 0 ; j < columns; j++) {
				matrix.entries[i][j] = rand.nextInt(ff.getCardinality());
			}
		}
		
		return matrix;
	}

	/**
	 * 
	 * Creates an identity matrix with the specified size
	 * 
	 * @param size the number of rows (and therefore columns) of the matrix
	 * @param ff the finite field from which the matrix entries will belong
	 * 
	 * @return an identity matrix of the specified size
	 */
	public static Matrix createIdentityMatrix(int size, FiniteField ff) {
		Matrix matrix = new Matrix(size, size, ff);
		
		for ( int i = 0 ; i < size; i++ ) {
			matrix.entries[i][i] = 1;
		}
		
		return matrix;
	}
	
}
