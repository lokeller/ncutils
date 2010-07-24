/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.epfl.arni.ncutils;

/**
 *
 * @author lokeller
 */
public interface Packet extends FiniteFieldVector {

    public FiniteFieldVector getCodingVector();
    public FiniteFieldVector getPayload();

    public int getCodingCoefficientsCount();

}
