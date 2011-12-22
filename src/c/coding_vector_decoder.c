/*
 * Copyright (c) 2010, EPFL - ARNI
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, self list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, self list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the EPFL nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from self software without specific prior written permission.
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
#include "ncutils_priv.h"
#include "field_tables.h"


coding_vector_decoder_t* create_coding_vector_decoder(int max_packets) {

    coding_vector_decoder_t *decoder = (coding_vector_decoder_t *) malloc(sizeof(coding_vector_decoder_t));

    if (!decoder) return 0;
    
    decoder->decodeMatrix = (ffe *) malloc(sizeof(ffe) * max_packets * max_packets * 2);    

    if (!decoder->decodeMatrix) {
	free(decoder);
	return 0;
    }

    decoder->isPivot = malloc(sizeof(char) * max_packets);

    if (!decoder->isPivot) {
	free(decoder->decodeMatrix);
	free(decoder);
	return 0;
    }


    decoder->pivotPos = malloc(sizeof(int) * max_packets);
    
    if (!decoder->pivotPos) {
        free(decoder->isPivot);
        free(decoder->decodeMatrix);
        free(decoder); 
        return 0;
    }


    decoder->decoded = malloc(sizeof(ffe) * max_packets);

    if (!decoder->decoded) {
	free(decoder->pivotPos);
        free(decoder->isPivot);
        free(decoder->decodeMatrix);
        free(decoder); 
        return 0;
    }

    decoder->decodedToRow = malloc(sizeof(ffe) * max_packets);

    if (!decoder->decodedToRow) {
	free(decoder->decoded);
	free(decoder->pivotPos);
        free(decoder->isPivot);
        free(decoder->decodeMatrix);
        free(decoder); 
        return 0;
    }

    decoder->decodedCount = 0;

    memset((ffe *) decoder->decodeMatrix, 0, sizeof(ffe) * max_packets * max_packets * 2);
    memset(decoder->isPivot, 0, sizeof(ffe) * max_packets);
    memset(decoder->pivotPos, 0, sizeof(int) * max_packets);
    memset(decoder->decoded, 0, sizeof(ffe) * max_packets);

    decoder->maxPackets = max_packets;

    decoder->packetCount = 0;

    return decoder;

}

void destroy_coding_vector_decoder(coding_vector_decoder_t *self) {

    free(self->decodeMatrix);

    free(self->decoded);
    free(self->isPivot);
    free(self->pivotPos);
    free(self->decodedToRow);

    free(self);
    
}

int cvd_add_vector(coding_vector_decoder_t *self, ffe *v) {
    
    ffe *mul, *sub, *div, *decodeMatrix;
    int *pivotPos;
    char *isPivot;
    int size, totalSize, packetCount, offset;
    int i,j;
    
    decodeMatrix = self->decodeMatrix;
    packetCount = self->packetCount;
    pivotPos = self->pivotPos;
    isPivot = self->isPivot;

    size = self->maxPackets;
    totalSize = self->maxPackets * 2;
    offset = packetCount * totalSize;
    
   

    memcpy(decodeMatrix + offset, v, size);
    decodeMatrix[offset+size+packetCount] = 1;

    /* simplify the new packet */

    /* zeros before */
    for ( i = 0 ; i < packetCount ; i++)  {

            ffe m = decodeMatrix[offset+pivotPos[i]];

            if (m == 0) continue;

            for ( j = 0 ; j < totalSize ; j++) {
                    ffe val = decodeMatrix[offset+j];
                    ffe val2 = decodeMatrix[i*totalSize+j];

                    ffe ret = val ^ mul_table[(val2 << 8) + m];
                    decodeMatrix[offset+j] = ret;
            }

    }

    /* find pivot on the line */
    int pivot = -1;
    for ( i = 0 ; i < size ; i++) {
        if (isPivot[i]) continue;
        if (decodeMatrix[offset +i] != 0) {
                pivotPos[packetCount] = i;
                isPivot[i] = 1;
                pivot = i;
                break;
        }
    }

    /* packet is li */	
    if (pivot == -1 ) {
	
	// clean the decoding matrix row
	memset(decodeMatrix + offset, 0, totalSize);

	return 0;

    /* if the packet is li */
    } else {
	    /* divide the line */

	    if ( decodeMatrix[offset+pivot] != 1 ) {
		ffe pval = decodeMatrix[offset+pivot];

		for ( j = 0 ; j < totalSize ; j++) {
			ffe val = decodeMatrix[offset+j];
			decodeMatrix[offset+j] = div_table[(val << 8) + pval];
		}

	    }

	    /* zero the column above the pivot */
	    for (  i = 0 ; i < packetCount ; i++ ) {
		    ffe m = decodeMatrix[i*totalSize+pivot];

		    if (m == 0) continue;

		    for ( j = 0 ; j < totalSize ; j++) {

			    ffe val2 = decodeMatrix[offset+j];
			    ffe val = decodeMatrix[i*totalSize+j];                    

			    decodeMatrix[i*totalSize+j] = val ^ mul_table[(val2<<8) + m];
		    }

	    }

	    self->packetCount++;

	    /* look for decodable blocks */

	    for (  i = 0; i < self->packetCount ; i++) {
		int pos = -1;

		/* skip if the line is marked decoded */
		if (self->decoded[i]) continue;

		for (  j = 0 ; j < size ; j++) {

		    if (decodeMatrix[i * totalSize + j] != 0 && pos != -1) {
			pos = -1;
			break;
		    } else if (decodeMatrix[i * totalSize + j] != 0) pos = j;
		}

		if ( pos >= 0) {
		    self->decodedToRow[self->decodedCount] = i;
		    self->decodedCount++;
		    self->decoded[i] = 1;
		}
	    }

	    return -1;
    }

}

int cvd_decoded_coordinates_get_count(p_coding_vector_decoder_t self) {
	return self->decodedCount;
}

int cvd_decoded_coordinates_get_coordinate(p_coding_vector_decoder_t self, int pos) {
	return self->pivotPos[self->decodedToRow[pos]];
}

ffe* cvd_decoded_coordinates_get_coefficients(p_coding_vector_decoder_t self, int pos) {
	return self->decodeMatrix + self->decodedToRow[pos] * self->maxPackets * 2 + self->maxPackets;
}
