/*
 * Copyright (c) 2010 - 2011, EPFL - ARNI
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the EPFL nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package ch.epfl.arni.ncutils;

/**
 *
 * This class represents a coded packet. A coded packet is composed
 * by a coding vector that keeps track of the linear combination of
 * uncoded packets contained in the packet and of a payload vector that
 * contains the actual linear combination of uncoded packets.
 *
 * A packet can be seen as the finite vector obtained by concatenating
 * coding vector and payload vector.
 *
 *
 * @author lokeller
 */
public class CodedPacket {
    
    private FiniteFieldVector codingVector;
    private FiniteFieldVector payloadVector;

    /**
     *
     * Create a new coded packet containing only one uncoded packet. The
     * coding vector of this packet is an elementary vector, i.e. all entries
     * but one, corresponding to the uncoded packet id, are zero.
     *
     * @param packet the uncoded packet that will be put in the coded packet
     * @param maxPackets the maximal number of uncoded packets that can be combined
     * in this coded packet. This correspond to the length of teh coding vector.
     * @param ff The finite field that over which the vectors in the packet are
     * defined
     */
    public CodedPacket( UncodedPacket packet, int maxPackets, FiniteField ff) {

        this( new FiniteFieldVector(maxPackets, ff), ff.byteToVector(packet.getPayload()));

        codingVector.setCoordinate(packet.getId(), 1);
    }

    /**
     * Creates an empty coded packet, i.e. the coding vector is set to the zero
     * vector (and consequentely the payload vector is set to zero).
     *
     * @param maxPackets the maximal number of uncoded packets that can be combined
     * in this coded packet. This correspond to the length of teh coding vector.
     * @param payloadByteLen the length in bytes of the uncoded packets that can be combined
     * in this packet. The length of the payload vector of this packet will be choosen
     * based on this number accordingly to the finite field used.
     * @param ff The finite field that over which the vectors in the packet are
     * defined
     */
    public CodedPacket(int maxPackets, int payloadByteLen, FiniteField ff) {

        this( new FiniteFieldVector(maxPackets, ff),
                new FiniteFieldVector(ff.coordinatesCount(payloadByteLen), ff));
        
    }

    /**
     * Creates a coded packet from its binary representation.
     *
     * @param maxPackets the maximal number of uncoded packets that can be combined
     * in this coded packet. This correspond to the length of teh coding vector.
     * 
     * @param data an array containing the binary representation of the coded packet
     * @param offset the first byte of the binary representation in the array data
     * @param length the length of the binary representation
     * @param ff The finite field that over which the vectors in the packet are
     * defined
     */
    
    public CodedPacket(int maxPackets, byte[] data, int offset, int length, FiniteField ff) {
    	
    	int headerLen = ff.bytesLength(maxPackets);
    	
    	this.codingVector = ff.byteToVector(data, offset, headerLen);
    	this.payloadVector = ff.byteToVector(data, headerLen+offset, length - headerLen);
    	
        
    }
    
    private CodedPacket(FiniteFieldVector codingVector, FiniteFieldVector payloadVector) {
        this.codingVector = codingVector;
        this.payloadVector = payloadVector;
    }


    /**
     * Returns the coding vector of this packet. The coding vector describes
     * which, and how uncoded packets have been combined to form this coded
     * packet. WARNING: Changing the coding vector of a packet without updating accordingly
     * the payload vector introduces decoding errors.
     *
     * @return the coding vector of this packet
     */
    public FiniteFieldVector getCodingVector() {
       return codingVector;
    }

    /**
     * Returns the payload vector of this packet. The payload vector is a 
     * a linear combination of uncoded packets (seen as finite field vectors).
     * If the coding vector is ( a1, a2, ...., an) then the payload vector is
     * a1 * p1 + a2 * p2 + ... + an * pn where p1, ...pn are the finite field 
     * vector  representations of the payload of the uncoded packets.
     *
     * @return the payload vector of this packet
     */
    public FiniteFieldVector getPayload() {
        return payloadVector;
    }


