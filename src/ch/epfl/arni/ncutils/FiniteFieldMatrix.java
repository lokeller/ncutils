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

import java.util.Arrays;
import java.util.Map;
import java.util.Random;

public class FiniteFieldMatrix {

	FiniteField ff;
	
	int[][] entries;
	
	int columns;
	int rows;
	
	public FiniteFieldMatrix(int rows, int columns, FiniteField ff) {
		
		if (ff == null) throw new IllegalArgumentException("Finite field cannot be null");
		
		this.ff = ff;
		this.columns = columns;
		this.rows = rows;
		this.entries = new int[rows][columns];
	}
		
	FiniteFieldMatrix(int[][] entries, FiniteField ff) {
		this.ff = ff;
		this.entries = entries;
		this.rows = entries.length;
		if (entries.length == 0) {
			this.columns = 0;
		} else {
			this.columns = entries[0].length;
		}
	}
	
	public int getColumnCount() {
		return columns;
	}

	public int getRowCount() {
		return rows;
	}

	public static FiniteFieldMatrix wrap(int [][] entries, FiniteField ff) {
		return new FiniteFieldMatrix(entries, ff);
	}
	
	public void setRow(int row, FiniteFieldVector v) {
		System.arraycopy(v.coordinates, 0, entries[row], 0, columns);
	}
	
	public void setColumn(int column, FiniteFieldVector v) {
		for ( int i = 0 ; i < rows; i++) {
			entries[i][column] = v.coordinates[i];
		}
	}
	
	public void setEntry(int row, int column, int value) {
		entries[row][column] = value;
	}
	
	public void appendRow(FiniteFieldVector v) {
		int[][] newEntries = new int[rows+1][];
		System.arraycopy(entries, 0, newEntries, 0, rows);
		newEntries[rows] = new int[columns];
		entries = newEntries;
		setRow(rows, v);
		rows++;
	}
	
	public void appendColumn(FiniteFieldVector v) {
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
	
	public FiniteFieldMatrix getInverse() {
		
		if (rows != columns) {
			throw new RuntimeException("Matrix is not square");
		}
		
		CodingVectorDecoder decoder = new CodingVectorDecoder(columns, ff);
		
		FiniteFieldMatrix output = new FiniteFieldMatrix(this.rows, this.columns, ff);
		
		for (int i = 0 ; i < rows ; i++ ) {
			try {
				Map<Integer, FiniteFieldVector> decoded = decoder.addVector(new FiniteFieldVector(entries[i], ff));
				
				for ( Map.Entry<Integer, FiniteFieldVector> entry : decoded.entrySet()) {
					output.setRow(entry.getKey(), entry.getValue());
				}
				
			} catch (LinearDependantException e) { return null; }
		}
		
		return output;
	}
	
	public FiniteFieldMatrix getTranspose() {
		FiniteFieldMatrix output = new FiniteFieldMatrix(columns, rows, ff);
		
		for ( int i = 0 ; i < output.rows ; i ++ ) {
			for ( int j = 0 ; j < output.columns ; j++) {
					output.entries[i][j] = entries[j][i];
			}
		}
		
		return output;
	}
	
	public VectorSpace copyRowSpace() {
		
		FiniteFieldVector [] base = new FiniteFieldVector[rows]; 
		
		for ( int i = 0 ; i < rows ; i ++ ) {
			base[i] = new FiniteFieldVector(entries[i], ff);
		}
		
		return new VectorSpace(columns, base, ff);
	}
	
	public VectorSpace copyColumnSpace() {
		
		FiniteFieldMatrix transpose = getTranspose();
		
		return transpose.copyRowSpace();
	}
	
	public int getRank() {
		return copyRowSpace().getDimension();
	}
	
	public FiniteFieldMatrix scalarMultiply(int c) {
		FiniteFieldMatrix output = new FiniteFieldMatrix(rows, columns, ff);
		
		int [][] mul = ff.mul;
		
		for ( int i = 0 ; i < output.rows ; i ++ ) {
			for ( int j = 0 ; j < output.columns ; j++) {
				output.entries[i][j] = mul[entries[i][j]][c];				
			}
		}
		
		return output;
		
		
	}
	
	public FiniteFieldMatrix add(FiniteFieldMatrix other) {
		FiniteFieldMatrix output = new FiniteFieldMatrix(rows, columns, ff);
		
		int [][] sum = ff.sum;
		
		for ( int i = 0 ; i < output.rows ; i ++ ) {
			for ( int j = 0 ; j < output.columns ; j++) {
				output.entries[i][j] = sum[entries[i][j]][other.entries[i][j]];				
			}
		}
		
		return output;
		
		
	}
	
	public FiniteFieldMatrix multiply(FiniteFieldMatrix other) {
		
		FiniteFieldMatrix output = new FiniteFieldMatrix(this.rows, other.columns, ff);
		
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
	
	
	public FiniteFieldMatrix toRowEchelonForm() {
		
		FiniteFieldMatrix output = copy();
		
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
			FiniteFieldVector v = FiniteFieldVector.wrap(output.entries[nextLine], ff);			
			v.scalarMultiplyInPlace(ff.inverse[output.entries[nextLine][nextPivot]]);
			
			// zero out all entries below the pivot
			for ( int i = nextLine+1; i < rows; i++) {
				FiniteFieldVector v2 = FiniteFieldVector.wrap(output.entries[i], ff);
				
				v2.multiplyAndAddInPlace(ff.sub[0][v2.getCoordinate(nextPivot)], v);
			}
			
			nextLine++;
			nextPivot++;
			
		}
		
		return output;
		
	}
	
	public FiniteFieldMatrix toReducedRowEchelonForm() {
	
		FiniteFieldMatrix output = copy();
		
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
			FiniteFieldVector v = FiniteFieldVector.wrap(output.entries[nextLine], ff);			
			v.scalarMultiplyInPlace(ff.inverse[output.entries[nextLine][nextPivot]]);
			
			// zero out all entries below and above the pivot
			for ( int i = 0; i < rows; i++) {
				
				if ( i == nextLine) continue;
				
				FiniteFieldVector v2 = FiniteFieldVector.wrap(output.entries[i], ff);
				
				v2.multiplyAndAddInPlace(ff.sub[0][v2.getCoordinate(nextPivot)], v);
			}
			
			nextLine++;
			nextPivot++;
			
		}
		
		return output;
		
		
	}
	
