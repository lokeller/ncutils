/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.epfl.arni.ncutils;

import java.util.HashSet;

/**
 *
 * @author lokeller
 */
public interface Decoder {

    HashSet<Integer> decode(FiniteFieldVector p) throws LinearDependantException;

    int decodedBlockCount();

}
