/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.epfl.arni.ncutils;

/**
 *
 * @author lokeller
 */
public class UncodedPacket {

    private int id;
    private byte[] payload;

    public UncodedPacket(int id, byte[] payload) {
        this.id = id;
        this.payload = payload;
    }

    public UncodedPacket(int id, FiniteFieldVector payload, int size) {

        if ( payload.getFiniteField().getCardinality() != 256 &&
               payload.getFiniteField().getCardinality() != 16 ) {
            throw new RuntimeException("The only field size supported is 2^8 and 2^4 ( Q was "
                                            +payload.getFiniteField().getCardinality() );
        }

        this.id = id;
        this.payload = new byte[size];

        if (payload.getFiniteField().getCardinality() == 256) {
            for (int i = 0 ; i < size; i++) {
                this.payload[i] = (byte) payload.getCoefficient(i);
            }
        } else if (payload.getFiniteField().getCardinality() == 16) {
            for (int i = 0 ; i < size; i++) {
                this.payload[i] = (byte) ( (payload.getCoefficient(2*i+1) << 4) + payload.getCoefficient(2*i )) ;
            }
        }

    }

    public int getId() {
        return id;
    }

    public byte[] getPayload() {
        return payload;
    }

}
