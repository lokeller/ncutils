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
	 * Returns the complement of this vector space, i.e. a vector space
	 * that shares only the zero vector with this space that has a base
	 * that combined with the base of this subspace spans the whole space
	 * FiniteField ^ vectorLength .   
	 * 
	 * @return the complement of this vector space
	 */
	public VectorSpace getComplement() {
		
		CodingVectorDecoder decoder2 = decoder.copy();		

		ArrayList<Vector> complementaryBase = new ArrayList<Vector>();
		
		for ( int i = 0 ; i < baseMatrix.getColumnCount() ; i++) {
			
			Vector v = new Vector(baseMatrix.getColumnCount(), baseMatrix.getFiniteField());			
			v.setCoordinate(i, 1);
			
			if ( decoder2.addVector(v) != null) {
				complementaryBase.add(v);
			}			
			
		}
		
		return new VectorSpace(baseMatrix.getFiniteField(), baseMatrix.getColumnCount(), complementaryBase.toArray(new Vector[0]));
		
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
		
		// create a new base of the whole space		
		VectorSpace complement = getComplement();
		
		Matrix spaceBase = complement.baseMatrix.copy();
		
		for ( int i = 0; i < baseMatrix.getRowCount() ; i++) {	
			spaceBase.appendRow(baseMatrix.copyRow(i));
		}
		
		// find the matrix that is used to do a change of base
		Matrix changeOfBase = spaceBase.toTranspose().toInverse();
		
		// express the basis vector of the second space in terms of this new base
		Matrix otherBase = changeOfBase.multiply(other.baseMatrix.toTranspose()).toTranspose();
		
		// find the row echelon form and identify the rows that are linear combinations of our base
		otherBase = otherBase.toRowEchelonForm();
		
		int complementDimensions = complement.getDimension(); 
		
		Matrix intersectionBase = otherBase;
				
		outer : for ( int i = 0; i < otherBase.rows ; i++) {
			for ( int j = 0 ; j < complementDimensions; j++) {
				if ( otherBase.entries[otherBase.rows - i - 1][j] != 0) {
					intersectionBase = otherBase.copySubMatrix(otherBase.rows - (i-1) - 1, 0, otherBase.rows - 1, otherBase.columns - 1);
					break outer;
				}
			}
		}		
		
		return spaceBase.toTranspose().multiply(intersectionBase.toTranspose()).toTranspose().copyRowSpace();		
		
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
