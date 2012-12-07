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

public class VectorSpace {

	private FiniteFieldVector[] base;
	private CodingVectorDecoder decoder;
	private FiniteFieldMatrix baseMatrix;
	
	private int vectorLen; 
	private FiniteField ff;	
	
	public VectorSpace(int vectorLen, FiniteFieldVector[] base, FiniteField ff) {
		
		if ( ff == null) throw new IllegalArgumentException("Finite field cannot be null");
		
		this.vectorLen = vectorLen;
		this.ff = ff;
		
		ArrayList<FiniteFieldVector> list = new ArrayList<FiniteFieldVector>();
		
		decoder = new CodingVectorDecoder(vectorLen, ff);
		
		baseMatrix = new FiniteFieldMatrix(0, vectorLen, ff);
		
		for ( FiniteFieldVector v : base) {
			try {				
				decoder.addVector(v);				
				list.add(v.copy());
				baseMatrix.appendRow(v);
			} catch ( LinearDependantException e) {}
		}		
		
		this.baseMatrix = baseMatrix.toReducedRowEchelonForm();
		
		this.base = list.toArray(new FiniteFieldVector[0]);
		
	}
	
	public int getDimension() {
		return decoder.getSubspaceSize();
	}
	
	public boolean isZero() {
		return baseMatrix.rows == 0;
	}
	
	public FiniteFieldVector[] getBase() {
		return base;
	}
	
	public VectorSpace getComplement() {
		
		CodingVectorDecoder decoder = new CodingVectorDecoder(vectorLen, ff);
		
		for (FiniteFieldVector v : base) {
			try {
				decoder.addVector(v);
			} catch (LinearDependantException e) {}
		}

		ArrayList<FiniteFieldVector> complementaryBase = new ArrayList<FiniteFieldVector>();
		
		for ( int i = 0 ; i < vectorLen ; i++) {
			
			FiniteFieldVector v = new FiniteFieldVector(vectorLen, ff);			
			v.setCoordinate(i, 1);
			
			try {
				decoder.addVector(v);
				complementaryBase.add(v);
			} catch (LinearDependantException ex) {}
			
		}
		
		return new VectorSpace(vectorLen, complementaryBase.toArray(new FiniteFieldVector[0]), ff);
		
	}
	
	public VectorSpace getSum(VectorSpace other) {
		
		ArrayList<FiniteFieldVector> sumBase = new ArrayList<FiniteFieldVector>();
		
		CodingVectorDecoder decoder = new CodingVectorDecoder(vectorLen, ff);
		
		for (FiniteFieldVector v : base) {
			try {
				decoder.addVector(v);
				sumBase.add(v);
			} catch (LinearDependantException e) {}
		}

		for (FiniteFieldVector v : other.base) {
			try {
				decoder.addVector(v);
				sumBase.add(v);
			} catch (LinearDependantException e) {}
		}
		
		return new VectorSpace(vectorLen, sumBase.toArray(new FiniteFieldVector[0]), ff);
	}
	
	public VectorSpace getIntersection(VectorSpace other) {
		
		// create a new base of the whole space		
		VectorSpace complement = getComplement();
		
		FiniteFieldMatrix spaceBase = complement.baseMatrix.copy();
		
		for ( FiniteFieldVector v : base) {
			spaceBase.appendRow(v);
		}
		
		// find the matrix that is used to do a change of base
		FiniteFieldMatrix changeOfBase = spaceBase.getTranspose().getInverse();
		
		// express the basis vector of the second space in terms of this new base
		FiniteFieldMatrix otherBase = changeOfBase.multiply(other.baseMatrix.getTranspose()).getTranspose();
		
		// find the row echelon form and identify the rows that are linear combinations of our base
		otherBase = otherBase.toRowEchelonForm();
		
		int complementDimensions = complement.getDimension(); 
		
		FiniteFieldMatrix intersectionBase = otherBase;
				
		outer : for ( int i = 0; i < otherBase.rows ; i++) {
			for ( int j = 0 ; j < complementDimensions; j++) {
				if ( otherBase.entries[otherBase.rows - i - 1][j] != 0) {
					intersectionBase = otherBase.copySubMatrix(otherBase.rows - (i-1) - 1, 0, otherBase.rows - 1, otherBase.columns - 1);
					break outer;
				}
			}
		}		
		
		return spaceBase.getTranspose().multiply(intersectionBase.getTranspose()).getTranspose().copyRowSpace();		
		
	}

	public boolean contains(FiniteFieldVector v) {		
		try {
			decoder.copy().addVector(v);
			return false;
		} catch (LinearDependantException e) {
			return true;
		}
	}
	
	public boolean contains(VectorSpace other) {
		
		if ( other.getDimension() > getDimension()) return false;
		
		CodingVectorDecoder newDecoder = this.decoder.copy(); 
		
		for ( FiniteFieldVector v : other.base) {
			try {
				newDecoder.addVector(v);
				return false;
			} catch (LinearDependantException e) {}	
		}
		
		return true;
		
	}
	
	@Override
	public int hashCode() {
		
		int prime = 31;
		int result = ff.hashCode();
		
		for ( int i = 0 ; i < baseMatrix.rows; i++) {
			for ( int j = 0 ; j < baseMatrix.columns; j++) {
				result += prime * result + baseMatrix.entries[i][j];
			}
		}
		
		return result;
		
	}
	
	@Override
	public boolean equals(Object other) {
		
		if ( other == this) return true;
		
		if ( !( other instanceof VectorSpace) ) return false;
		
		VectorSpace otherSubspace = (VectorSpace) other;
		
		if ( !otherSubspace.ff.equals(ff)) return false;
		
		if ( otherSubspace.getDimension() != getDimension()) return false;
		
		if ( otherSubspace.contains(this)) return true;
		
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
