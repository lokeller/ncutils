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

#include "ncutils.h"
#include "ncutils_priv.h"
#include <assert.h>
#include <malloc.h>
#include "field_tables.h"
#include <string.h>

packet_decoder_t *create_packet_decoder(int max_packets, int payload_length) {
    packet_decoder_t *this = (packet_decoder_t *) malloc(sizeof(packet_decoder_t));

    if (!this) return 0;

    this->codingVectorDecoder = create_coding_vector_decoder(max_packets);

    if ( !this->codingVectorDecoder) {
	free(this);
    }

    this->encodedPackets = (ffe *) malloc(sizeof(ffe) * max_packets * ( payload_length + max_packets));
    
    if (!this->encodedPackets) {
        destroy_coding_vector_decoder(this->codingVectorDecoder);
        free(this);
	return 0;
    }

    this->decodedPackets = (ffe *) malloc(sizeof(ffe) * max_packets * payload_length);
    
    if (!this->decodedPackets) {
        destroy_coding_vector_decoder(this->codingVectorDecoder);
	free(this->encodedPackets);
        free(this);
	return 0;
    }

    this->decodedIds = (char *) malloc(sizeof(char) * max_packets);
    
    if (!this->decodedIds) {
        destroy_coding_vector_decoder(this->codingVectorDecoder);
	free(this->encodedPackets);
	free(this->decodedPackets);
        free(this);
	return 0;
    }

    this->payloadLength = payload_length;
    this->packetLength = payload_length + max_packets;
    this->decodedPacketsCount = 0;
    this->encodedPacketsCount = 0;
    return this;

}

void destroy_packet_decoder(packet_decoder_t *this) {
    int i;
    destroy_coding_vector_decoder(this->codingVectorDecoder);
    free(this->encodedPackets);
    free(this->decodedPackets);
    free(this->decodedIds);
    free(this);
}

void pd_get_packet(p_packet_decoder_t this, ffe* packet) {

    int i,j;
    ffe v1, v2;
    int coeff;
    ffe *table_row;
    ffe *coded_packet;

    memset(packet, 0, this->packetLength);

    for (i = 0; i < this->encodedPacketsCount; i++) {

        coeff = lrand48() & 0xFF;

	table_row = mul_table + (coeff << 8);

        coded_packet = this->encodedPackets + i * this->packetLength;

        for ( j = 0; j < this->packetLength; j++) {
            v2 = packet[j];
            v1 = coded_packet[j];
            packet[j] = v1 ^ table_row[v2];
        }
        
    }
    
}

void pd_decode_payload(packet_decoder_t *this, ffe* encoding) {

    int i,j;
    ffe v1, v2;
    int coeff;
    ffe *table_row;
    ffe *output, *encoded_packet;

    output = this->decodedPackets + this->decodedPacketsCount * this->payloadLength;

    memset(output, 0, this->payloadLength);

    for (i = 0; i < this->encodedPacketsCount; i++) {

        coeff = encoding[i];

        /* skip the packet if the coordinate is zero */
        if (coeff == 0) {
            continue;
        }

	table_row = mul_table + (coeff << 8);

        encoded_packet = this->encodedPackets + (i+1)*this->packetLength - this->payloadLength;

        /* linearly combine the payload of packet "codedPacketId" */
        for ( j = 0; j < this->payloadLength; j++) {
            v2 = encoded_packet[j];
            v1 = output[j];
            output[j] = v1 ^ table_row[v2];
        }
        
    }
    
}


int pd_add_packet(p_packet_decoder_t this, ffe* packet) {

    int i;
    int innovative;
    int decodedBefore, decodedAfter;

    decodedBefore = cvd_decoded_coordinates_get_count( this->codingVectorDecoder);

    innovative = cvd_add_vector(this->codingVectorDecoder, packet);    

    if ( innovative ) {
	memcpy(this->encodedPackets + this->encodedPacketsCount * this->packetLength, packet, this->packetLength);
	this->encodedPacketsCount++;
    }

    decodedAfter = cvd_decoded_coordinates_get_count( this->codingVectorDecoder);

    for ( i = decodedBefore ; i < decodedAfter ; i++) {

	ffe *encoding = cvd_decoded_coordinates_get_coefficients(this->codingVectorDecoder, i);
	
	pd_decode_payload(this, encoding);

	this->decodedIds[this->decodedPacketsCount] = cvd_decoded_coordinates_get_coordinate(this->codingVectorDecoder, i);

	this->decodedPacketsCount++;
    }

    return innovative;

}

int pd_decoded_packets_get_count(p_packet_decoder_t this) {
	return this->decodedPacketsCount;
}

ffe* pd_decoded_packets_get_packet(p_packet_decoder_t this, int idx) {
	return this->decodedPackets+this->payloadLength*idx;
}

int pd_decoded_packets_get_id(p_packet_decoder_t this, int idx) {
	return this->decodedIds[idx];
}

