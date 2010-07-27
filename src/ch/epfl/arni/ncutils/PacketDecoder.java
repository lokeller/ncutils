/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.epfl.arni.ncutils;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;


/**
 *
 * @author lokeller
 */
public class PacketDecoder {

    private Vector<CodedPacket> packets = new Vector<CodedPacket>();

    private CodingVectorDecoder codingVectorDecoder;

    private int payloadCoefficientsCount;

    private FiniteField ff;

    public PacketDecoder(FiniteField field, int maxPackets, int payloadCoefficientsCount) {
        this.ff = field;
        codingVectorDecoder = new CodingVectorDecoder(maxPackets,ff);
        this.payloadCoefficientsCount = payloadCoefficientsCount;
    }
    
    public Vector<UncodedPacket> decode(CodedPacket p) {

        assert(p.getFiniteField() == ff);
        assert(p.getCodingVector().getLength() == codingVectorDecoder.getMaxPackets());
        assert(p.getPayload().getLength() == payloadCoefficientsCount);
        
        try {

            Map<Integer, FiniteFieldVector> decoded = codingVectorDecoder.decode(p.getCodingVector());
            
            /* add the current packet only if it was linearly independant, this
             will be used to decode future packets*/
            packets.add(p);

            /* decode the new packets that can be decoded */
            Vector<UncodedPacket> output = new Vector<UncodedPacket>();
            
            for ( Map.Entry<Integer, FiniteFieldVector> entry : decoded.entrySet() ) {

                FiniteFieldVector decodedPayload = decodePayload(entry.getValue());

                output.add(new UncodedPacket((int) entry.getKey(), decodedPayload));

            }

            return output;

            
        } catch (LinearDependantException ex) {
            return new Vector<UncodedPacket>();
        }
        
    }

    private FiniteFieldVector decodePayload(FiniteFieldVector encoding) {
        
        /* this vector will store the linear combination of coded payloads that
           correspond to the decoded payload */
        FiniteFieldVector decodedPayload = new FiniteFieldVector(payloadCoefficientsCount, ff);

        /* linearly combine the payloads */
        for (int codedPacketId = 0; codedPacketId < encoding.getLength(); codedPacketId++) {

            int coeff = encoding.getCoefficient(codedPacketId);

            /* skip the packet if the coefficient is zero */
            if (coeff == 0) {
                continue;
            }

            FiniteFieldVector codedPayload = packets.get(codedPacketId).getPayload();

            /* linearly combine the payload of packet "codedPacketId" */
            for (int c = 0; c < codedPayload.getLength(); c++) {
                int v2 = codedPayload.getCoefficient(c);                
                int v1 = decodedPayload.getCoefficient(c);
                int val = ff.sum[v1][ff.mul[coeff][v2]];
                decodedPayload.setCoefficient(c, val);
            }
        }
        return decodedPayload;
    }




}