	public FiniteFieldMatrix copy() {
		
		int [][] newEntries = new int[rows][columns];
		
		for ( int i = 0 ; i < entries.length ; i++) {
			System.arraycopy(entries[i], 0, newEntries[i], 0, columns);
		}
		
		FiniteFieldMatrix finiteFieldMatrix = new FiniteFieldMatrix(newEntries, ff);
		finiteFieldMatrix.columns = columns;
		finiteFieldMatrix.rows = rows;
		
		return finiteFieldMatrix;
		
	}
	
	public static FiniteFieldMatrix createRandomMatrix(int rows, int columns, FiniteField ff) {
		return createRandomMatrix(rows, columns, ff, new Random().nextLong());
	}
	
	public static FiniteFieldMatrix createRandomMatrix(int rows, int columns, FiniteField ff, long seed) {
		FiniteFieldMatrix matrix = new FiniteFieldMatrix(rows, columns, ff);
		
		Random rand = new Random(seed);
		
		for ( int i = 0 ; i < rows; i++ ) {
			for ( int j = 0 ; j < columns; j++) {
				matrix.entries[i][j] = rand.nextInt(ff.getCardinality());
			}
		}
		
		return matrix;
	}
	
	public static FiniteFieldMatrix createIdentityMatrix(int size, FiniteField ff) {
		FiniteFieldMatrix matrix = new FiniteFieldMatrix(size, size, ff);
		
		for ( int i = 0 ; i < size; i++ ) {
			matrix.entries[i][i] = 1;
		}
		
		return matrix;
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
		
		if ( ! (other instanceof FiniteFieldMatrix) ) return false;
		
		FiniteFieldMatrix otherMatrix = (FiniteFieldMatrix) other;
		
		if ( otherMatrix.columns != columns || 
			  otherMatrix.rows != rows ) return false;
			 
		
		for ( int i = 0 ; i < rows; i++ ) {
			for ( int j = 0 ; j < columns; j++) {
				if ( otherMatrix.entries[i][j] != entries[i][j])
					return false;				
			}
		}
		
		return true;
	}

	public FiniteField getFiniteField() {
		return ff;
	}

	public int getEntry(int i, int j) {
		return entries[i][j];
	}

	public FiniteFieldVector copyRow(int i) {
		return FiniteFieldVector.wrap(entries[i], ff).copy();
	}
	
	public FiniteFieldVector copyColumn(int i) {
		return FiniteFieldVector.wrap(getTranspose().entries[i], ff).copy();
	}
	
	public boolean isZero() {
		
		for ( int i = 0 ; i < rows; i++ ) {
			for ( int j = 0 ; j < columns; j++) {
				if ( entries[i][j] != 0) return false;
			}
		}
		
		return true;
	}
	
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
	
	public FiniteFieldMatrix copySubMatrix(int firstRow, int firstColumn, int lastRow, int lastColumn) {
		
		FiniteFieldMatrix output = new FiniteFieldMatrix(lastRow - firstRow + 1, lastColumn - firstColumn + 1, ff);
		
		for (int i = 0 ; i < output.rows ; i++) {
			for (int j = 0 ; j < output.columns ; j++) {
				output.entries[i][j] = entries[i+firstRow][j+firstColumn];
			}
		}
		
		return output;
		
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
	
}
