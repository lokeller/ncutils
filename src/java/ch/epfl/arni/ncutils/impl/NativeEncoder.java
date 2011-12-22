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

import ch.epfl.arni.ncutils.Encoder;

/**
 * Native implementation of an encoder
 *
 */

public class NativeEncoder implements Encoder {
	
	/** buffer holding the segment being encoded */
	private byte[] buffer;
	
	/** offset of the first byte of the segment in the buffer */
	private int offset;
	
	/** length of the segment */
	private int length;
	
	/** number of original packets in which the segment is divided */
	private int packetsPerSegment;
	
	/** length of a packet created by the encoder, including the coding coefficient vector */
	private int packetLength;
		

	/**
	 * Encodes the specified vector contained in buffer, starting at offset and of the
	 * specified length.  The segment is splitted in packetPerSegment original packets.  
	 * 
	 * @param buffer the buffer holding the segment being encoded
	 * @param offset the offset of the first byte of the segment in buffer
	 * @param length the length of the segment
	 * @param packetsPerSegment the number of original packets in which the segment must be divided.
	 */
	public NativeEncoder(byte[] buffer, int offset, int length, int packetsPerSegment) {
		this.buffer = buffer;
		this.packetsPerSegment = packetsPerSegment;
		this.packetLength = length / packetsPerSegment + packetsPerSegment;
		this.offset = offset;
		this.length = length;
	}	
	
	@Override
	public void getPacket(byte [] packet, int offset) {
		createPacket(buffer, offset, length, packet, offset, packetsPerSegment);
	}
	
	@Override
	public int getPacketLength() {
		return packetLength;
	}

	private static native void createPacket(byte[] segment, int offset, int len, byte[] packet, int packetOffset, int packetsPerSegment);
	
	
}
