/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.epfl.arni.ncutils.examples;

import ch.epfl.arni.ncutils.EncodedPacket;
import ch.epfl.arni.ncutils.EncodedPacketImpl;
import ch.epfl.arni.ncutils.FiniteField;
import ch.epfl.arni.ncutils.FiniteFieldVector;
import ch.epfl.arni.ncutils.PacketDecoder;
import ch.epfl.arni.ncutils.SparseFiniteFieldVector;
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
        EncodedPacket[] codewords = new EncodedPacket[blockNumber];

        for ( int i = 0 ; i < blockNumber ; i++) {
            codewords[i] = new EncodedPacketImpl(blockNumber, inputPackets[i]);
        }

        System.out.println(" Codewords: ");
        printCodedPackets(Arrays.asList(codewords), payloadLenCoeffs);

        /* create a set of linear combinations that simulate
         * the output of the network
         */

        EncodedPacket[] networkOutput = new EncodedPacket[blockNumber];

        FiniteField ff = FiniteField.getDefaultFiniteField();

        Random r = new Random(2131231);

        FiniteFieldVector copy = new SparseFiniteFieldVector();

        for ( int i = 0 ; i < blockNumber ; i++) {

            networkOutput[i] = new EncodedPacketImpl(blockNumber);

            for ( int j = 0 ; j < blockNumber ; j++) {
                int x = r.nextInt(ff.getCardinality());
                copy.setToZero();
                codewords[j].copyTo(copy);
                copy.scalarMultiply(x);
                networkOutput[i].add(copy);
                
            }
        }

        System.out.println(" Network output: ");
        printCodedPackets(Arrays.asList(networkOutput), payloadLenCoeffs);

        /* decode the received packets */
        PacketDecoder decoder = new PacketDecoder(blockNumber, payloadLen);

        System.out.println(" Decoded packets: ");
        for ( int i = 0; i < blockNumber ; i++) {
            Vector<UncodedPacket> packets = decoder.decode(networkOutput[i]);
            printUncodedPackets(packets, payloadLen);
        }

    }

    private static void printUncodedPackets(Iterable<UncodedPacket> packets, int payloadLen) {
        for (UncodedPacket p : packets) {
            System.out.print("Id: " + p.getId());
            System.out.print(" Payload: ");
            for (int k = 0; k < payloadLen; k++) {
                System.out.print(String.format("%02X ", p.getPayload()[k]));
            }
            System.out.println("");
        }
    }

    private static void printCodedPackets(Iterable<EncodedPacket> packets, int payloadLen) {
        for (EncodedPacket p : packets) {
            for (int k = 0; k < p.getCodingCoefficientsCount(); k++) {
                System.out.print(" " + p.getCodingVector().getCoefficient(k));
            }
            System.out.print( " |");
            for (int k = 0; k < payloadLen; k++) {
                System.out.print(" " + p.getPayload().getCoefficient(k));
            }
            System.out.println("");
        }
    }

}
