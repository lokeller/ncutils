/*
 * Copyright (c) 2011, EPFL - ARNI
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

package ch.epfl.arni.ncutils.impl;

import java.util.Random;

import ch.epfl.arni.ncutils.Decoder;


/**
 * 
 * Java implementation of a Decoder.
 * 
 *
 */
public class JavaDecoder implements Decoder {

	/** contains the length of a packet, including the coefficient header */
	private int packetLength;
	
	/** contains the size of generation, the number of packets the segment was divided */ 
	private int packetsPerSegment;
	
	/** holds the decoded segment */
	private byte decodedSegment[];
	
	/** holds a copy of the payload of the encoded packets that were added to the decoder */
	private byte[][] encodedPackets;
	
	/** holds the number of coded packets added to the decoder */ 
	private int encodedPacketCount;
	
	/** contains the length of the payload of each packet, its length 
	 * minus the length of the coding coefficient vector */
	private int packetPayloadLength;
	
	/** an instance of random used to create new encoded packets */
	private Random random;
	
	/** decoder used to find how to combine encoded packets to recover orginal packets */
    private CodingVectorDecoder codingVectorDecoder;
    
    /**
     * 
     * Creates a new DecoderImpl with a given segment size and packet per segment length.
     * 
     * @param segmentLength the length of the segment that has to be recovered
     * @param packetsPerSegment the number of original packets in which the segment has 
     * 							been divided (also known as generation size)
     */
	public JavaDecoder(int segmentLength, int packetsPerSegment) {
		packetLength = segmentLength / packetsPerSegment + packetsPerSegment;
		packetPayloadLength = segmentLength / packetsPerSegment ;
		this.packetsPerSegment = packetsPerSegment;
		codingVectorDecoder = new CodingVectorDecoder(packetsPerSegment);
		decodedSegment = new byte[segmentLength];
		random = new Random();
		encodedPackets = new byte[packetsPerSegment][packetLength];
	}

	@Override
	public void addPacket(byte[] data, int offset) {
		
		int prevDecoded = codingVectorDecoder.getDecodingVectorsCount();
		
		boolean innovative = codingVectorDecoder.addCodingVector(data, offset);
		
		if ( innovative ) {
			encodedPackets[encodedPacketCount] = data;
			System.arraycopy(data, offset, 
								encodedPackets[encodedPacketCount], 0, packetLength);
			encodedPacketCount++;
		}
		
		/* decode all original packets that we discovered how to decode */
        for ( int i = prevDecoded ; i < codingVectorDecoder.getDecodingVectorsCount(); i++ ) {
        	decodePayload(i);            	
        }
	}

	@Override
	public boolean isDecoded() {
		return codingVectorDecoder.getSubspaceSize() == packetsPerSegment;
	}

	@Override
	public int getRank() {
		return codingVectorDecoder.getSubspaceSize();
	}

	@Override
	public void getSegment(byte[] segment, int offset) {
		System.arraycopy(decodedSegment, 0, segment, offset, decodedSegment.length);
	}

	@Override
	public void getPacket(byte[] packet, int offset) {		
		
		VectorHelper.setToZero(packet, offset, packetLength);
		
		for  ( int i = 0 ; i < encodedPacketCount ; i++) {
			int coeff = random.nextInt() & 0xFF;
			VectorHelper.multiplyAndAdd(packet, offset, packetLength, encodedPackets[i], 0, coeff);
		}
		
	}
	
	/**
	 * Decodes an original packet using the information from the coding vector decoder. 
	 * @param index
	 */
    private void decodePayload(int index) {
        
        byte[] decodingVector = codingVectorDecoder.getDecodingVectorBuffer(index);
        int decodingVectorOffset = codingVectorDecoder.getDecodingVectorOffset();
        int decodedPacketOffset = codingVectorDecoder.getDecodingVectoPacketId(index) * packetPayloadLength;
        
		for (int codedPacketId = 0 ; codedPacketId < packetsPerSegment; codedPacketId++) {
          
			int coeff = decodingVector[codedPacketId + decodingVectorOffset] & 0xFF;

            /* skip the packet if we don't need to combine it*/
            if (coeff == 0) continue;                      
            
            byte[] codedPacket = encodedPackets[codedPacketId];
            
            /* linearly combine the payload of packet "codedPacketId" */    
            VectorHelper.multiplyAndAdd(decodedSegment, decodedPacketOffset, packetPayloadLength, 
            								codedPacket, packetsPerSegment, 
            								coeff);
        }        
    }

	@Override
	public void dispose() {
		return;
	}

	@Override
	public int getPacketLength() {
		return packetLength;
	}

}
