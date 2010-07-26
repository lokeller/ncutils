package ch.epfl.arni.ncutils;

import java.util.HashMap;
import java.util.Vector;
import java.util.HashSet;
import java.util.Map;


/**
 * 
 * This class is implemented with Vectors, implementing it with LinkedList is slower 
 * ( 2.5x slower) 
 * 
 * @author lokeller
 *
 */

public class ArrayDecoder implements Decoder {
		
	private int[][] decodeMatrix;
        private int[] colToBlock;
        private int[] blockToCol;
        private int[] pivotPos;
        private boolean[] isPivot;
        private boolean[] decoded;
        private int usedCols = 0;
        private int decCount = 0;

        public ArrayDecoder(int size) {
            decodeMatrix = new int[size][size * 2];
            colToBlock = new int[size];
            blockToCol = new int[size];
            pivotPos = new int[size];
            decoded = new boolean[size];
            isPivot = new boolean[size];
            for (int i = 0; i < size; i++) blockToCol[i] = -1;

        }

	private int packetCount = 0;

        private FiniteField ff = FiniteField.getDefaultFiniteField();

        public int decodedBlockCount() {
            return decCount;
        }

	public Map<Integer,FiniteFieldVector> decode(FiniteFieldVector p) throws LinearDependantException {
                                		
                int [][] mul = ff.mul;
                int [][] sub = ff.sub;
                int [][] div = ff.div;

                int size = decodeMatrix.length;

		boolean linearlyDependant = true;

		/* add the column for the new received Integers */
                for ( Integer b : p.getNonZeroCoefficients()) {

                    int tb = blockToCol[b];

                    if ( tb == -1) {
                        blockToCol[b] = usedCols;
                        tb = usedCols;
                        colToBlock[usedCols] = b;
                        usedCols++;                        
                    }

                    decodeMatrix[packetCount][tb] = p.getCoefficient(b);

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

                        FiniteFieldVector vector = new SparseFiniteFieldVector();

                        for ( int j = size ; j < size + usedCols ; j++) {
                            vector.setCoefficient(j-size, decodeMatrix[i][j]);
                        }

                        willDecode.put(colToBlock[pos], vector);
                        decCount++;
                    }
                }

		return willDecode;			
		
	}
		
}
