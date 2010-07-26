/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.epfl.arni.ncutils.examples;

import ch.epfl.arni.ncutils.ArrayBasedCodingVectorDecoder;
import ch.epfl.arni.ncutils.CodingVectorDecoder;
import ch.epfl.arni.ncutils.FiniteField;
import ch.epfl.arni.ncutils.FiniteFieldVector;
import ch.epfl.arni.ncutils.LinearDependantException;
import ch.epfl.arni.ncutils.SparseFiniteFieldVector;
import ch.epfl.arni.ncutils.VectorDecoder;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lokeller
 */
public class CodingVectorLevelExample {

    public static void main(String[] args) {

        /* intializes a random generator with a fixed seed to obtain
         * deterministic results.
         */
        Random r = new Random(12312);

        /**
         * This is the number of different blocks that are being sent
         */
        int size = 10;

        /*
         * Creates as many linearly independent vectors as possible
         * by drawing random coefficients (this will create errors in
         * general with a small probability, with this random seed it
         * will not)
         *
         */
        FiniteFieldVector[] vectors = new FiniteFieldVector[size];

        for (int i = 0; i < size; i++) {
            vectors[i] = new SparseFiniteFieldVector();

            for (int j = 0; j < size ; j++) {
                vectors[i].setCoefficient(j, r.nextInt(FiniteField.getDefaultFiniteField().getCardinality()));
            }            
            //vectors[i].setCoefficient(size-i-1,1);
            

        }

        /* initialize a decoder */
        CodingVectorDecoder d = new ArrayBasedCodingVectorDecoder(size);
        //Decoder d = new VectorDecoder();

        /* store the start time of the decoding */
        long m = System.currentTimeMillis();

        FiniteFieldVector[] inverse = new FiniteFieldVector[size];

        /* decode one after another all the coding vectors */
        for (int i = 0 ; i < size ; i++) {
            try {

                /* the decode method returns the blocks that can be decoded
                 * from the vector given as a parameter and the vectors that
                 * were previously inserted
                 */

                Map<Integer,FiniteFieldVector> o = d.decode(vectors[i]);

                for (Map.Entry<Integer, FiniteFieldVector> entry : o.entrySet()) {
                    inverse[entry.getKey()] = entry.getValue();
                }

                //System.out.println( "Decoded: " + o);
            } catch (LinearDependantException ex) {

                /* this exception is thrown if the vector being decoded is
                 linearly dependent from what has been sent previously */
                Logger.getLogger(CodingVectorLevelExample.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        FiniteField ff = FiniteField.getDefaultFiniteField();

        for (int i= 0 ; i < size ; i++) {
            for (int j = 0 ; j < size ; j++) {
                System.out.print(" " + inverse[i].getCoefficient(j));
            }

            System.out.println();
        }

        System.out.println("-----------------");

        for (int i= 0 ; i < size ; i++) {
            for (int j = 0 ; j < size ; j++) {

                int sum = 0;
                for (int k = 0 ; k < size ; k++) {
                    sum = ff.sum[sum][ff.mul[vectors[i].getCoefficient(k)][inverse[k].getCoefficient(j)]];
                }

                System.out.print(" " + sum);

                if (i == j && sum != 1) throw new RuntimeException();
                if (i != j && sum != 0) throw new RuntimeException();
            }
             System.out.println();
        }

        System.out.println("Total decoding time :" + (System.currentTimeMillis() - m));
    }
    
}
