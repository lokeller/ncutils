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

#ifndef _NCUTILS_H
#define	_NCUTILS_H

#ifdef	__cplusplus
extern "C" {
#endif

typedef unsigned char ffe;

/* type declarations */
typedef struct _packet_decoder_t* p_packet_decoder_t;
typedef struct _coding_vector_decoder_t* p_coding_vector_decoder_t;


/*******************************************************************************
 *
 * Packet decoder: this object receives as input coded packets and as soon
 * as it is possible it outputs the original uncoded packets.
 *
 * ****************************************************************************/

/* Creates a packet decoder that operates with coded packets defined over the finite
 * field ff, with coding coefficient vectors of length max_packets and with payloads
 * of lenght payload_length_bytes bytes */
p_packet_decoder_t create_packet_decoder(int max_packets, int payload_length_bytes) ;

/* Destroys a packet decoder and releases the associated resources */
void destroy_packet_decoder(p_packet_decoder_t this) ;

/* Returns the number of packets that were decoded */
int pd_decoded_packets_get_count(p_packet_decoder_t this);

/* Returns the buffer holding the specified packet in the list of packets that were decoded */
ffe* pd_decoded_packets_get_packet(p_packet_decoder_t this, int idx);

/* Returns the id of the the specified packet in the list of packets that were decoded */
int pd_decoded_packets_get_id(p_packet_decoder_t this, int idx);

/*
 * Adds a new coded packet to the specified packet decoder. It the packet is a linear
 * combination of the packets previously added the function will return 0 otherwise
 * it will return -1.  */
int pd_add_packet(p_packet_decoder_t this, ffe* packet) ;

/*
 * Returns a random vector from the subspace currently stored in the packet decoder */
void pd_get_packet(p_packet_decoder_t this, ffe* packet);


/*******************************************************************************
 *
 * Coding vector decoder: this object receives as input coding vectors and as soon
 * as it is possible it outputs the coefficients that must be used to obtain from
 * the packets corresponding the the received coding vector uncoded packets by
 * linear combination.
 *
 * ****************************************************************************/

/* Creates a new coding vector decoder that operates on coding vectors of length
 * max_packets defined over the field ff
 */
p_coding_vector_decoder_t create_coding_vector_decoder(int max_packets) ;

/* Destroys the specified coding vector decoder and releases all the associated
 * resources.
 */
void destroy_coding_vector_decoder(p_coding_vector_decoder_t self) ;

/*
 * Adds a coding vector to a specified coding vector decoder. If the vector
 * specified is lineraly dependent from the vectors already added the function
 * returns 0 otherwise it returns -1
 */ 
int cvd_add_vector(p_coding_vector_decoder_t self, ffe* buffer ) ;

/* Returns the number of decoded coordinates contained */
int cvd_decoded_coordinates_get_count(p_coding_vector_decoder_t self);

/* Returns the index of the block at the specified position */
int cvd_decoded_coordinates_get_coordinate(p_coding_vector_decoder_t self, int pos);

/* Returns the coefficients that have to be used to decode the block at the specified position */
ffe* cvd_decoded_coordinates_get_coefficients(p_coding_vector_decoder_t self, int pos);


#ifdef	__cplusplus
}
#endif

#endif	/* _NCUTILS_H */
