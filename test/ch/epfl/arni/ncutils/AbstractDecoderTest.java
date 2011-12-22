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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

public abstract class AbstractDecoderTest {
	
	public abstract Decoder createDecoder(int segmentLength, int packetsPerSegment);
	
	public abstract Encoder createEncoder(byte[] segment, int offset, int length, int packetsPerSegment);
	
	@Test
	public void test() {
		
		byte [] segment = new byte[20000];
		
		new Random().nextBytes(segment);
		
		Decoder decoder = createDecoder(segment.length, 20);
		
		Encoder encoder = createEncoder(segment, 0, segment.length, 20);
		
		while (!decoder.isDecoded()) {
			byte [] packet = new byte[20 + segment.length / 20];
			 
			encoder.getPacket(packet, 0);
	
			decoder.addPacket(packet, 0);
				
		}
		
		byte [] decodedSegment = new byte[segment.length];
		
		decoder.getSegment(decodedSegment, 0);
		
		assertArrayEquals(segment, decodedSegment);
		
	}

	@Test
	public void testOnlineCoding() {
		
		byte [] segment = new byte[1000];
		
		new Random().nextBytes(segment);
		
		Decoder decoder = createDecoder(segment.length, 20);
		
		Decoder decoder2 = createDecoder(segment.length, 20);
		
		Encoder encoder = createEncoder(segment, 0, segment.length, 20);
		
		while( !decoder.isDecoded()) {
			
			byte [] packet = new byte[segment.length / 20 + 20];
			
			encoder.getPacket(packet, 0);
			
			int prevRank = decoder.getRank();
			
			decoder.addPacket(packet, 0);
			
			if ( prevRank < decoder.getRank()) {
				prevRank = decoder2.getRank();
				while ( decoder2.getRank() == prevRank) {
					byte [] packet2 = new byte[segment.length / 20 + 20];			
					decoder.getPacket(packet2, 0);		
					decoder2.addPacket(packet2, 0);
				}
			}
			
		}
		
		byte [] segmentDecoded = new byte[segment.length];
		
		decoder.getSegment(segmentDecoded, 0);		
		assertArrayEquals(segment, segmentDecoded);
		
		decoder2.getSegment(segmentDecoded, 0);		
		assertArrayEquals(segment, segmentDecoded);
		
	}
	
	@Test
	public void testLinearDependance() {
		
		byte [] segment = new byte[1000];
		
		new Random().nextBytes(segment);
		
		Decoder decoder = createDecoder(segment.length, 10);		

		byte [] packet = new byte[10 + segment.length / 10];

		Encoder encoder = createEncoder(segment, 0, segment.length, 10);
		
		encoder.getPacket(packet, 0);

		decoder.addPacket(packet, 0);
		
		decoder.addPacket(packet, 0);
		
		assertEquals(1, decoder.getRank());
		
		while (!decoder.isDecoded()) {
			byte [] packet2 = new byte[10 + segment.length / 10];
			 
			encoder.getPacket(packet2, 0);
	
			decoder.addPacket(packet2, 0);
			
		}
		
		byte [] decodedSegment = new byte[segment.length];
		
		decoder.getSegment(decodedSegment, 0);
		
		assertArrayEquals(segment, decodedSegment);
		
	}
	
	@Test
	public void testGB() {
		
		byte [] segment = new byte[22500];
		
		new Random().nextBytes(segment);
		
		byte[][] packets = new  byte[25][25 + 900];

		long startTime = System.currentTimeMillis();
		
		Encoder encoder = createEncoder(segment, 0, segment.length, 25);
		
		for (int i = 0 ; i < 1000 ; i++) {
				 
			for(int j = 0 ; j < 25; j++) {
				encoder.getPacket(packets[j], 0);	
			}
			
		}

		System.out.println("Rate:" + ( 8 * 1000.0 * segment.length)  / (double) (System.currentTimeMillis() - startTime) + " kbps");
		
		startTime = System.currentTimeMillis();
		
		for (int i = 0 ; i < 1000 ; i++) {
			Decoder decoder = createDecoder(segment.length, 25);
			
			for(int j = 0 ; j < 25; j++) {				
				decoder.addPacket(packets[j], 0);
			}
			
			byte [] decodedSegment = new byte[segment.length];
			
			decoder.getSegment(decodedSegment, 0);
		}
		
		System.out.println("Rate:" + ( 8 * 1000.0 * segment.length)  / (double) (System.currentTimeMillis() - startTime) + " kbps");
		
	}
	
}
