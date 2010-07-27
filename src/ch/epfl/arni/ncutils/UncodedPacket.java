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

    public UncodedPacket(int id, FiniteFieldVector payload) {        

        this.id = id;
        this.payload = payload.getFiniteField().vectorToBytes(payload);

    }

    public int getId() {
        return id;
    }

    public byte[] getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        String ret = "Id: " + id + " Payload: ";
        for (int k = 0; k < payload.length; k++) {
            ret += String.format("%02X ", payload[k]);
        }

        return ret;
    }



}
