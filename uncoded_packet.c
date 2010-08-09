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

#include <malloc.h>
#include <memory.h>
#include "ncutils.h"
#include <stdio.h>
#include <stdarg.h>


uncoded_packet_t * create_uncoded_packet(int id, char* payload, int len) {
    
    uncoded_packet_t *this = (uncoded_packet_t *) malloc(sizeof(uncoded_packet_t));

    this->id = id;

    this->payload = (char *) malloc(len);
    this->payloadLen = len;
    memcpy(this->payload, payload, len);

    return this;

}

uncoded_packet_t * create_uncoded_packet_from_vector(int id, vector_t* payload) {

    uncoded_packet_t *this = (uncoded_packet_t *) malloc(sizeof(uncoded_packet_t));

    this->id = id;

    this->payloadLen = ff_coordinates_to_bytes(payload->ff, payload->length);

    this->payload = (char *) malloc(this->payloadLen);

    ff_vector_to_bytes(payload->ff, payload, this->payload, this->payloadLen);

    return this;

}

void destroy_uncoded_packet(uncoded_packet_t *this) {
    if (this->payload) {
        free(this->payload);
    }

    free(this);

}

void uncoded_packet_to_string(uncoded_packet_t* this, char* buffer, int len) {

    int i;
    char tmp[10];

    buffer[0] = 0;

    snprintf(buffer, len, "ID: %d - " , this->id);

    for (i = 0 ; i < this->payloadLen ; i++) {
        snprintf(tmp, 10, "%02hhx ", this->payload[i]);
        
        if ( strlen(buffer) + strlen(tmp) < len) {
            strcat(buffer, tmp);
        }
    }

}
