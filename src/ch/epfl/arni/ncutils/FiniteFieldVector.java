/*
 * Copyright (c) 2010, EPFL - ARNI
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
package ch.epfl.arni.ncutils;

import java.util.Arrays;

/**
 * Represents a vector of finite field elements.
 * 
 * @author lokeller
 */

public class FiniteFieldVector {

    int[] coordinates;
    private FiniteField ff ;

    /**
     * Constructs a vector
     *
     * @param length the number of coordinates of the vector
     * @param ff the finite field used to define the vector
     */
    public FiniteFieldVector(int length, FiniteField ff) {
        this.ff = ff;
        coordinates = new int[length];
    }

    public FiniteFieldVector(int [] coordinates, FiniteField ff) {
        this.ff = ff;
        this.coordinates = coordinates;
    }
    
    /**
     * Returns the number of coordinates of the vector
     *
     * @return the length of the vector
     */
    public int getLength() {
        return coordinates.length;
    }

    /**
     * Returns the finite field over which the vector is defined
     *
     * @return a finite field
     */
    public FiniteField getFiniteField() {
        return ff;
    }


    /**
     * Set a coordinate of the vector
     *
     * @param index the index of the coordinate (starts at 0)
     * @param value the value of the coordinate, must be an element of
     * the finite field where the vector has been defined
     */
    public void setCoordinate(int index, int value) {
        assert(index >= 0);

        assert(value < ff.getCardinality() && value >= 0);

        coordinates[index] = value;
    }

    /**
     *
     * Returns a coordinate of the vector
     *
     * @param index the index of the coordinate (starts at 0)
     * @return an element of the finite field used to define this vector
     */
    public int getCoordinate(int index) {
        assert(index >= 0);
        
        return coordinates[index];
        
    }

    /**
     * Sets all the coordinates of the vector to zero
     */
    public void setToZero() {
        Arrays.fill(coordinates, 0);
    }

    /**
     * Creates a copy of the vector
     *
     * @return a copy of the vector
     */
    public FiniteFieldVector copy() {

        FiniteFieldVector vector = new FiniteFieldVector(coordinates.length, ff);
        System.arraycopy(coordinates, 0, vector.coordinates, 0, coordinates.length);

        return vector;
    }

    /**
     * Returns the sum of this vector and another vector
     *
     * @param vector the other summand
     * @return the sum of this and vector
     */
    public FiniteFieldVector add(FiniteFieldVector vector) {

        assert(vector.getFiniteField() == ff);
        assert(vector.coordinates.length == coordinates.length);

        FiniteFieldVector out = new FiniteFieldVector(getLength(), ff);

        for ( int i = 0 ; i < coordinates.length ; i++ ) {
            out.coordinates[i] = ff.sum[coordinates[i]][vector.coordinates[i]];
        }

        return out;
    }
    
    public void addInPlace(FiniteFieldVector vector) {

        for ( int i = 0 ; i < coordinates.length ; i++ ) {
            coordinates[i] = ff.sum[coordinates[i]][vector.coordinates[i]];
        }

    }

    /**
     * Returns the scalar multiplication of this vector by a coefficient
     *
     * @param c an element form the field used to define the vecoto
     * @return the scalar multiple of this vector
     */
    public FiniteFieldVector scalarMultiply(int c) {

        assert(c < ff.getCardinality() && c >= 0);

        FiniteFieldVector out = new FiniteFieldVector(getLength(), ff);

        for ( int i = 0 ; i < coordinates.length ; i++ ) {
            out.coordinates[i] = ff.mul[coordinates[i]][c];
        }

        return out;

    }
    
    public void scalarMultiplyInPlace(int c) {
    	for ( int i = 0 ; i < coordinates.length ; i++ ) {
            coordinates[i] = ff.mul[coordinates[i]][c];
        }    
    }
    
    public FiniteFieldVector multiplyAndAdd(int c, FiniteFieldVector other) {
    	
        assert(c < ff.getCardinality() && c >= 0);
        
        FiniteFieldVector out = new FiniteFieldVector(getLength(), ff);

        for ( int i = 0 ; i < coordinates.length ; i++ ) {
            out.coordinates[i] = ff.sum[ff.mul[other.coordinates[i]][c]][coordinates[i]];
        }

        return out;
    	
    }

    public void multiplyAndAddInPlace(int c, FiniteFieldVector other) {

        for ( int i = 0 ; i < coordinates.length ; i++ ) {
            coordinates[i] = ff.sum[ff.mul[other.coordinates[i]][c]][coordinates[i]];
        }
    	
    }
    
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(coordinates);
		result = prime * result + ((ff == null) ? 0 : ff.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof FiniteFieldVector))
			return false;
		FiniteFieldVector other = (FiniteFieldVector) obj;
		if (!Arrays.equals(coordinates, other.coordinates))
			return false;
		if (ff == null) {
			if (other.ff != null)
				return false;
		} else if (!ff.equals(other.ff))
			return false;
		return true;
	}

	@Override
    public String toString() {
            String ret = "";
            for (int c : coordinates) {
                    ret += (ret.length() != 0 ? " " : "") + String.format("%02d ", c); ;
            }
            return ret;
    }


}
