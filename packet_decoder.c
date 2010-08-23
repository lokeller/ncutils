/*
 * Copyright (c) 2010, EPFL - ARNI
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

#include "ncutils_priv.h"
#include "ncutils.h"
#include <assert.h>
#include <malloc.h>

packet_decoder_t *create_packet_decoder(finite_field_t *ff, int max_packets, int payload_length_bytes) {
    packet_decoder_t *this = (packet_decoder_t *) malloc(sizeof(packet_decoder_t));

    this->codingVectorDecoder = create_coding_vector_decoder(max_packets, ff);
    this->ff = ff;
    this->packets = (coded_packet_t **) malloc(sizeof(coded_packet_t *) * max_packets);
    this->packet_count = 0;
    this->payloadCoordinatesCount = ff_bytes_to_coordinates(ff, payload_length_bytes);

    return this;

}

void destroy_packet_decoder(packet_decoder_t *this) {
    int i;
    destroy_coding_vector_decoder(this->codingVectorDecoder);
    for (i = 0 ; i < this->packet_count; i++) {
        destroy_coded_packet(this->packets[i]);
    }
    free(this->packets);
    free(this);
}

decoded_packets_t *_create_decoded_packets(int count) {
    decoded_packets_t *this = (decoded_packets_t*) malloc(sizeof(decoded_packets_t));

    this->packets = (uncoded_packet_t **) malloc(sizeof(uncoded_packet_t*) * count);
    this->count = count;

    return this;

}

void destroy_decoded_packets(p_decoded_packets_t this) {

    int i;

    for(i = 0 ; i < this->count ; i++) {
        destroy_uncoded_packet(this->packets[i]);
    }

    free(this->packets);    
    free(this);

}

vector_t *_packet_decoder_decode_payload(packet_decoder_t *this, vector_t *encoding) {


    int codedPacketId;
    int q = this->ff->q;


    /* this vector will store the linear combination of coded payloads that
       correspond to the decoded payload */

    vector_t *decodedPayload = create_vector(this->payloadCoordinatesCount, this->ff);

    /* linearly combine the payloads */
    for (codedPacketId = 0; codedPacketId < encoding->length; codedPacketId++) {
        int c;
        vector_t * codedPayload;

        int coeff = encoding->coordinates[codedPacketId];

        /* skip the packet if the coordinate is zero */
        if (coeff == 0) {
            continue;
        }

        codedPayload = this->packets[codedPacketId]->payload;

        /* linearly combine the payload of packet "codedPacketId" */
        for ( c = 0; c < codedPayload->length; c++) {
            int v2 = codedPayload->coordinates[c];
            int v1 = decodedPayload->coordinates[c];
            int val = this->ff->sum[v1 * q + this->ff->mul[coeff * q + v2]];
            decodedPayload->coordinates[c]=val;
        }
        
    }
    
    return decodedPayload;
}


p_decoded_packets_t packet_decoder_add_packet(p_packet_decoder_t this, p_coded_packet_t p) {

    decoded_coordinates_t *coords;
    decoded_packets_t *packets;
    int i;

    assert(p->coding_vector->ff->q == this->ff->q);
    assert(p->coding_vector->length == this->codingVectorDecoder->maxPackets);
    assert(p->payload->length == this->payloadCoordinatesCount);

    coords = coding_vector_decoder_add_vector(this->codingVectorDecoder, p->coding_vector);    

    if ( coords == 0) return 0;

    /* add the current packet only if it was linearly independant, this
     will be used to decode future packets*/
    this->packets[this->packet_count] = cp_copy(p);
    this->packet_count ++;
    /* decode the new packets that can be decoded */
    packets = _create_decoded_packets(coords->count);

    for (i = 0 ; i < coords->count ; i++) {        

            vector_t *decoded_payload = _packet_decoder_decode_payload(this, coords->coefficients[i]);

            packets->packets[i] = create_uncoded_packet_from_vector(coords->coordinates[i], decoded_payload);

            destroy_vector(decoded_payload);

    }

    destroy_decoded_coordinates(coords);

    return packets;


}

int decoded_packets_get_count(p_decoded_packets_t this) {
    return this->count;
}

p_uncoded_packet_t decoded_packets_get_packet(p_decoded_packets_t this, int pos) {
    return this->packets[pos];
}



