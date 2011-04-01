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
package ch.epfl.arni.ncutils.f256;

import java.util.Arrays;

import ch.epfl.arni.ncutils.FiniteField;

/**
 * Represents a vector of elements of F_{2^8}.
 * 
 *
 * @author lokeller
 */

public class F256Vector {

	private static FiniteField ff = F256.getF256();
	
    byte[] coordinates;
    int offset;
    int len;

    /**
     * Constructs a vector
     *
     * @param len the number of coordinates of the vector
     * @param ff the finite field used to define the vector
     */
    public F256Vector(int len) {        
        coordinates = new byte[len];
        offset = 0;
        this.len = len;
    }

    public F256Vector(byte [] coordinates, int offset, int len) {        
        this.coordinates = coordinates;
        this.offset = offset;
        this.len = len;
    }
    
    /**
     * Returns the number of coordinates of the vector
     *
     * @return the length of the vector
     */
    public int getLength() {
        return len;
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
    public void setCoordinate(int index, byte value) {
        coordinates[index+offset] = value;
    }

    /**
     *
     * Returns a coordinate of the vector
     *
     * @param index the index of the coordinate (starts at 0)
     * @return an element of the finite field used to define this vector
     */
    public byte getCoordinate(int index) {                
        return coordinates[index+offset];        
    }

    /**
     * Sets all the coordinates of the vector to zero
     */
    public void setToZero() {
        Arrays.fill(coordinates, offset, offset+len, (byte) 0);
    }

    /**
     * Creates a copy of the vector
     *
     * @return a copy of the vector
     */
    public F256Vector copy() {

        F256Vector vector = new F256Vector(len);
        System.arraycopy(coordinates, offset, vector.coordinates, 0, len);

        return vector;
    }

    /**
     * Returns the sum of this vector and another vector
     *
     * @param vector the other summand
     * @return the sum of this and vector
     */
    public F256Vector add(F256Vector vector) {

        F256Vector out = new F256Vector(len);

        for ( int i = 0 ; i < len; i++ ) {
            out.coordinates[i] = (byte) ( coordinates[i+offset] ^ vector.coordinates[i+vector.offset]);
        }

        return out;
    }
    
    public void addInPlace(F256Vector vector) {

        for ( int i = 0 ; i < len ; i++ ) {
            coordinates[i+offset] = (byte) ( coordinates[i+offset] ^ vector.coordinates[i+vector.offset] );
        }

    }

    /**
     * Returns the scalar multiplication of this vector by a coefficient
     *
     * @param c an element form the field used to define the vecoto
     * @return the scalar multiple of this vector
     */
    public F256Vector scalarMultiply(int c) {

        F256Vector out = new F256Vector(len);

        for ( int i = 0 ; i < len; i++ ) {
            out.coordinates[i] = (byte) ff.mul[((int) coordinates[i+offset]) & 0xFF][c];
        }

        return out;

    }
    
    public void scalarMultiplyInPlace(int c) {
    	for ( int i = 0 ; i < len ; i++ ) {
            coordinates[i+offset] = (byte) ff.mul[((int) coordinates[i+offset]) & 0xFF][c];
        }    
    }
    
    public F256Vector multiplyAndAdd(int c, F256Vector other) {
    	        
        F256Vector out = new F256Vector(len);

        for ( int i = 0 ; i < len ; i++ ) {
            out.coordinates[i] = (byte) ( ff.mul[((int) other.coordinates[i+other.offset]) & 0xFF][c] ^ (((int) coordinates[i+offset]) & 0xFF));
        }

        return out;
    	
    }

    public void multiplyAndAddInPlace(int c, F256Vector other) {

        for ( int i = 0 ; i < len ; i++ ) {
            coordinates[i+offset] = (byte) ( ff.mul[((int)other.coordinates[i+other.offset]) & 0xFF][c] ^ (((int) coordinates[i+offset]) & 0xFF));
        }
    	
    }
       
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(coordinates);
		result = prime * result + len;
		result = prime * result + offset;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof F256Vector))
			return false;
		F256Vector other = (F256Vector) obj;
		if (!Arrays.equals(coordinates, other.coordinates))
			return false;
		if (len != other.len)
			return false;
		if (offset != other.offset)
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
