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

import ch.epfl.arni.ncutils.CodedPacket;
import ch.epfl.arni.ncutils.FiniteField;

public class CodedPacketTest {

	@Test
	public void testCodedPacketIntByteArrayIntIntFiniteField() {
		
		byte [] data = new byte[70];
		
		int offset = 10;
		int lenght = 40;
		
		data[offset] = 0x8;
		data[offset+15] = 0x8;
		
		FiniteField ff = new FiniteField(2, 4);
		
		CodedPacket packet = new CodedPacket(30, data, offset, lenght, ff);
	
		assertEquals(ff, packet.getFiniteField());
		
		assertEquals(1, packet.getCoordinate(0));
				
		assertEquals(1, packet.getPayload().getCoordinate(0));
		
		packet.setCoordinate(1, 2);
		
		assertEquals(2, packet.getCoordinate(1));
		assertEquals(2, packet.getCodingVector().getCoordinate(1));
		
		packet.setCoordinate(30, 3);
		
		assertEquals(3, packet.getCoordinate(30));
		assertEquals(3, packet.getPayload().getCoordinate(0));
	}	

	@Test
	public void testAddInPlace() {
	
		FiniteField ff = new FiniteField(2, 4);
				
		CodedPacket packet1 = new CodedPacket(3, 10, ff);		
		CodedPacket packet2 = new CodedPacket(3, 10, ff);
		
		packet1.setCoordinate(1, 1);
		packet1.getPayload().setCoordinate(0, 1);
		
		packet2.setCoordinate(1, 2);
		packet2.getPayload().setCoordinate(0, 2);
				
		packet2.addInPlace(packet1);
		
		assertEquals(3, packet2.getCoordinate(1));
		assertEquals(3, packet2.getCodingVector().getCoordinate(1));
		assertEquals(3, packet2.getPayload().getCoordinate(0));
	}

	@Test
	public void testMultiplyAndAddInPlace() {

		FiniteField ff = new FiniteField(2, 4);
		
		CodedPacket packet1 = new CodedPacket(3, 10, ff);		
		CodedPacket packet2 = new CodedPacket(3, 10, ff);
		
		packet1.setCoordinate(1, 2);
		packet1.getPayload().setCoordinate(0, 2);
		
		packet2.setCoordinate(1, 1);
		packet2.getPayload().setCoordinate(0, 1);
				
		packet2.multiplyAndAddInPlace(ff.inverse[2], packet1);
		
		assertEquals(0, packet2.getCoordinate(1));
		assertEquals(0, packet2.getCodingVector().getCoordinate(1));
		assertEquals(0, packet2.getPayload().getCoordinate(0));

		
	}
	
	@Test
	public void testMultiplyAndAdd() {

		FiniteField ff = new FiniteField(2, 4);
		
		CodedPacket packet1 = new CodedPacket(3, 10, ff);		
		CodedPacket packet2 = new CodedPacket(3, 10, ff);
		
		packet1.setCoordinate(1, 2);
		packet1.getPayload().setCoordinate(0, 2);
		
		packet2.setCoordinate(1, 1);
		packet2.getPayload().setCoordinate(0, 1);
				
		CodedPacket packet3 = packet2.multiplyAndAdd(ff.inverse[2], packet1);
		
		assertEquals(0, packet3.getCoordinate(1));
		assertEquals(0, packet3.getCodingVector().getCoordinate(1));
		assertEquals(0, packet3.getPayload().getCoordinate(0));

		assertEquals(2, packet1.getCoordinate(1));
		assertEquals(2, packet1.getCodingVector().getCoordinate(1));
		assertEquals(2, packet1.getPayload().getCoordinate(0));
		
		assertEquals(1, packet2.getCoordinate(1));
		assertEquals(1, packet2.getCodingVector().getCoordinate(1));
		assertEquals(1, packet2.getPayload().getCoordinate(0));
		
	}
	
	@Test
	public void testScalarMultiplyInPlace() {

		FiniteField ff = new FiniteField(2, 4);
		
		CodedPacket packet1 = new CodedPacket(3, 10, ff);		
		
		packet1.setCoordinate(1, 2);
		packet1.getPayload().setCoordinate(0, 2);
		
		packet1.scalarMultiplyInPlace(ff.inverse[2]);
		
		assertEquals(1, packet1.getCoordinate(1));
		assertEquals(1, packet1.getCodingVector().getCoordinate(1));
		assertEquals(1, packet1.getPayload().getCoordinate(0));

		
	}


	@Test
	public void testSetToZero() {

		FiniteField ff = new FiniteField(2, 4);
		
		CodedPacket packet1 = new CodedPacket(3, 10, ff);		
		
		packet1.setCoordinate(1, 2);
		packet1.getPayload().setCoordinate(0, 2);
		
		packet1.setToZero();
		
		assertEquals(0, packet1.getCoordinate(1));
		assertEquals(0, packet1.getCodingVector().getCoordinate(1));
		assertEquals(0, packet1.getPayload().getCoordinate(0));

		
	}
	
	@Test
	public void testCopy() {

		FiniteField ff = new FiniteField(2, 4);
		
		CodedPacket packet1 = new CodedPacket(3, 10, ff);		
		
		
		packet1.setCoordinate(1, 2);
		packet1.getPayload().setCoordinate(0, 2);

		CodedPacket packet2 = packet1.copy();
		
		packet2.setCoordinate(1, 1);
		packet2.getPayload().setCoordinate(0, 1);
				
		assertEquals(2, packet1.getCoordinate(1));
		assertEquals(2, packet1.getCodingVector().getCoordinate(1));
		assertEquals(2, packet1.getPayload().getCoordinate(0));
		
		assertEquals(1, packet2.getCoordinate(1));
		assertEquals(1, packet2.getCodingVector().getCoordinate(1));
		assertEquals(1, packet2.getPayload().getCoordinate(0));
		
	}

	
	@Test
	public void testToByteArray() {

		FiniteField ff = new FiniteField(2, 4);
		
		CodedPacket packet1 = new CodedPacket(3, 10, ff);				
		
		packet1.setCoordinate(1, 1);
		packet1.getPayload().setCoordinate(0, 1);

		byte[] data = packet1.toByteArray();
		
		assertEquals((byte) 0x80, data[0]);
		assertEquals((byte) 0x8, data[2]);		
		
	}
	
	@Test
	public void testToString() {

		FiniteField ff = new FiniteField(2, 4);
		
		CodedPacket packet1 = new CodedPacket(3, 10, ff);				
		
		packet1.setCoordinate(1, 1);
		packet1.getPayload().setCoordinate(0, 1);
		
		assertEquals("00  01  00  | 01  00  00  00  00  00  00  00  00  00  00  00  00  00  00  00  00  00  00  00 ", packet1.toString());		
		
	}
	
}
