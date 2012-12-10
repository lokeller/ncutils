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

import java.util.ArrayList;

/**
 * Represents a vector space spanned by a set of vectors. A VectorSpace is immutable.
 * 
 * @author lokeller
 *
 */

public class VectorSpace {
	
	
	/** a decoder that has been initialized with the basis vectors */
	private CodingVectorDecoder decoder;
	
	/** the rows of this matrix are a base of the vector space.
	 *  This matrix has been initialized with the reduced row echelon form of the 
	 *  matrix whose rows are the basis vectors.
	 */
	private Matrix baseMatrix;	
	
	/**
	 * Creates a new vector space with the specified base
	 * 
	 * @param ff the finite field to which the base entries belong
	 * @param vectorLen the length of the base vectors 
	 * @param base the base vectors
	 */
	public VectorSpace(FiniteField ff, int vectorLen, Vector[] base) {
		
		if ( ff == null) throw new IllegalArgumentException("Finite field cannot be null");		
		
		ArrayList<Vector> list = new ArrayList<Vector>();
		
		decoder = new CodingVectorDecoder(vectorLen, ff);
		
		baseMatrix = new Matrix(0, vectorLen, ff);
		
		for ( Vector v : base) {			
			if ( decoder.addVector(v) != null) {				
				list.add(v.copy());
				baseMatrix.appendRow(v);
			}			
		}		
		
		this.baseMatrix = baseMatrix.toReducedRowEchelonForm();
		
	}
	
	/**
	 * Returns the dimension of this vector space
	 * 
	 * @return the dimension of the space
	 */
	public int getDimension() {
		return decoder.getSubspaceSize();
	}
	
	/**
	 * 
	 * Returns a base for this vector space
	 * 
	 * @return a base of the subspace
	 */
	public Vector[] getBase() {
		
		Vector[] base = new Vector[baseMatrix.getRowCount()];
		
		for ( int i = 0; i < baseMatrix.getRowCount() ; i++) {
			base[i] = baseMatrix.copyRow(i);
		}
		
		return base;
	}
	
	/**
	 * Returns the orthogonal complement of this vector space.   
	 * 
	 * @return the orthogonal complement of this vector space
	 */
	public VectorSpace getOrthogonalComplement() {
		
		return baseMatrix.copyNullSpace();
				
	}
	
	
	/**
	 * Returns a vector space that contains only the linear combinations of vectors
	 * in this space and in the another space. 
	 * 
	 * @param other another vector space
	 * 
	 * @return the sum of the two subspaces
	 */
	public VectorSpace getSum(VectorSpace other) {
		
		ArrayList<Vector> sumBase = new ArrayList<Vector>();
		
		CodingVectorDecoder decoder2 = decoder.copy();
		
		for ( int i = 0; i < baseMatrix.getRowCount() ; i++) {			
			sumBase.add(baseMatrix.copyRow(i));						
		}

		for ( int i = 0; i < other.baseMatrix.getRowCount() ; i++) {	
			Vector v = other.baseMatrix.copyRow(i);
			if ( decoder2.addVector(v) != null) {				
				sumBase.add(v);
			}			
		}
		
		return new VectorSpace(baseMatrix.getFiniteField(), baseMatrix.getColumnCount(), sumBase.toArray(new Vector[0]));
	}
	
	
	/**
	 * Returns the intersection between this vector space and another vector space.
	 * 
	 * @param other another vector space
	 * 
	 * @return the vector space containing all the vectors that belong both to this vector
	 * space and other
	 */
	public VectorSpace getIntersection(VectorSpace other) {
		
		if ( !baseMatrix.ff.equals(other.baseMatrix.ff) ||
				baseMatrix.columns != other.baseMatrix.columns) 
			throw new IllegalArgumentException("Cannot intersect vector spaces with different type of vectors");
		
		Matrix B = baseMatrix.toTranspose();
		Matrix C = other.baseMatrix.toTranspose();
		
		Matrix minusI1 = Matrix.createIdentityMatrix(B.rows, B.ff).scalarMultiply(B.ff.sub[0][1]); 
		Matrix minusI2 = Matrix.createIdentityMatrix(C.rows, C.ff).scalarMultiply(B.ff.sub[0][1]);
		
		Matrix zeros1 = new Matrix(B.rows, C.columns, B.ff);
		Matrix zeros2 = new Matrix(C.rows, B.columns, B.ff);
		
		Matrix T1 = B.copy();
		T1.appendMatrixRight(zeros1);
		T1.appendMatrixRight(minusI1);
		
		Matrix T2 = zeros2.copy();
		T2.appendMatrixRight(C);
		T2.appendMatrixRight(minusI2);
		
		Matrix T = T1.copy();
		T.appendMatrixBelow(T2);
		
		VectorSpace nullSpace = T.copyNullSpace();
				
		Matrix b2 = nullSpace.baseMatrix.copySubMatrix(0, 
														B.columns + C.columns, 
														nullSpace.baseMatrix.rows - 1, 
														nullSpace.baseMatrix.columns - 1);
		
		ArrayList<Vector> base = new ArrayList<Vector>();		
		
		for ( int i = 0 ; i < b2.rows; i++) {
			base.add(b2.copyRow(i));
		}
		
		return new VectorSpace(baseMatrix.ff, baseMatrix.columns, base.toArray(new Vector[0]));
	}

	/**
	 * Returns true if the vector v is contained in this vector space
	 * 
	 * @param v a vector
	 * 
	 * @return true if the vector is contained in this space, false otherwise
	 */
	public boolean contains(Vector v) {		
		
		return decoder.copy().addVector(v) == null;
	}
	
	/**
	 * Returns true if the given vector space is a subspace of this vector space
	 * 
	 * @param other another subspace
	 * 
	 * @return true if other is a subspace, false otherwise
	 */
	public boolean contains(VectorSpace other) {
		
		if ( other.getDimension() > getDimension()) return false;
		
		CodingVectorDecoder newDecoder = this.decoder.copy(); 
		
		for ( int i = 0; i < other.baseMatrix.getRowCount() ; i++) {	
			if ( newDecoder.addVector(other.baseMatrix.copyRow(i)) != null) {
				return false;
			}
		}
		
		return true;
		
	}
	
	/**
	 * 
	 * Returns true if this subspace contains only the zero element
	 * 
	 * @return true if this is the zero space, false otherwise
	 */
	public boolean isZero() {
		return baseMatrix.rows == 0;
	}

	@Override
	public int hashCode() {				
		return baseMatrix.hashCode();		
	}
	
	@Override
	public boolean equals(Object other) {
		
		if ( other == this) return true;
		
		if ( !( other instanceof VectorSpace) ) return false;
		
		VectorSpace otherSubspace = (VectorSpace) other;		
				
		if ( otherSubspace.baseMatrix.equals(this.baseMatrix)) return true;
		
		return false;
		
	}
	
	@Override
	public String toString() {
		
		StringBuilder b = new StringBuilder();
		
		b.append("<");
		
		for ( int i = 0 ; i < baseMatrix.rows; i++) {
			if (i>0) b.append(",");
			b.append(baseMatrix.copyRow(i));
		}
		
		b.append(">");
		
		return b.toString();
	}
	
}
