/*******************************************************************************
 * Copyright (c) 2012, EPFL - ARNI
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the EPFL nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *  
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package ch.epfl.arni.ncutils;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.epfl.arni.ncutils.FiniteField;
import ch.epfl.arni.ncutils.Vector;
import ch.epfl.arni.ncutils.UncodedPacket;

public class UncodedPacketTest {

	@Test
	public void testWrap() {
		
		byte[] payload = new byte[100];
		int id = 10;
	
		UncodedPacket packet = UncodedPacket.wrap(id, payload);
		
		assertEquals(id, packet.getId());	
		assertEquals(payload, packet.getPayload());
		
		payload[10] = 10;
		
		assertEquals(10, packet.getPayload()[10]);
		
	}

	@Test
	public void testConstructor1() {

		byte[] payload = new byte[100];
		int id = 10;
	
		UncodedPacket packet = new UncodedPacket(id, payload);
		
		assertEquals(id, packet.getId());	
		assertArrayEquals(payload, packet.getPayload());
		
		payload[10] = 10;
		
		assertEquals(0, packet.getPayload()[10]);
			
	}
	

	@Test
	public void testConstructor2() {

		int[] payload = new int[100];
		int id = 10;
	
		payload[0] = 1;		
		payload[1] = 1;
		
		FiniteField ff = new FiniteField(2, 4);
		
		Vector v  = Vector.wrap(payload, ff);
		
		UncodedPacket packet = new UncodedPacket(id, v);
		
		assertEquals(id, packet.getId());	
		assertEquals((byte) 0x88, packet.getPayload()[0]);		
			
	}
	
	@Test 
	public void testEquals() {
	
		byte[] payload = new byte[100];
		int id = 10;
	
		payload[10] = 10;
		 
		UncodedPacket packet = UncodedPacket.wrap(id, payload);
	
		assertFalse(packet.equals(new Object()));
		assertFalse(packet.equals(null));
		assertFalse(packet.equals(UncodedPacket.wrap(id+1, payload)));
		assertFalse(packet.equals(UncodedPacket.wrap(id, new byte[100])));
		assertFalse(packet.equals(UncodedPacket.wrap(id, new byte[10])));
		
		byte[] payload2 = new byte[110];
		payload2[10] = 10;
		
		assertFalse(packet.equals(UncodedPacket.wrap(id, payload2)));
		
		assertTrue(packet.equals(packet));
		
		byte[] payload3 = new byte[100];
		payload3[10] = 10;
		assertTrue(packet.equals(UncodedPacket.wrap(id, payload3)));
		
	}
	
	@Test
	public void testCompareTo() {
		
		UncodedPacket packet1 = new UncodedPacket(1, new byte[100]);
		UncodedPacket packet2 = new UncodedPacket(2, new byte[100]);
	
		assertTrue(packet1.compareTo(packet2) < 0);
		assertTrue(packet2.compareTo(packet1) > 0);
		assertTrue(packet2.compareTo(packet2) == 0);
		
	}
	
	@Test
	public void testCopy() {
		
		UncodedPacket packet = new UncodedPacket(1, new byte[100]);
		
		packet.getPayload()[1] = 10;
	
		UncodedPacket copy = packet.copy();
		
		assertEquals(10, copy.getPayload()[1]);
		
		packet.getPayload()[1] = 11;
		
		assertEquals(10, copy.getPayload()[1]);
		
		
	}
	
	@Test
	public void testHashCode() {
		
		UncodedPacket packet = new UncodedPacket(1, new byte[100]);
		
		packet.getPayload()[1] = 10;
	
		UncodedPacket packet2 = new UncodedPacket(2, new byte[100]);
		
		packet2.getPayload()[1] = 10;

		UncodedPacket packet3 = new UncodedPacket(1, new byte[100]);
		
		packet3.getPayload()[1] = 10;
		
		UncodedPacket packet4 = new UncodedPacket(1, new byte[100]);		
		
		assertEquals(packet.hashCode(), packet.hashCode());
		
		assertFalse(packet.hashCode() == packet2.hashCode());
		
		assertEquals(packet.hashCode(), packet3.hashCode());
		
		assertFalse(packet.hashCode() == packet4.hashCode());
	}
	

	@Test
	public void testToString() {
		
		UncodedPacket packet = new UncodedPacket(1, new byte[3]);
		
		packet.getPayload()[1] = 10;
		
		assertEquals("Id: 1 Payload: 00 0A 00 ", packet.toString());
	
	}
	
	
	
}
