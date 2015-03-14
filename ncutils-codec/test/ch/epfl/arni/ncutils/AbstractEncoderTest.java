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

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import ch.epfl.arni.ncutils.Encoder;
import ch.epfl.arni.ncutils.impl.JavaDecoder;
import ch.epfl.arni.ncutils.impl.FiniteField;

public abstract class AbstractEncoderTest {

	public abstract Encoder createEncoder(byte[] segment, int offset, int length, int packetsPerSegment);
	
	@Test
	public void testEncode() {

		int segmentLen = 100;
		int packetLen = 10;
		int coords = segmentLen / packetLen;
		
		byte[] data = new byte[segmentLen];
		
		Random random = new Random();
		
		random.nextBytes(data);
		
		byte[] data2 = new byte[segmentLen+1];
		
		Encoder encoder = createEncoder(data, 0, segmentLen, 1);
		
		encoder.getPacket(data2, 0);
				
		for (int i = 0 ; i < segmentLen ; i++) {
			int j = FiniteField.getF256().mul[data[i] & 0xFF][data2[0]&0xFF];
			if ( j != (data2[i+1] & 0xFF)) {
				System.out.println("Error at coordinate " + i + " is " + data2[i+1] + " was " + j);
			}
		}
		
		encoder = createEncoder(data, 0, segmentLen, coords);		
		
		byte[][] packets = new byte[coords][]; 
		
		for (int i = 0 ; i < packets.length; i++) {
			packets[i] = new byte[packetLen+coords];
			encoder.getPacket(packets[i], 0);					
		}
	
		JavaDecoder decoder = new JavaDecoder(segmentLen, coords);
		
		byte [] decodedSegment = new byte[segmentLen];
		
		for (int i = 0 ; i < packets.length ; i++) {
			decoder.addPacket(packets[i], 0);			
		}
		
		decoder.getSegment(decodedSegment, 0);
		
		assertArrayEquals(data, decodedSegment);		
	
	}

}
