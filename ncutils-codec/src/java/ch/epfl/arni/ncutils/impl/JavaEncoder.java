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

import ch.epfl.arni.ncutils.Encoder;

/**
 * 
 * Java implementation of an Encoder
 *
 */

public class JavaEncoder implements Encoder {
	
	/** segment that is being encoded */
	private byte[] buffer;	
	
	/** offset at which the payload of the segment being encoded starts */
	private int offset;	
	
	/** number of original packets in which the segment is divided to perform encoding */ 
	private int packetsPerSegment;
	
	/** length of each packet including the coding vector header */
	private int packetLength;
	
	/** length of each packet excluding the coding vector header */
	private int packetPayloadLength;
	
	/** random number generator used to create random coefficients for encoding */
	private Random random;
	
	/**
	 * Encodes the specified vector contained in buffer, starting at offset and of the
	 * specifed length.  The semgent is splitted in packetPerSegment original packets.  
	 * 
	 * @param buffer the buffer holding the segment being encoded
	 * @param offset the offset of the first byte of the segment in buffer
	 * @param length the length of the segment
	 * @param packetsPerSegment the number of original packets in which the segment must be divided.
	 */
	public JavaEncoder(byte[] buffer, int offset, int length, int packetsPerSegment) {
		this.buffer = buffer;
		this.packetLength = length / packetsPerSegment + packetsPerSegment;
		this.packetPayloadLength = length / packetsPerSegment;
		this.packetsPerSegment = packetsPerSegment;
		this.offset = offset;
		this.random = new Random();
	}

	
	@Override
	public void getPacket(byte[] packet, int offset) {			
		
		VectorHelper.setToZero(packet, offset, packetLength);
				
		for  ( int i = 0 ; i < packetsPerSegment ; i++) {
			
			int coeff = random.nextInt() & 0xFF;
			
			packet[offset + i] = (byte) coeff;
			
			int offsetPayload = offset + packetsPerSegment;
			int offsetSegmentPacket = i * packetPayloadLength + this.offset;
		
			VectorHelper.multiplyAndAdd(packet, offsetPayload, packetPayloadLength, buffer, offsetSegmentPacket, coeff);

		}		
	
	}

	@Override
	public int getPacketLength() {
		return packetLength;
	}
	
}
