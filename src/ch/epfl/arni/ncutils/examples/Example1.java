/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.epfl.arni.ncutils.examples;

import ch.epfl.arni.ncutils.ArrayDecoder;
import ch.epfl.arni.ncutils.Decoder;
import ch.epfl.arni.ncutils.FiniteField;
import ch.epfl.arni.ncutils.FiniteFieldVector;
import ch.epfl.arni.ncutils.LinearDependantException;
import ch.epfl.arni.ncutils.SparseFiniteFieldVector;
import ch.epfl.arni.ncutils.VectorDecoder;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lokeller
 */
public class Example1 {

    public static void main(String[] args) {

        /* intializes a random generator with a fixed seed to obtain
         * deterministic results.
         */
        Random r = new Random(12312);

        /**
         * This is the number of different blocks that are being sent
         */
        int size = 500;

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

        }

        /* initialize a decoder */
        Decoder d = new ArrayDecoder(size);
        //Decoder d = new VectorDecoder();

        /* store the start time of the decoding */
        long m = System.currentTimeMillis();

        /* decode one after another all the coding vectors */
        for (int i = 0 ; i < size ; i++) {
            try {

                /* the decode method returns the blocks that can be decoded
                 * from the vector given as a parameter and the vectors that
                 * were previously inserted
                 */

                Object o = d.decode(vectors[i]);
                //System.out.println( "Decoded: " + o);
            } catch (LinearDependantException ex) {

                /* this exception is thrown if the vector being decoded is
                 linearly dependent from what has been sent previously */
                Logger.getLogger(Example1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        System.out.println("Total decoding time :" + (System.currentTimeMillis() - m));
    }
    
}
