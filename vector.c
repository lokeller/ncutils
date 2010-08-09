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
#include <malloc.h>
#include <memory.h>


vector_t* create_vector(int length, finite_field_t* ff) {

    vector_t* this = (vector_t *) malloc(sizeof(vector_t));

    this->ff = ff; 
    this->coordinates = malloc(sizeof(int) * length);
    this->length = length;

    vector_set_to_zero(this);

    return this;
}

void destroy_vector(vector_t *this) {
    free(this->coordinates);
    free(this);
}

void vector_set_to_zero(vector_t* this) {
    memset(this->coordinates, 0, this->length * sizeof(int));
}

vector_t *vector_copy(vector_t* this) {

    vector_t *dest;   

    dest = create_vector(this->length, this->ff);

    memcpy(dest->coordinates, this->coordinates, this->length * sizeof(int));

    return dest;

}

vector_t *vector_add(vector_t* this, vector_t* other) {

    vector_t* dest;
    int i;

    assert(this->ff->q == other->ff->q);
    assert(this->length == other->length);

    dest = create_vector(this->length, this->ff);

    for ( i = 0 ; i < this->length ; i++ ) {
        dest->coordinates[i] = this->ff->sum[this->coordinates[i] * this->ff->q + other->coordinates[i]];
    }

    return dest;

}

vector_t *vector_scalar_multiply(vector_t* this, int c) {

    int i;
    vector_t* dest;       

    assert( c >= 0 && c < this->ff->q);

    dest = create_vector(this->length, this->ff);

    for ( i = 0 ; i < this->length ; i++ ) {
        dest->coordinates[i] = this->ff->mul[this->coordinates[i] * this->ff->q + c];
    }

    return dest;
}

void vector_to_string(vector_t* this, char* buffer, int len) {

    int i;
    char tmp[10];

    buffer[0] = 0;

    for (i = 0 ; i < this->length ; i++) {
        if ( this->ff->q < 10) {
            snprintf(tmp, 10, "%d ", this->coordinates[i]);
        } else if (this->ff->q < 100) {
            snprintf(tmp, 10, "%02d ", this->coordinates[i]);
        } else {
            snprintf(tmp, 10, "%03d ", this->coordinates[i]);
        }
        
        if ( strlen(buffer) + strlen(tmp) < len) {
            strcat(buffer, tmp);
        }
    }

}

int vector_get_length(vector_t *this) {
    return this->length;
}

int vector_get_coordinate(vector_t *this, int coordinate) {
    assert(coordinate < this->length && coordinate > -1);
    return this->coordinates[coordinate];
}

void vector_set_coordinate(vector_t *this, int coordinate, int value) {
    assert(coordinate < this->length && coordinate > -1);
    this->coordinates[coordinate] = value;
}

finite_field_t *vector_get_finite_field(vector_t *this) {
    return this->ff;
}