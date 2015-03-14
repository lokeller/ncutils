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
#include <string.h>

coded_packet_t *create_coded_packet_from_uncoded(uncoded_packet_t *data, int max_packets, finite_field_t *ff) {
    coded_packet_t * this = (coded_packet_t *) malloc(sizeof(coded_packet_t));

    this->coding_vector = create_vector(max_packets,ff);
    this->payload = create_vector(ff_bytes_to_coordinates(ff, data->payloadLen), ff);

    this->coding_vector->coordinates[data->id] = 1;

    ff_bytes_to_vector(ff, data->payload, data->payloadLen, this->payload);

    return this;
}

coded_packet_t *create_coded_packet(int max_packets, int payload_bytes_len, finite_field_t *ff) {
    coded_packet_t * this = (coded_packet_t *) malloc(sizeof(coded_packet_t));

    this->coding_vector = create_vector(max_packets,ff);
    this->payload = create_vector(ff_bytes_to_coordinates(ff, payload_bytes_len), ff);

    return this;

}

coded_packet_t *_create_coded_packet_from_vectors(vector_t *coding_vector, vector_t *payload) {
    coded_packet_t * this = (coded_packet_t *) malloc(sizeof(coded_packet_t));

    this->coding_vector = coding_vector;
    this->payload = payload;

    return this;
}

void destroy_coded_packet(coded_packet_t *this) {
    destroy_vector(this->coding_vector);
    destroy_vector(this->payload);
    free(this);

}

void cp_set_coordinate(coded_packet_t *this, int index, int value) {
    assert( index >= 0);
    assert( value < this->coding_vector->ff->q && value >= 0);

    if ( index < this->coding_vector->length) {
        this->coding_vector->coordinates[index] = value;
    } else {
        this->payload->coordinates[index-this->coding_vector->length] = value;
    }
}

int cp_get_coordinate(coded_packet_t *this, int index) {

        assert(index >= 0);

        if ( index < this->coding_vector->length) {
            return this->coding_vector->coordinates[index];
        } else {
            return this->payload->coordinates[index-this->coding_vector->length];
        }
    }

coded_packet_t *cp_copy(coded_packet_t *this ) {

    coded_packet_t *new = (coded_packet_t *) malloc(sizeof(coded_packet_t));

    new->coding_vector = vector_copy(this->coding_vector);
    new->payload = vector_copy(this->payload);

    return new;

}

 
 void cp_set_to_zero(coded_packet_t *this) {
    vector_set_to_zero(this->coding_vector);
    vector_set_to_zero(this->payload);
}



coded_packet_t* cp_add(coded_packet_t *this, coded_packet_t* other) {
        
    assert(this->coding_vector->ff->q == other->coding_vector->ff->q);

    return _create_coded_packet_from_vectors(vector_add(this->coding_vector,
                                                           other->coding_vector),
                                               vector_add(this->payload,
                                                            other->payload) );

}

coded_packet_t* cp_scalar_multiply(coded_packet_t *this, int c) {


    return _create_coded_packet_from_vectors(vector_scalar_multiply(this->coding_vector,
                                                           c),
                                               vector_scalar_multiply(this->payload,
                                                            c) );

}

void cp_to_string(coded_packet_t *this, char* buf, int len) {
    
    vector_to_string(this->coding_vector, buf, len);
    
    if ( strlen(buf) < len - 3) {
        strcat(buf, " | ");
    }

    vector_to_string(this->payload, buf+strlen(buf), len - strlen(buf));
        

}


