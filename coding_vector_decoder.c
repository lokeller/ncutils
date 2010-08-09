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

#include <stdlib.h>
#include <memory.h>

#include "ncutils.h"

coding_vector_decoder_t* create_coding_vector_decoder(int max_packets, finite_field_t* ff) {

    coding_vector_decoder_t *decoder = (coding_vector_decoder_t *) malloc(sizeof(coding_vector_decoder_t));

    
    decoder->decodeMatrix = (int *) malloc(sizeof(int) * max_packets * max_packets * 2);    
    memset((char *) decoder->decodeMatrix, 0, sizeof(int) * max_packets * max_packets * 2);
    
    decoder->ff = ff;
    
    decoder->isPivot = malloc(sizeof(char) * max_packets);
    memset(decoder->isPivot, 0, sizeof(char) * max_packets);

    decoder->pivotPos = malloc(sizeof(int) * max_packets);
    memset(decoder->pivotPos, 0, sizeof(int) * max_packets);

    decoder->decoded = malloc(sizeof(char) * max_packets);
    memset(decoder->decoded, 0, sizeof(char) * max_packets);

    decoder->maxPackets = max_packets;

    return decoder;

}

void destroy_coding_vector_decoder(coding_vector_decoder_t *this) {

    free(this->decodeMatrix);

    free(this->decoded);
    free(this->isPivot);
    free(this->pivotPos);

    free(this);
    
}

decoded_coordinates_t *_create_decoded_coordinates(int count) {
    
    decoded_coordinates_t *this = (decoded_coordinates_t *) malloc(sizeof(decoded_coordinates_t));

    this->coordinates = (int *)malloc( sizeof(int) * count );
    this->coefficients = (vector_t **)malloc( sizeof(vector_t *) * count );

    this->count = count;

    return this;
}

void destroy_decoded_coordinates(decoded_coordinates_t *this) {
    int i;

    for ( i = 0 ; i < this->count ; i++) {        
        free(this->coefficients[i]);
    }
    
    free(this->coordinates);
    
    free(this);
    
}

decoded_coordinates_t* coding_vector_decoder_add_vector(coding_vector_decoder_t *this, vector_t *v ) {
    
    int *mul, *sub, *div, *decodeMatrix;
    int *pivotPos;
    char *isPivot;
    int size, totalSize, packetCount;
    int i,q;
    q = this->ff->q;
    
    mul = this->ff->mul;
    sub = this->ff->sub;
    div = this->ff->div;

    decodeMatrix = this->decodeMatrix;
    packetCount = this->packetCount;
    pivotPos = this->pivotPos;
    isPivot = this->isPivot;
    
    size = this->maxPackets;
    totalSize = this->maxPackets * 2;
   
    /* add the received packet at the bottom of the matrix */
    for (  i = 0 ; i < v->length ; i++) {
        decodeMatrix[packetCount * totalSize + i] = v->coordinates[i];
        decodeMatrix[packetCount * totalSize + i + size] = 0 ;
    }

    /* put zeros on the inverse matrix but on position packet count*/
    for (  i = size ; i < totalSize ; i++) {
        decodeMatrix[packetCount*totalSize +i ] = 0 ;
    }

    decodeMatrix[packetCount*totalSize+size+packetCount] = 1;

    /* simplify the new packet */

    /* zeros before */
    for ( i = 0 ; i < packetCount ; i++)  {

            int j;
            
            int m = decodeMatrix[packetCount*totalSize+pivotPos[i]];

            if (m == 0) continue;

            for ( j = 0 ; j < totalSize ; j++) {
                    int val = decodeMatrix[packetCount*totalSize+j];
                    int val2 = decodeMatrix[i*totalSize+j];
                    int ret = sub[val*q+ mul[val2*q + m]];
                    decodeMatrix[packetCount*totalSize+j] = ret;
            }

    }

    /* find pivot on the line */
    int pivot = -1;
    for ( i = 0 ; i < size ; i++) {
        if (isPivot[i]) continue;
        if (decodeMatrix[packetCount*totalSize +i] != 0) {
                pivotPos[packetCount] = i;
                isPivot[i] = 1;
                pivot = i;
                break;
        }
    }

    /* if the packet is not li stop here */

    if (pivot == -1 ) {

        return 0;
    }

    /* divide the line */

    if ( decodeMatrix[packetCount*totalSize+pivot] != 1 ) {
        int j;
        int pval = decodeMatrix[packetCount*totalSize+pivot];

        for ( j = 0 ; j < totalSize ; j++) {
                int val = decodeMatrix[packetCount*totalSize+j];
                int ret = div[val*q+pval];
                decodeMatrix[packetCount*totalSize+j] = ret;
        }

    }

    /* zero the column above the pivot */
    for (  i = 0 ; i < packetCount ; i++ ) {
            int j;
            int m = decodeMatrix[i*totalSize+pivot];

            if (m == 0) continue;

            for ( j = 0 ; j < totalSize ; j++) {

                    int val2 = decodeMatrix[packetCount*totalSize+j];
                    int val = decodeMatrix[i*totalSize+j];                    
                    decodeMatrix[i*totalSize+j] = sub[val * q + mul[val2 * q + m]];

            }

    }

    this->packetCount++;


    /* look for decodable blocks */


    int *willDecode = (int*) malloc(sizeof(int) * this->packetCount);
    int *willDecodePos = (int*) malloc(sizeof(int) * this->packetCount);
    int decodedCount = 0;

    for (  i = 0; i < this->packetCount ; i++) {
        int j;
        int pos = -1;

        /* skip if the line is marked decoded */
        if (this->decoded[i]) continue;

        for (  j = 0 ; j < size ; j++) {

            if (decodeMatrix[i * totalSize + j] != 0 && pos != -1) {
                pos = -1;
                break;
            } else if (decodeMatrix[i * totalSize + j] != 0) pos = j;
        }

        if ( pos >= 0) {
            willDecode[decodedCount] = i;
            willDecodePos[decodedCount] = pos;
            decodedCount++;
        }
    }

    decoded_coordinates_t *decoded = _create_decoded_coordinates(decodedCount);

    for (  i = 0; i < decodedCount ; i++) {
        int j;
        
        this->decoded[willDecode[i]] = 1;
        decoded->coefficients[i] = create_vector(size, this->ff);

        for (  j = size ; j < size + size ; j++) {
            decoded->coefficients[i]->coordinates[j-size] = decodeMatrix[willDecode[i] * totalSize + j];
        }

        decoded->coordinates[i] = willDecodePos[i];
        
    }

    free(willDecode);
    free(willDecodePos);

    return decoded;

}
