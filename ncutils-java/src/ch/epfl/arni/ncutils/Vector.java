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

/**
 * Represents a vector of finite field elements.
 * 
 * @author lokeller
 */

public class Vector {

    int[] coordinates;
    private FiniteField ff ;

    /**
     * 
     * Creates a vector that uses the specified array as storage of its coordinates,
     * the coordinates are not cleared.
     * 
     * @param coordinates an array that will be used as storage for the vector contents
     * @param ff the finite field to which the coordinates of this vector belong
     * 
     * @return a vector that is using the specified array to store its coordinates
     */
    public static Vector wrap(int [] coordinates, FiniteField ff) {
    	return new Vector(coordinates, ff);
    }
    
    /**
     * Constructs a vector
     *
     * @param length the number of coordinates of the vector
     * @param ff the finite field used to define the vector
     */
    public Vector(int length, FiniteField ff) {
        this.ff = ff;
        coordinates = new int[length];
    }    
    
    Vector(int [] coordinates, FiniteField ff) {
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
    public Vector copy() {

        Vector vector = new Vector(coordinates.length, ff);
        System.arraycopy(coordinates, 0, vector.coordinates, 0, coordinates.length);

        return vector;
    }

    /**
     * Returns the sum of this vector and another vector
     *
     * @param vector the other summand
     * @return the sum of this and vector
     */
    public Vector add(Vector vector) {
        Vector out = new Vector(getLength(), ff);

        for ( int i = 0 ; i < coordinates.length ; i++ ) {
            out.coordinates[i] = ff.sum[coordinates[i]][vector.coordinates[i]];
        }

        return out;
    }
    
    /**
     * Adds to each of the coordinates of this vector the corresponding coordinate in
     * the other vector
     * 
     * @param vector a second vector to be added
     */
    public void addInPlace(Vector vector) {

        for ( int i = 0 ; i < coordinates.length ; i++ ) {
            coordinates[i] = ff.sum[coordinates[i]][vector.coordinates[i]];
        }

    }

    /**
     * Returns the scalar multiplication of this vector by a coefficient
     *
     * @param c an element form the field used to define the vector
     * @return the scalar multiple of this vector
     */
    public Vector scalarMultiply(int c) {

        Vector out = new Vector(getLength(), ff);

        for ( int i = 0 ; i < coordinates.length ; i++ ) {
            out.coordinates[i] = ff.mul[coordinates[i]][c];
        }

        return out;

    }
    
    /**
     * Multiplies each of the coordinates of this vector by a given
     * constant.
     * 
     * @param c a constant that is used to multiply the vector coordinates
     */
    public void scalarMultiplyInPlace(int c) {
    	for ( int i = 0 ; i < coordinates.length ; i++ ) {
            coordinates[i] = ff.mul[coordinates[i]][c];
        }    
    }
    
    /**
     * 
     * Returns a copy of this vector where to each coordinate of this vector 
     * the corresponding coordinate in another vector multiplied by a give 
     * constant has been added.
     * 
     * 
     * @param c a constant value
     * @param other a vector
     * 
     * @return a copy of this vector to which a scalar multiple of the other vector
     * has been added
     */
    public Vector multiplyAndAdd(int c, Vector other) {
    	
        Vector out = new Vector(getLength(), ff);

        for ( int i = 0 ; i < coordinates.length ; i++ ) {
            out.coordinates[i] = ff.sum[ff.mul[other.coordinates[i]][c]][coordinates[i]];
        }

        return out;
    	
    }

    /**
     * Adds to each coordinate of this vector a the corresponding coordinate
     * of antother vector multiplied by a constant 
     * 
     * @param c a constant
     * @param other the vector to be multiplied and added
     */
    public void multiplyAndAddInPlace(int c, Vector other) {

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
		if (!(obj instanceof Vector))
			return false;
		Vector other = (Vector) obj;
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
