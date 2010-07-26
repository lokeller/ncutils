/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.epfl.arni.ncutils;

import java.util.Map;

/**
 *
 * @author lokeller
 */
public interface CodingVectorDecoder {

    Map<Integer, FiniteFieldVector> decode(FiniteFieldVector p) throws LinearDependantException;

    int decodedBlockCount();

}
