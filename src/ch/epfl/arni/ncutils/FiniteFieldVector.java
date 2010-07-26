/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.epfl.arni.ncutils;

import java.util.Iterator;

/**
 *
 * @author lokeller
 */
public interface FiniteFieldVector {

    public FiniteField getFiniteField();
    
    public void setCoefficient(int index, int value);
    public int getCoefficient(int index);
    public void copyTo(FiniteFieldVector c);
    public void setToZero();

    public void add(FiniteFieldVector vector);
    public void scalarMultiply(int c);

    public int getHammingWeight();

    public Iterable<Integer> getNonZeroCoefficients();

}
