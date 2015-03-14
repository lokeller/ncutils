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
 * This class is used to find how to linearly combine packets represented 
 * by coding vectors in order to obtain original uncoded packets.
 * 
 * The algorithm used in this class is a variant of the standard Gaussian-
 * Jordan elimination. Each coding vector inserted is simplified with elementary
 * rows operations trying to obtain an elementary vector. The operations
 * performed are kept track in an auxiliary vector (one per added vector).
 * 
 * Internally the class uses O(N²) memory where N is the maximum number of
 * packets that can get combined.
 * 
 * 
 */

public class CodingVectorDecoder {

	/**
	 * the matrix used for gaussian jordan elimination, the first half of the
	 * columns store the matrix being inverted the second half the inverted
	 * matrix.
	 */
	private byte[][] decodeMatrix;

	/** stores the position of the pivot of each row of the decode matrix*/
	private int[] pivotPos;

	/** stores for each column if it is a pivot column for one of the rows of the decode matrix*/
	private boolean[] isPivot;

	/** stores for each column of the decode matrix if it has already been decoded or not */
	private boolean[] decoded;

	/**
	 * stores the number of non-zero lines in the decode matrix ( the number of 
	 * non linearly dependent coding vectors received )
	 */
	private int rowCount = 0;

	/** number of rows of the decode matrix that have been decoded */
	private int decodedCount;

	/** contains the i-th decoded row of the decode matrix*/
	private int[] decodedAddress;

	/**
	 * Construct a new decoder
	 * 
	 * @param generationSize number of coefficients per coding vector
	 */
	public CodingVectorDecoder(int generationLength) {
		decodeMatrix = new byte[generationLength][generationLength * 2];
		pivotPos = new int[generationLength];
		decoded = new boolean[generationLength];
		isPivot = new boolean[generationLength];
		decodedAddress = new int[generationLength];
	}

	/**
	 * Returns the generation size, i.e. the number of coefficients in each coding vector
	 * 
	 * @return the number of coefficients in each coding vector
	 */
	public int getGenerationSize() {
		return decodeMatrix.length;
	}

	/**
	 * 
	 * Adds the coding vector to the internal decoding buffer.
	 * 
	 * @param buffer the buffer containing the coding coefficient vector
	 * @param offest the offset of the first byte of the coding coefficient vector in the buffer
	 * 
	 * @return true if the coding coefficient vector is linearly independent 
	 * 				from the previously added coding vectors
	 */
	public boolean addCodingVector(byte[] buffer, int offset) {

		final int size = decodeMatrix.length;
		final int totalSize = decodeMatrix[0].length;
		
		/* if the matrix is already full rank, this coding vector is for sure linearly dependant*/
		if (rowCount == size) {
			return false;
		}

		/* add the vector at the bottom of the matrix */
		System.arraycopy(buffer, offset, decodeMatrix[rowCount], 0, size);		
		
		/* put zeros on the inverse matrix but on position packet count */
		VectorHelper.setToZero(decodeMatrix[rowCount], size, size);
		decodeMatrix[rowCount][size + rowCount] = 1;

		/* simplify the new coding vector */

		/* make sure that all columns for which we already have a pivot are 0 */
		for (int i = 0; i < rowCount; i++) {

			int m = decodeMatrix[rowCount][pivotPos[i]] & 0XFF;
			
			if (m == 0) continue;

			VectorHelper.multiplyAndAdd(decodeMatrix[rowCount], 0, totalSize, decodeMatrix[i], 0, m);
			
		}

		/* find pivot on the new row */
		int pivot = -1;
		for (int i = 0; i < size; i++) {
			
			if (isPivot[i]) continue;
			
			if (decodeMatrix[rowCount][i] != 0) {
				pivotPos[rowCount] = i;
				isPivot[i] = true;
				pivot = i;
				break;
			}
		}

		/* if the coding vector is not linearly independent stop here */
		if (pivot == -1) {
			return false;
		}

		/* make sure the pivot value is equal to 1*/
		if (decodeMatrix[rowCount][pivot] != 1) {
			int pval = decodeMatrix[rowCount][pivot] & 0xFF;
			VectorHelper.divide(decodeMatrix[rowCount], 0, totalSize, pval);			
		}

		/* make sure that the column of the new pivot is 0 on all the other rows */
		for (int i = 0; i < rowCount; i++) {

			int m = decodeMatrix[i][pivot] & 0XFF;

			if (m == 0)	continue;

			VectorHelper.multiplyAndAdd(decodeMatrix[i], 0, totalSize, decodeMatrix[rowCount], 0, m);
			
		}
		
		/* increase the number of rows of the decode matrix that are used */
		rowCount++;

		/* if there are rows that we finished decoding ( they are elementary vectors) */
		for (int i = 0; i < rowCount; i++) {
			int pos = -1;

			/* skip if the line is marked decoded */
			if (decoded[i])	continue;

			/* check if there is exactly one 1 in the first half of the row */
			for (int j = 0; j < size; j++) {
				if (decodeMatrix[i][j] != 0 && pos != -1) {
					pos = -1;
					break;
				} else if (decodeMatrix[i][j] != 0) {
					pos = j;
				}
			}

			/* the row has been decoded */
			if (pos >= 0) {
				decoded[i] = true;
				decodedAddress[decodedCount] = i;
				decodedCount++;
			}
		}
		
		return true;

	}

	/**
	 * Returns the number of linearly independent coding vectors received up to
	 * now.
	 * 
	 * @return a number between 0 and the generation size
	 */
	public int getSubspaceSize() {
		return rowCount;
	}

	/**
	 * Returns the number of coding vectors that have been fully decoded
	 * 
	 * @return a number between 0 and the generation size
	 */
	public int getDecodingVectorsCount() {
		return decodedCount;
	}

	/**
	 * 
	 * Return a buffer holding the coefficients that describe how to linearly
	 * combine the packets corresponding to the coding vectors successfully added 
	 * to this decoder to obtain an original packet. The index of the original 
	 * packet can be obtained by calling getDecodingVectorPacketId().
	 * 
	 * @param index a number between 0 and getDecodedVectorsCount()
	 * 
	 * @return a buffer holding the coefficients, coefficients start at offset getDecodingVectorOffset() 
	 */
	public byte[] getDecodingVectorBuffer(int index) {
		return decodeMatrix[decodedAddress[index]];
	}

	/**
	 * Returns the index of original packet obtained by linearly combining 
	 * the packets corresponding to the successfully added coding vectors
	 * using the coefficients returned by getDecodingVectorBuffer().
	 * 
	 * @param index
	 * @return
	 */
	public int getDecodingVectoPacketId(int index) {
		return pivotPos[decodedAddress[index]];
	}

	/**
	 * Returns the offset of the first coefficient in the buffer returned by 
	 * getDecodingVectorBuffer()
	 * 
	 * @return the index of the first byte
	 */
	public int getDecodingVectorOffset() {
		return decodeMatrix.length;
	}

}
