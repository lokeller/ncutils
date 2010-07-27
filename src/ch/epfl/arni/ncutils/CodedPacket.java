/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.epfl.arni.ncutils;

/**
 *
 * @author lokeller
 */
public class CodedPacket {
    
    private FiniteFieldVector codingVector;
    private FiniteFieldVector payloadVector;

    public CodedPacket( UncodedPacket packet, int maxPackets, FiniteField ff) {

        this( new FiniteFieldVector(maxPackets, ff), ff.byteToVector(packet.getPayload()));

        codingVector.setCoefficient(packet.getId(), 1);
    }

    public CodedPacket(int maxPackets, int payloadByteLen, FiniteField ff) {

        this( new FiniteFieldVector(maxPackets, ff),
                new FiniteFieldVector(ff.coefficientCount(payloadByteLen), ff));
        
    }

    private CodedPacket(FiniteFieldVector codingVector, FiniteFieldVector payloadVector) {
        this.codingVector = codingVector;
        this.payloadVector = payloadVector;
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
        if ( index < codingVector.getLength()) {
            codingVector.setCoefficient(index, value);
        } else {
            payloadVector.setCoefficient(index - codingVector.getLength(), value);
        }
    }

    public int getCoefficient(int index) {

        assert(index >= 0);

        if ( index < codingVector.getLength()) {
            return codingVector.getCoefficient(index);
        } else {
            return payloadVector.getCoefficient(index - codingVector.getLength());
        }
    }

    public CodedPacket copy(CodedPacket c) {

        assert(c.getFiniteField() == getFiniteField());

        return new CodedPacket(codingVector.copy(), payloadVector.copy());
        
    }

    public void setToZero() {
        codingVector.setToZero();
        payloadVector.setToZero();
    }

    public CodedPacket add(CodedPacket vector) {
        assert(vector.getFiniteField() == getFiniteField());

        return new CodedPacket(codingVector.add(vector.codingVector), payloadVector.add(vector.payloadVector));

    }

    public CodedPacket scalarMultiply(int c) {
        assert(c < getFiniteField().getCardinality() && c >= 0);

        return new CodedPacket(codingVector.scalarMultiply(c), payloadVector.scalarMultiply(c));
        
    }

    @Override
    public String toString() {
        
        return codingVector.toString() + " | " + payloadVector.toString();

    }




}
