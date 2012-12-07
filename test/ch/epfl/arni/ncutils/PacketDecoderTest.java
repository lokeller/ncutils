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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

import org.junit.Test;

import ch.epfl.arni.ncutils.CodedPacket;
import ch.epfl.arni.ncutils.FiniteField;
import ch.epfl.arni.ncutils.PacketDecoder;
import ch.epfl.arni.ncutils.UncodedPacket;

public class PacketDecoderTest {

	@Test
	public void testDecoder() {
		
        FiniteField ff = FiniteField.getDefaultFiniteField();

        int blockNumber = 10;
        int payloadLen = 10;

        /* create the uncoded packets */
        UncodedPacket[] inputPackets = new UncodedPacket[blockNumber];
        for ( int i = 0 ; i < blockNumber ; i++) {
            byte[] payload = new byte[payloadLen];
            Arrays.fill(payload, (byte) (0XA0 +  i));
            inputPackets[i] = new UncodedPacket(i, payload);
        }
        
        /* prepare the input packets to be sent on the network */
        CodedPacket[] codewords = new CodedPacket[blockNumber];

        for ( int i = 0 ; i < blockNumber ; i++) {
            codewords[i] = new CodedPacket( inputPackets[i], blockNumber, ff);
        }
        
        /* create a set of linear combinations that simulate
         * the output of the network
         */

        CodedPacket[] networkOutput = new CodedPacket[blockNumber];

        Random r = new Random(2131231);

        for ( int i = 0 ; i < blockNumber ; i++) {

            networkOutput[i] = new CodedPacket(blockNumber, payloadLen, ff);

            for ( int j = 0 ; j < blockNumber ; j++) {
                int x = r.nextInt(ff.getCardinality());                
                CodedPacket copy = codewords[j].scalarMultiply(x);
                networkOutput[i] = networkOutput[i].add(copy);
                
            }
        }

        /* decode the received packets */
        PacketDecoder decoder = new PacketDecoder(ff, blockNumber, payloadLen);

        assertEquals(blockNumber, decoder.getMaxPackets());
        
        ArrayList<UncodedPacket> uncoded = new ArrayList<UncodedPacket>();
        
        for ( int i = 0; i < blockNumber ; i++) {
            Vector<UncodedPacket> packets = decoder.addPacket(networkOutput[i]);
            
            assertEquals(i+1, decoder.getSubspaceSize());
            
            uncoded.addAll(packets);
        }
        
        assertEquals(0, decoder.addPacket(networkOutput[0]).size());

        assertEquals(uncoded.size(), blockNumber);
        
        boolean decoded [] = new boolean[blockNumber]; 
        
        for ( UncodedPacket packet : uncoded ) {
        	
        	assertFalse(decoded[packet.getId()]);
        	
        	decoded[packet.getId()] = true;
        	
        	assertArrayEquals(packet.getPayload(), inputPackets[packet.getId()].getPayload());
        	
        }
        
        for (int i = 0 ; i < blockNumber ; i++) {
        	assertEquals(decoder.getCodedPackets().get(i), networkOutput[i]);
        }
        
		
	}


}
