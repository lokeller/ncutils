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

import java.security.InvalidParameterException;

import ch.epfl.arni.ncutils.Decoder;

/**
 * 
 * Native implementation of an encoder.
 *
 */

public final class NativeDecoder implements Decoder {
	
	/** handle of the decoded created in the C code */
	private long handle;	
	
	/** current number of innovative packets recieved */
	private int rank;
	
	/** number of original packets in which the segment has been divided */
	private int packetsPerSegment;
	
	/** length of a packet, excluding the coding coefficient vector */ 
	private int packetLen;
	
	/** length of the segment being reconstructed */
	private int segmentLen;
	
	/**
     * Creates a new NativeDecoder with a given segment size and packet per segment length.
     * 
     * @param segmentLength the length of the segment that has to be recovered
     * @param packetsPerSegment the number of original packets in which the segment has 
     * 							been divided (also known as generation size)
	 */
	public NativeDecoder(int segmentLength, int packetsPerSegment) {
		
		handle = createDecoderNative(segmentLength, packetsPerSegment);
		this.packetLen = segmentLength / packetsPerSegment;
		this.segmentLen = segmentLength;
		this.packetsPerSegment = packetsPerSegment;
	}
	
	@Override
	public synchronized void addPacket(byte[] data, int offset) {
		
		if (data.length - offset <  packetLen + packetsPerSegment) 
			throw new InvalidParameterException("Data packet too short");
		
		boolean innovative = addPacketNative(handle, data, offset);
		
		if (innovative) rank++;
	}
	
	@Override
	public boolean isDecoded() {
		return rank == packetsPerSegment;
	}
	
	@Override
	public int getRank() {
		return rank;
	}

	@Override
	public synchronized void getSegment(byte[] segment, int offset) {
		
		if ( rank < packetsPerSegment ) throw new IllegalStateException("Segment not fully decoded yet");
		
		if (segment.length - offset <  segmentLen) 
			throw new InvalidParameterException("Segment too short (expected " + segmentLen + " received " + (segment.length - offset));
		
		getSegmentNative(handle, segment, offset);
	}
	
	@Override
	public synchronized void getPacket(byte[] packet, int offset) {		
		getPacketNative(handle, packet, offset);			
	}

	@Override
	public int getPacketLength() {
		return packetLen;
	}

    @Override
	public void dispose() { 
    	disposeNative(handle); 
    	handle = 0;
    }
    
    protected void finalize() {    	
    	// make sure the decoder has been disposed
    	dispose(); 
    }
	
    private native long createDecoderNative(int segmentLength, int packetLength);
	private native void getSegmentNative(long handle, byte[] segment, int offset);
	private native boolean addPacketNative(long handle, byte[] data, int offset);
	private native void getPacketNative(long handle, byte[] packet, int offset);
	private native void disposeNative(long handle);
	
}