    /**
     * Returns the finite field that is used to define the vectors
     * of this packet
     *
     * @return the finite field of the coding and payload vectors
     */
    public FiniteField getFiniteField() {
        return codingVector.getFiniteField();
    }


    /**
     *
     * Set the index-th coordinate of the vector representation of the packet. If
     * index is smaller than the length of the coding vector the corresponding
     * coding vector coordinate will be set, otherwise the cofficient index -
     * (lenght of the coding vector) of the payload will be set
     *
     *
     * @param index the index of the coordinate that must be set
     * @param value an element of the field over which the packet is defined
     */
    public void setCoordinate(int index, int value) {
        assert( index >= 0);
        assert(value < getFiniteField().getCardinality() && value >= 0);
        if ( index < codingVector.getLength()) {
            codingVector.setCoordinate(index, value);
        } else {
            payloadVector.setCoordinate(index - codingVector.getLength(), value);
        }
    }

    /**
     *
     * Get the index-th coordinate of the vector representation of the packet. If
     * index is smaller than the length of the coding vector the corresponding
     * coding vector coordinate will be returned, otherwise the cofficient index -
     * (lenght of the coding vector) of the payload will be returned
     *
     * @param index the index of the coordinate that must be retrieved
     * @return the value of the coordinate, an element of the field over which the packet is defined
     */
    public int getCoordinate(int index) {

        assert(index >= 0);

        if ( index < codingVector.getLength()) {
            return codingVector.getCoordinate(index);
        } else {
            return payloadVector.getCoordinate(index - codingVector.getLength());
        }
    }

    /**
     * Creates a copy of the packet
     *
     * @return a copy of the packet
     */
    public CodedPacket copy() {
      
        return new CodedPacket(codingVector.copy(), payloadVector.copy());
        
    }

    /**
     * Set the packet contents to be a linear combination of no uncoded packets.
     * This sets coding and payload vector of the packet to zero.
     */
    public void setToZero() {
        codingVector.setToZero();
        payloadVector.setToZero();
    }

    /**
     *
     * Returns a CodedPacket which is the sum of the current CodedPacket and
     * another packet. The created packet will have a coding and payload vector
     * which will be consistent, i.e. the content of the payload of the newly
     * created packet corresponds to the linear combination specified in its
     * coding vector
     *
     * @param vector the CodedPacket that will be summed
     * @return the sum of this and vector
     */
    public CodedPacket add(CodedPacket vector) {
        assert(vector.getFiniteField() == getFiniteField());

        return new CodedPacket(codingVector.add(vector.codingVector), payloadVector.add(vector.payloadVector));

    }

    /**
     *
     * Returns a CodedPacket which is a scalar multiple of the current
     * CodedPacket. The created packet will have a coding and payload vector
     * which will be consistent, i.e. the content of the payload of the newly
     * created packet corresponds to the linear combination specified in its
     * coding vector
     *
     * @param c an element of the finite field used to define this packet that
     * will be used to multiply the packet
     * @return the scalar multiple of the current packet, i.e. each coordinate
     * of the current packet will be multiplied by c.
     */
    public CodedPacket scalarMultiply(int c) {
        assert(c < getFiniteField().getCardinality() && c >= 0);

        return new CodedPacket(codingVector.scalarMultiply(c), payloadVector.scalarMultiply(c));
        
    }
    
    
    /**
     * Returns the binary representation of the packet
     * 
     * @return a byte array containing coding vector and payload
     */
    public byte[] toByteArray() {
    	byte [] header = codingVector.getFiniteField().vectorToBytes(codingVector);
    	byte [] payload = payloadVector.getFiniteField().vectorToBytes(payloadVector);
    	
    	byte[] ret = new byte[header.length + payload.length];
    	System.arraycopy(header, 0, ret, 0, header.length);
    	System.arraycopy(payload, 0, ret, header.length, payload.length);
    	
    	return ret;
    	
    }
    

    @Override
    public String toString() {
        
        return codingVector.toString() + " | " + payloadVector.toString();

    }




}
