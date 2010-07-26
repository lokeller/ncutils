/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.epfl.arni.ncutils;

import java.util.Map;
import java.util.Vector;


/**
 *
 * @author lokeller
 */
public class PacketDecoder {

    private Vector<EncodedPacket> packets = new Vector<EncodedPacket>();

    private CodingVectorDecoder codingVectorDecoder;

    private int payloadLen;

    private FiniteField ff;

    public PacketDecoder(int blockCount, int payloadLen) {
        this(FiniteField.getDefaultFiniteField(), blockCount, payloadLen);
    }

    public PacketDecoder(FiniteField field, int blockCount, int payloadLen) {
        this.ff = field;
        codingVectorDecoder = new ArrayBasedCodingVectorDecoder(blockCount);
        this.payloadLen = payloadLen;
    }
    
    public Vector<UncodedPacket> decode(EncodedPacket p) {
        try {

            Map<Integer, FiniteFieldVector> decoded = codingVectorDecoder.decode(p.getCodingVector());
            
            /* add the current packet only if it was linearly independant */
            packets.add(p);

            Vector<UncodedPacket> output = new Vector<UncodedPacket>();
            
            for ( Map.Entry<Integer, FiniteFieldVector> entry : decoded.entrySet() ) {

                FiniteFieldVector decodedPayload = new SparseFiniteFieldVector();

                for ( Integer codedPacketId : entry.getValue().getNonZeroCoefficients()) {

                    FiniteFieldVector codedPayload = packets.get(codedPacketId).getPayload();

                    int coeff = entry.getValue().getCoefficient(codedPacketId);

                    for ( Integer c : codedPayload.getNonZeroCoefficients()) {

                        int v1 = decodedPayload.getCoefficient(c);
                        int v2 = codedPayload.getCoefficient(c);

                        int val = ff.sum[v1][ff.mul[coeff][v2]];

                        decodedPayload.setCoefficient(c, val);
                    }

                }

                output.add(new UncodedPacket((int) entry.getKey(), decodedPayload, payloadLen));

            }


            return output;

            
        } catch (LinearDependantException ex) {
            return new Vector<UncodedPacket>();
        }
        
    }




}
