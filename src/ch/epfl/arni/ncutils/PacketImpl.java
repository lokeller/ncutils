/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.epfl.arni.ncutils;

import java.util.Vector;

/**
 *
 * @author lokeller
 */
public class PacketImpl implements Packet {

    private int codingCoefficientsCount;
    private FiniteFieldVector codingVector;
    private FiniteFieldVector payloadVector;


    public FiniteFieldVector getCodingVector() {
       return codingVector;
    }

    public FiniteFieldVector getPayload() {
        return payloadVector;
    }

    public FiniteField getFiniteField() {
        return codingVector.getFiniteField();
    }


    public void setCoefficient(int index, int value) {
        if ( index < codingCoefficientsCount) {
            codingVector.setCoefficient(index, value);
        } else {
            payloadVector.setCoefficient(index - codingCoefficientsCount, value);
        }
    }

    public int getCoefficient(int index) {
        if ( index < codingCoefficientsCount) {
            return codingVector.getCoefficient(index);
        } else {
            return payloadVector.getCoefficient(index - codingCoefficientsCount);
        }
    }

    public void copyTo(FiniteFieldVector c) {
        c.setToZero();

        for ( Integer i : codingVector.getNonZeroCoefficients()) {
            c.setCoefficient(i, codingVector.getCoefficient(i));
        }

        for ( Integer i : payloadVector.getNonZeroCoefficients()) {
            c.setCoefficient(i, payloadVector.getCoefficient(i));
        }
    }

    public void setToZero() {
        codingVector.setToZero();
        payloadVector.setToZero();
    }

    public int getHammingWeight() {
        return payloadVector.getHammingWeight() + codingVector.getHammingWeight();
    }

    public Iterable<Integer> getNonZeroCoefficients() {
        Vector<Integer> out = new Vector<Integer>();

        for( Integer i : codingVector.getNonZeroCoefficients()) {
            out.add(i);
        }

        for( Integer i : payloadVector.getNonZeroCoefficients()) {
            out.add(i);
        }

        return out;
    }

    public int getCodingCoefficientsCount() {
        return codingCoefficientsCount;
    }


}
