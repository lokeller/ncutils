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

package ch.epfl.arni.ncutils;

/**
 * 
 * Reconstructs a segment from a set of packets. While the segment it is
 * being reconstructed the decoder can be used as an encoder to create new
 * packets.
 *
 */

public interface Decoder extends Encoder {

	/**
	 * Adds a new packet to the the decoder. The packet starts
	 * at position offset in the buffer
	 * 
	 * @param buffer the buffer containing the packet
	 * @param offset the position of the first byte of the packet in the buffer
	 */
	public abstract void addPacket(byte[] buffer, int offset);

	/**
	 * Returns true if enough packets where received and the segment can
	 * be reconstructed
	 * 
	 * @return true if the segment was reconstructed, false if more packets are necessary
	 */
	public abstract boolean isDecoded();

	/**
	 * Returns the number of innovative packets received up to now.
	 * 
	 * @return
	 */
	public abstract int getRank();
	
	/**
	 * Writes the segment that was reconstructed in the buffer at the specified
	 * position 
	 * 
	 * @param buffer a buffer where the segment will be stored 
	 * @param offset in the buffer where the segment should be written
	 */
	public abstract void getSegment(byte[] buffer, int offset);

	/**
	 * Releases the resources associated with this decoder.
	 * Call this method when you don't need the decoder anymore.
	 * If the user doesn't call this method it will be automatically
	 * called when the decoder is garbage-collected.
	 *  
	 */
	public abstract void dispose();

}