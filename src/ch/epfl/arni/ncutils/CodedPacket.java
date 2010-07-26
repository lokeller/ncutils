/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.epfl.arni.ncutils;

import ch.epfl.arni.ncutils.impl.DenseFiniteFieldVector;
import ch.epfl.arni.ncutils.impl.SparseFiniteFieldVector;

import java.util.Vector;

/**
 *
 * @author lokeller
 */
public class CodedPacket {


    private int codingCoefficientsCount;
    private int payloadLength;
    private FiniteFieldVector codingVector;
    private FiniteFieldVector payloadVector;

    public CodedPacket(int blockCount, UncodedPacket packet, FiniteField ff) {

        this(blockCount, packet.getPayload().length, ff);

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

    public CodedPacket(int blockCount, int payloadLen, FiniteField ff) {
        assert(blockCount >= 0);
        this.codingCoefficientsCount = blockCount;
        this.payloadLength = payloadLen;

        codingVector = new SparseFiniteFieldVector(ff);
        if (ff.getCardinality() == 16) {
            payloadVector = new DenseFiniteFieldVector(payloadLen * 2, ff);
        } else if (ff.getCardinality() == 256) {
            payloadVector = new DenseFiniteFieldVector(payloadLen, ff);
        } else {
            payloadVector = new SparseFiniteFieldVector(ff);
        }
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
        assert( index >= 0);
        assert(value < getFiniteField().getCardinality() && value >= 0);
        if ( index < codingCoefficientsCount) {
            codingVector.setCoefficient(index, value);
        } else {
            payloadVector.setCoefficient(index - codingCoefficientsCount, value);
        }
    }

    public int getCoefficient(int index) {

        assert(index >= 0);

        if ( index < codingCoefficientsCount) {
            return codingVector.getCoefficient(index);
        } else {
            return payloadVector.getCoefficient(index - codingCoefficientsCount);
        }
    }

    public void copyTo(FiniteFieldVector c) {

        assert(c.getFiniteField() == getFiniteField());

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
        assert(vector.getFiniteField() == getFiniteField());
        
        FiniteField ff = vector.getFiniteField();

        for ( Integer i : vector.getNonZeroCoefficients()) {
            setCoefficient(i, ff.sum[getCoefficient(i)][vector.getCoefficient(i)]);
        }

    }

    public void scalarMultiply(int c) {
        assert(c < getFiniteField().getCardinality() && c >= 0);
        payloadVector.scalarMultiply(c);
        codingVector.scalarMultiply(c);
    }

    public int getPayloadLength() {
        return payloadLength;
    }

    public int getLength() {
        return codingCoefficientsCount + payloadLength;
    }


}
