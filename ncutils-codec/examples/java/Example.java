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

import java.util.Arrays;
import java.util.Random;

import ch.epfl.arni.ncutils.CodecFactory;
import ch.epfl.arni.ncutils.Decoder;
import ch.epfl.arni.ncutils.Encoder;


public class Example {

	
	public static void main( String [] args) {
		
		// this is the length of the segment we want to send
		int segmentLength = 1000;
		
		// this is the segment that has to be transmitted
		byte [] segment = new byte[segmentLength];
		
		// we fill it with some random data
		new Random().nextBytes(segment);
		
		// we set the generation size to 10, this means that the packets will have
		// length segmentLength / generationSize + generationSize
		int generationSize = 10;
		
		// we create an encoder for the source
		Encoder encoder = CodecFactory.createEncoder(segment, 0, segmentLength, generationSize);
		
		// we create a decoder for the destination
		Decoder decoder = CodecFactory.createDecoder(segmentLength, generationSize);
		
		do {
			// we allocate some space to hold the packet we are going to create
			byte[] packet = new byte[encoder.getPacketLength()];
			
			// create the packet with the encoder
			encoder.getPacket(packet, 0);
			
		    // with probability 0.1 we drop the packet
			if ( Math.random() < 0.1 ) continue;
			
			// now we assume that the packet was received at the destination
			// let's put it in the decoder
			decoder.addPacket(packet, 0);
			
		// repeat the loop till we can decode, usually this happens after generationSize packets
		// have been sent
		} while ( decoder.isDecoded());
		
		// now we can get the decoded segment
		byte [] decodedSegment = new byte[segmentLength];
		decoder.getSegment(decodedSegment, 0);
		
		// let's check that we received everything correctly
		assert( Arrays.equals(decodedSegment, segment));
		
	}
	
}
