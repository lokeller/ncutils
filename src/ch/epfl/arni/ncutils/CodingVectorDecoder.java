package ch.epfl.arni.ncutils;

import java.util.HashMap;
import java.util.Map;

/**
 * This class implements matrix inversion over finite fields. The algorithm used
 * is a modification of the standard Gaussian-Jordan elimination. The class
 * accepts the matrix that has to be inverted line by line. As soon some lines
 * of the inverted matrix are available they are outputted.
 *
 * Internally the class uses stores two matrices. It uses therefore O(N²) memory.
 *
 * @author lokeller
 */


public class CodingVectorDecoder {

        /* the matrix used for gaussian jordan elimination, the first half of the
         * columns store the matrix being inverted the second half the inverted
         * matrix.
         */
	private int[][] decodeMatrix;
        
        private int[] colToBlock;
        private int[] blockToCol;
        private int[] pivotPos;
        private boolean[] isPivot;
        private boolean[] decoded;
        private int usedCols = 0;
        private int packetCount = 0;
        private FiniteField ff;

        public CodingVectorDecoder(int maxPackets, FiniteField ff) {
            decodeMatrix = new int[maxPackets][maxPackets * 2];
            colToBlock = new int[maxPackets];
            blockToCol = new int[maxPackets];
            pivotPos = new int[maxPackets];
            decoded = new boolean[maxPackets];
            isPivot = new boolean[maxPackets];
            for (int i = 0; i < maxPackets; i++) blockToCol[i] = -1;
            this.ff = ff;

        }

        public int getMaxPackets() {
            return decodeMatrix.length;
        }

	public Map<Integer,FiniteFieldVector> decode(FiniteFieldVector v) throws LinearDependantException {
                                		
                int [][] mul = ff.mul;
                int [][] sub = ff.sub;
                int [][] div = ff.div;

                int size = decodeMatrix.length;

		boolean linearlyDependant = true;

		/* add the column for the new received Integers */
                for ( int i = 0 ; i < v.getLength() ; i++) {

                    int val = v.getCoordinate(i);

                    if ( val == 0 ) continue;

                    int tb = blockToCol[i];

                    if ( tb == -1) {
                        blockToCol[i] = usedCols;
                        tb = usedCols;
                        colToBlock[usedCols] = i;
                        usedCols++;                        
                    }

                    decodeMatrix[packetCount][tb] = val;

                    linearlyDependant = false;

                }
                
                if (linearlyDependant) {
                    throw new LinearDependantException();
                }

		/* simplify the new packet */
		
		/* zeros before */
		for (int i = 0 ; i < packetCount ; i++)  {
                    
                        int m = decodeMatrix[packetCount][pivotPos[i]];

                        if (m == 0) continue;

                        /* entries in the first half */
			for (int j = 0 ; j < usedCols ; j++) {                                
                                int val = decodeMatrix[packetCount][j];
				int val2 = decodeMatrix[i][j];
                                decodeMatrix[packetCount][j] = sub[val][mul[val2][m]];
			}

                        /* entries in the second half */
                        for (int j = size ; j < usedCols + size; j++) {
                                int val = decodeMatrix[packetCount][j];
				int val2 = decodeMatrix[i][j];
                                decodeMatrix[packetCount][j] = sub[val][mul[val2][m]];
			}
			
		}

		/* find pivot on the line */
		
		int pivot = -1;
		for (int i = 0 ; i < usedCols ; i++) {
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
                    
                        /* cleanup the second half of the decode matrix */
                        for (int j = size ; j < usedCols + size; j++) {
                            decodeMatrix[packetCount][j] = 0;
                        }
                        throw new LinearDependantException();
		}                

                /* add the 1 in the inverse matrix */
                decodeMatrix[packetCount][size+packetCount] = 1;

		/* divide the line */		

                if ( decodeMatrix[packetCount][pivot] != 1 ) {
                    int pval = decodeMatrix[packetCount][pivot];

                    /* first half */
                    for (int j = 0 ; j < usedCols ; j++) {
                            int val = decodeMatrix[packetCount][j];
                            decodeMatrix[packetCount][j] = div[val][pval];
                    }

                    /* second half */
                    for (int j = size ; j < usedCols + size; j++) {
                            int val = decodeMatrix[packetCount][j];
                            decodeMatrix[packetCount][j] = div[val][pval];
                    }
                }
		
		/* zero the column above the pivot */		
		for ( int i = 0 ; i < packetCount ; i++ ) {

			int m = decodeMatrix[i][pivot];

                        if (m == 0) continue;

			for (int j = 0 ; j < usedCols ; j++) {
                                
                                int val2 = decodeMatrix[packetCount][j];
				int val = decodeMatrix[i][j];
				
				decodeMatrix[i][j] = sub[val][mul[val2][m]];
				
			}

                        for (int j = size ; j < size + usedCols ; j++) {
                                
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

                    for ( int j = 0 ; j < usedCols ; j++) {

                        if (decodeMatrix[i][j] != 0 && pos != -1) {
                            pos = -1;
                            break;
                        } else if (decodeMatrix[i][j] != 0) pos = j;
                    }
                    
                    if ( pos >= 0) {
                        decoded[i] = true;


                        /* build the vector that explains how to obtain the block */
                        FiniteFieldVector vector = new FiniteFieldVector(decodeMatrix.length, ff);
                        for ( int j = size ; j < size + usedCols ; j++) {
                            vector.setCoordinate(j-size, decodeMatrix[i][j]);
                        }

                        willDecode.put(colToBlock[pos], vector);

                    }
                }

		return willDecode;			
		
	}

    
		
}
