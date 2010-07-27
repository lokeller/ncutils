/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.epfl.arni.ncutils.examples;

import ch.epfl.arni.ncutils.CodedPacket;
import ch.epfl.arni.ncutils.CodedPacket;
import ch.epfl.arni.ncutils.FiniteField;
import ch.epfl.arni.ncutils.FiniteFieldVector;
import ch.epfl.arni.ncutils.PacketDecoder;
import ch.epfl.arni.ncutils.UncodedPacket;
import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

/**
 *
 * @author lokeller
 */
public class BlockLevelExample {

    public static void main(String [] args) {

        FiniteField ff = FiniteField.getDefaultFiniteField();

        int blockNumber = 10;
        int payloadLen = 10;
        int payloadLenCoeffs = 20;

        /* create the uncoded packets */
        UncodedPacket[] inputPackets = new UncodedPacket[blockNumber];
        for ( int i = 0 ; i < blockNumber ; i++) {
            byte[] payload = new byte[payloadLen];
            Arrays.fill(payload, (byte) (0XA0 +  i));
            inputPackets[i] = new UncodedPacket(i, payload);
        }

        System.out.println(" Input blocks: ");
        printUncodedPackets(Arrays.asList(inputPackets), payloadLen);

        /* prepare the input packets to be sent on the network */
        CodedPacket[] codewords = new CodedPacket[blockNumber];

        for ( int i = 0 ; i < blockNumber ; i++) {
            codewords[i] = new CodedPacket( inputPackets[i], blockNumber, ff);
        }

        System.out.println(" Codewords: ");
        printCodedPackets(Arrays.asList(codewords), payloadLenCoeffs);

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

        System.out.println(" Network output: ");
        printCodedPackets(Arrays.asList(networkOutput), payloadLenCoeffs);

        /* decode the received packets */
        PacketDecoder decoder = new PacketDecoder(ff, blockNumber, payloadLenCoeffs);

        System.out.println(" Decoded packets: ");
        for ( int i = 0; i < blockNumber ; i++) {
            Vector<UncodedPacket> packets = decoder.decode(networkOutput[i]);
            printUncodedPackets(packets, payloadLen);
        }

    }

    private static void printUncodedPackets(Iterable<UncodedPacket> packets, int payloadLen) {
        for (UncodedPacket p : packets) {            
            System.out.println(p);
        }
    }

    private static void printCodedPackets(Iterable<CodedPacket> packets, int payloadLen) {
        for (CodedPacket p : packets) {
            System.out.println(p);
        }
    }

}
