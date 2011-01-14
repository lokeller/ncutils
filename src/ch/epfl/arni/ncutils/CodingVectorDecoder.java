/*
 * Copyright (c) 2010 - 2011, EPFL - ARNI
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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * This class is used to find how to linearly combine coding vectors in order
 * to obtain elementary vectors. Using this knowledge it is possible to
 * decoded coded packets.
 *
 * The algorithm used in this class is a modification of the standard Gaussian-
 * Jordan elimination. Each coding vector inserted is simplified with elementary
 * rows operations trying to obtain an elementary vector. The operations
 * performed are kept track in an auxiliary vector (one per added vector).
 *
 * Internally the class uses O(N²) memory where N is the maximum number of 
 * packets that can get combined.
 *
 * @author lokeller
 */


public class CodingVectorDecoder {

        /* the matrix used for gaussian jordan elimination, the first half of the
         * columns store the matrix being inverted the second half the inverted
         * matrix.
         */
	private int[][] decodeMatrix;

        /** stores the position of the pivot of each line */
        private int[] pivotPos;

        /** stores for each column if it is a pivot column for a line or not*/
        private boolean[] isPivot;

        /** stores for each column if it has already been decoded or not*/
        private boolean[] decoded;

        /** stores the number of non-zero lines in the decode matrix ( the number
         * of packets that have been received */
        private int packetCount = 0;

        /** the finite field that is used in this decoder */
        private FiniteField ff;

        /**
         * Construct a new decoder
         *
         * @param maxPackets the length of the vectors have to be decoded
         * @param ff the finite field used in the decoder
         */
        public CodingVectorDecoder(int maxPackets, FiniteField ff) {
            decodeMatrix = new int[maxPackets][maxPackets * 2];
            pivotPos = new int[maxPackets];
            decoded = new boolean[maxPackets];
            isPivot = new boolean[maxPackets];
            this.ff = ff;

        }

        /**
         * Returns the maximum number of packets that can be combined (i.e. the
         * length of the coding vectors being decoded)
         *
         * @return the number of packets supported
         */
        public int getMaxPackets() {
            return decodeMatrix.length;
        }

        /**
         *
         * Adds the coding vector to the internal decoding buffer and returns
         * for new each elementary vector that can be constructed by linearly
         * combining the vectors in the coding buffer a vector with the
         * coefficients necessary to create it.
         *
         * @param v a coding vector of length compatible with the decoder
         * @return a map that associates an id of an uncoded packet with a vector
         * containing the coefficients that must be used to recover its payload
         * 
         * @throws LinearDependantException the vector being added is linearly dependant
         * from the previously decoded vectors.
         */
	public Map<Integer,FiniteFieldVector> addVector(FiniteFieldVector v) throws LinearDependantException {
        
		
                /* if the matrix is already full rank don't add this vector */
                if ( packetCount == decodeMatrix.length) {
                        throw new LinearDependantException();
                }
		
                final int [][] mul = ff.mul;
                final int [][] sub = ff.sub;
                final int [][] div = ff.div;

                final int size = decodeMatrix.length;
                final  int totalSize = decodeMatrix[0].length;

                /* add the received packet at the bottom of the matrix */
                for ( int i = 0 ; i < v.getLength() ; i++) {
                    decodeMatrix[packetCount][i] = v.getCoordinate(i);
                    decodeMatrix[packetCount][i+size] = 0 ;                    
                }

                /* put zeros on the inverse matrix but on position packet count*/
                for ( int i = size ; i < totalSize ; i++) {
                    decodeMatrix[packetCount][i] = 0 ;
                }
                
                decodeMatrix[packetCount][size+packetCount] = 1;


		/* simplify the new packet */
		
		/* zeros before */
		for (int i = 0 ; i < packetCount ; i++)  {
                    
                        int m = decodeMatrix[packetCount][pivotPos[i]];

                        if (m == 0) continue;

			for (int j = 0 ; j < totalSize ; j++) {
                                int val = decodeMatrix[packetCount][j];
				int val2 = decodeMatrix[i][j];
                                decodeMatrix[packetCount][j] = sub[val][mul[val2][m]];
			}
			
		}

		/* find pivot on the line */		
		int pivot = -1;
		for (int i = 0 ; i < size ; i++) {
                    if (isPivot[i]) continue;                    
                    if (decodeMatrix[packetCount][i] != 0) {
                            pivotPos[packetCount] = i;
                            isPivot[i] = true;
                            pivot = i;
                            break;
                    }
		}
		
		/* if the packet is not li stop here */
		
		if (pivot == -1 ) {                        

                        throw new LinearDependantException();
		}                
                
		/* divide the line */		

                if ( decodeMatrix[packetCount][pivot] != 1 ) {
                    int pval = decodeMatrix[packetCount][pivot];

                    for (int j = 0 ; j < totalSize ; j++) {
                            int val = decodeMatrix[packetCount][j];
                            decodeMatrix[packetCount][j] = div[val][pval];
                    }

                }
		
		/* zero the column above the pivot */		
		for ( int i = 0 ; i < packetCount ; i++ ) {

			int m = decodeMatrix[i][pivot];

                        if (m == 0) continue;

			for (int j = 0 ; j < totalSize ; j++) {
                                
                                int val2 = decodeMatrix[packetCount][j];
				int val = decodeMatrix[i][j];
				
				decodeMatrix[i][j] = sub[val][mul[val2][m]];
				
			}

		}

                packetCount++;
                
		/* look for decodable blocks */
		
		HashMap<Integer,FiniteFieldVector> willDecode =
                        new HashMap<Integer, FiniteFieldVector>();

                for ( int i = 0; i < packetCount ; i++) {
                    int pos = -1;

                    /* skip if the line is marked decoded */
                    if (decoded[i]) continue;

                    for ( int j = 0 ; j < size ; j++) {

                        if (decodeMatrix[i][j] != 0 && pos != -1) {
                            pos = -1;
                            break;
                        } else if (decodeMatrix[i][j] != 0) pos = j;
                    }
                    
                    if ( pos >= 0) {
                        decoded[i] = true;


                        /* build the vector that explains how to obtain the block */
                        FiniteFieldVector vector = new FiniteFieldVector(decodeMatrix.length, ff);
                        for ( int j = size ; j < size + size ; j++) {
                            vector.setCoordinate(j-size, decodeMatrix[i][j]);
                        }

                        willDecode.put(pos, vector);

                    }
                }

		return willDecode;			
		
	}

    /**
     * Returns the number of linearly independent coding vectors received
     * up to now.
     * 
     * @return a number between 0 and getMaxPackets()
     */
	public int getSubspaceSize() {
		return packetCount;
	}
	
		
}
