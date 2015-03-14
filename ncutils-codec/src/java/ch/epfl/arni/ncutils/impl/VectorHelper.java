/*
 * Copyright (c) 2011, EPFL - ARNI
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

package ch.epfl.arni.ncutils.impl;

/**
 * 
 * Helper class to perform finite field vector operations.
 *
 */

public class VectorHelper {

	/** finite field used to perform the operations */
	private static FiniteField ff = FiniteField.getF256(); 
	
	/**
	 * 
	 * Multiplies vector stored in src by coeff and adds to
	 * vector stored in dest. The result is saved in dest. 
	 * 
	 * @param dest the buffer containing the destination vector.
	 * @param destStart the offset of the first byte of the vector in dest 
	 * @param length the length in bytes of the vectors being added
	 * @param src the buffer containing the vector that will be multiplied and added to dest 
	 * @param srcStart the offset of the first byte of the vector in src
	 * @param coeff the coefficient used to multiply src
	 */
	public static void multiplyAndAdd(byte[] dest, int destStart, int length, byte[] src, int srcStart, int coeff) {
		
		int srcEnd = length+destStart;
		
		int deltaSrc = -destStart + srcStart;
		
		for (int i = destStart; i < srcEnd; i++) {               
			int v2 = dest[i] & 0xFF;                
			int v1 = src[i+deltaSrc] & 0xFF;
            int val = v2 ^ ff.mul[coeff][v1];
            dest[i] = (byte) val;
        }

	}

	/**
	 * Sets a vector to zero.
	 * 
	 * @param buffer a buffer containing the vector
	 * @param bufferStart offset of the first byte of the vector in buffer
	 * @param length length in bytes of the vector
	 */
	public static void setToZero(byte[] buffer, int bufferStart, int length) {
		
		int bufferEnd = bufferStart + length;
		
		for (int i = bufferStart ; i < bufferEnd; i++) {
			buffer[i] = 0;
		}
			
	}

	/**
	 * Divides all entries of a vector by coeff
	 * 
	 * @param buffer a buffer containing the vector
	 * @param bufferStart offset of the first byte of the vector in buffer
	 * @param length length in bytes of the vector
	 * @param coeff the coefficient used to divide the vector
	 */
	public static void divide(byte[] buffer, int bufferStart, int length, int coeff) {
		int bufferEnd = bufferStart + length;
		
		for (int i = bufferStart ; i < bufferEnd; i++) {
			int v2 = buffer[i] & 0xFF;                
			buffer[i] = (byte) ff.div[v2][coeff];
		}
	}
	
}
