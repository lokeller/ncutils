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
public class EncodedPacketImpl implements EncodedPacket {


    private int codingCoefficientsCount;
    private FiniteFieldVector codingVector;
    private FiniteFieldVector payloadVector;

    public EncodedPacketImpl(int blockCount, UncodedPacket packet) {

        this(blockCount);

        codingVector.setCoefficient(packet.getId(), 1);

        if ( payloadVector.getFiniteField().getCardinality() != 256 &&
               payloadVector.getFiniteField().getCardinality() != 16 ) {
            throw new RuntimeException("The only field size supported is 2^8 and 2^4 ( Q was "
                                            +payloadVector.getFiniteField().getCardinality() );
        }

        if (payloadVector.getFiniteField().getCardinality() == 256) {
            for (int i = 0 ; i < packet.getPayload().length; i++) {
                this.payloadVector.setCoefficient(i, 0xFF & ((int) packet.getPayload()[i]));
            }
        } else if (payloadVector.getFiniteField().getCardinality() == 16) {
            for (int i = 0 ; i < packet.getPayload().length; i++) {
                this.payloadVector.setCoefficient(2*i, 0x0F & ((int) packet.getPayload()[i]));
                this.payloadVector.setCoefficient(2*i+1, (0xF0 & ((int) packet.getPayload()[i])) >> 4);
            }
        }


    }

    public EncodedPacketImpl(int blockCount) {
        this.codingCoefficientsCount = blockCount;
        codingVector = new SparseFiniteFieldVector();
        payloadVector = new SparseFiniteFieldVector();

    }

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

        for ( Integer i : getNonZeroCoefficients()) {
            c.setCoefficient(i, getCoefficient(i));
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
            out.add(i+codingCoefficientsCount);
        }

        return out;
    }

    public int getCodingCoefficientsCount() {
        return codingCoefficientsCount;
    }

    public void add(FiniteFieldVector vector) {

        FiniteField ff = vector.getFiniteField();

        for ( Integer i : vector.getNonZeroCoefficients()) {
            setCoefficient(i, ff.sum[getCoefficient(i)][vector.getCoefficient(i)]);
        }

    }

    public void scalarMultiply(int c) {

        payloadVector.scalarMultiply(c);
        codingVector.scalarMultiply(c);
    }


}
