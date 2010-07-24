package ch.epfl.arni.ncutils;

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

public class VectorDecoder implements Decoder {
		
	private HashSet<Integer> decodedBlocks = new HashSet<Integer>();
		
	private Vector<Vector<Integer>> decodeMatrix = new Vector<Vector<Integer>>();
	private Vector<Integer> blocks = new Vector<Integer>();
	private int packetCount = 0;

        private FiniteField ff = FiniteField.getDefaultFiniteField();

        public int decodedBlockCount() {
            return decodedBlocks.size();
        }

	public HashSet<Integer> decode(FiniteFieldVector p) throws LinearDependantException {
                
                
                int [][] mul = ff.mul;
                int [][] sub = ff.sub;
                int [][] div = ff.div;


		/* create a copy of the packets before processing it */

                FiniteFieldVector c = new SparseFiniteFieldVector();
                p.copyTo(c);
                
                /* set to zero the coefficients of all the blocks that have
                 * been already decoded
                 */

                
                for (Integer block : decodedBlocks) {
                    c.setCoefficient(block, 0);
                }

		
		if (c.getHammingWeight() == 0) throw new LinearDependantException();
		
		/* add the column for the new received Integers */		
		int count = 0;
		for (Integer b : c.getNonZeroCoefficients()) {
			if (!blocks.contains(b)) {
				blocks.add(b);
				count++;
			}
		}
		
		if (count > 0) {
			for (int i = 0 ; i < count ; i++) {
				Vector<Integer> v = new Vector<Integer>(packetCount);
				for (int j = 0 ; j < packetCount ; j++) {
					v.add(0);					
				}
				decodeMatrix.add(v);
			}
		}
		
		
		/* add the new packet */		
		for (int i = 0 ; i < blocks.size() ; i++) {
			Integer b = blocks.get(i);
                        decodeMatrix.get(i).add(c.getCoefficient(b));
		}
		
		packetCount++;
		
		/* simplify the new packet */
		
		/* zeros before */
		for (int i = 0 ; i < packetCount - 1 ; i++)  {
			
			int m = decodeMatrix.get(i).get(packetCount - 1 );
			
			for (int j = 0 ; j < blocks.size() ; j++) {
				int val = decodeMatrix.get(j).get(packetCount - 1 );
				int val2 = decodeMatrix.get(j).get(i);
				
				decodeMatrix.get(j).set(packetCount - 1 , sub[val][mul[val2][m]]);
				
			}
			
		}
		
		/* find pivot on the line */
		
		int pivot = -1;
		for (int i = packetCount - 1 ; i < blocks.size() ; i++) {
			if (decodeMatrix.get(i).get(packetCount - 1 ) != 0) {
				pivot = i;
				break;
			}
		}
		
		/* if the packet is not li stop here */
		
		if (pivot == -1 ) {
			
			for (Vector<Integer> col : decodeMatrix) {
				col.removeElementAt(packetCount - 1);
			}
			packetCount--;
			
                        throw new LinearDependantException();
		}
		
		/* divide the line */		
		
		int pval = decodeMatrix.get(pivot).get(packetCount - 1 );
		for (int j = 0 ; j < blocks.size() ; j++) {
			int val = decodeMatrix.get(j).get(packetCount - 1 );			
			decodeMatrix.get(j).set(packetCount - 1 , div[val][pval]);
		}
		
		/* zero the column above the pivot */
		
		for ( int i = 0 ; i < packetCount - 1 ; i++ ) {

			int m = decodeMatrix.get(pivot).get(i);
			
			for (int j = 0 ; j < blocks.size() ; j++) {
				int val2 = decodeMatrix.get(j).get(packetCount - 1 );
				int val = decodeMatrix.get(j).get(i);
				
				decodeMatrix.get(j).set(i , sub[val][mul[val2][m]]);
				
			}			
		}
		
		/* move the pivot column to the right position */
		if (pivot != packetCount - 1) {
			Vector<Integer> temp = decodeMatrix.remove(pivot);
			Integer tempB = blocks.remove(pivot);
			
			decodeMatrix.insertElementAt(temp, packetCount - 1);
			blocks.insertElementAt(tempB, packetCount - 1);
		}		
		
		/* look for decodable blocks */
		
		HashSet<Integer> willDecode = new HashSet<Integer>();
		Vector<Integer> elLines = new Vector<Integer>();
		outer: for (int j = 0 ; j < packetCount; j++) {
			boolean found = false;
			int pos = -1;
			for (int k = 0 ; k < blocks.size() ; k++) {
				if (decodeMatrix.get(k).get(j) != 0) {
					if (found) {
						continue outer;
					} else {

						found = true;
						pos = k;
					}
				}
			}
			if (pos != -1) {
				willDecode.add(blocks.elementAt(pos));
				elLines.add(j);
			}
		}
		
		
		/* remove the columns corresponing to the decodable blocks */
		for (Integer b : willDecode) {
			decodeMatrix.removeElementAt(blocks.indexOf(b));			
			blocks.remove(b);
		}
		
		/* Remove the empty lines */
		for (int i = elLines.size() - 1  ; i >= 0 ; i-- ) {
			for (Vector<Integer> col : decodeMatrix) {
				col.removeElementAt(elLines.get(i));
			}
			packetCount--;		
		}
		 
		decodedBlocks.addAll(willDecode);
		
		return willDecode;			
		
	}
		
}
