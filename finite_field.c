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
#include <assert.h>
#include <math.h>
#include <stdlib.h>

finite_field_t *create_extension_field(int b, int m) {

    int primitive_polynomial[] = { 3, 7, 11, 19, 37, 67, 137,
                                        285, 529,1033,2053,4179,
                                        8219,17475, 32771, 69643 };
    int c,i,j,k;
    int q;
    finite_field_t *this;

    assert(b > 1 && m > 0);
    assert(b == 2 && m <= 16);

    this = (finite_field_t*) malloc(sizeof(finite_field_t));

    this->q = (int) pow(b,m);

    q = this->q;

    this->sum = (int *) malloc(sizeof(int) * this->q * this->q);
    this->mul = (int *) malloc(sizeof(int) * this->q * this->q);
    this->div = (int *) malloc(sizeof(int) * this->q * this->q);
    this->sub = (int *) malloc(sizeof(int) * this->q * this->q);

    c = primitive_polynomial[m - 1] - ( 1 << m );

    for (i = 0 ; i < q ; i++) {
        for (j = 0 ; j < q ; j++) {

            int a = i;
            int b = j;
            int p = 0;

            this->sum[i*q + j] = i ^ j;
            this->sub[i*q + j] = i ^ j;


            /* paesant's algorithm*/
            for ( k = 0 ; k < m ; k++) {
                if ( (b & 0x1) == 1) {
                    p = p ^ a;
                }

                int r = (a & (0x1 << (m-1))) > 0;
                a = (a << 1) % q;
                if (r) {
                    a = a ^ c;
                }
                b = b >> 1;
            }

            this->mul[i*q+j] = p;

        }
    }

    for (i = 0 ; i < q ; i++) {
        for (j = 0 ; j < q ; j++) {

            this->div[this->mul[i*q + j]*q+i] = j;
            this->div[this->mul[i*q + j]*q+j] = i;

        }
    }

    return this;

}

void destroy_finite_field(finite_field_t *this) {

    free(this->div);
    free(this->sub);
    free(this->sum);
    free(this->mul);

    free(this);
    
}

void ff_bytes_to_vector(finite_field_t *this, char* src, int len, vector_t* dest) {
    int i;

    assert(dest->length == ff_coordinates_to_bytes(this, len));

    switch (this->q) {
        case 256:

            for (i = 0 ; i < dest->length; i++) {
                dest->coordinates[i] =  0xFF & ((int) src[i]);
            }

            break;
        case 16:

            for (i = 0 ; i < dest->length; i++) {
                dest->coordinates[2*i] = 0x0F & ((int) src[i]);
                dest->coordinates[2*i+1] = (0xF0 & ((int) src[i])) >> 4;
            }

            break;

        default:
            assert(0);
    }


}

void ff_vector_to_bytes(finite_field_t *this, vector_t *vector, char *dest, int len) {

    int i;

    assert(len >= ff_bytes_to_coordinates(this, len));

    switch (this->q) {
        case 256:

            for (i = 0 ; i < vector->length; i++) {
                dest[i] = (char) vector->coordinates[i];
            }

            break;

        case 16:

            for (i = 0 ; i < vector->length; i++) {
                dest[i] = (char) ( (vector->coordinates[2*i+1] << 4) + vector->coordinates[2*i]) ;
            }

            break;
        default:
            assert(0);
    }
}

int ff_coordinates_to_bytes(finite_field_t* this, int coordinatesCount) {

     switch (this->q) {
        case 256:

            return coordinatesCount;

        case 16:

            return (coordinatesCount + 1) / 2;

        default:
            assert(0);
    }
}


int ff_bytes_to_coordinates(finite_field_t* this, int bytesLength) {
     switch (this->q) {
        case 256:

            return bytesLength;

        case 16:

            return bytesLength * 2;

        default:
            assert(0);
    }

}
