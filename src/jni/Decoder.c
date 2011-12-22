#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include <ncutils.h>
#include <ncutils_priv.h>
#include <stdint.h>

typedef struct decoder_data { 
	p_packet_decoder_t decoder;
	int decoded_dimensions;
	int segment_len;
	int packets_per_segment;
	int packet_len;
} decoder_data_t;

JNIEXPORT jlong JNICALL Java_ch_epfl_arni_ncutils_impl_NativeDecoder_createDecoderNative (JNIEnv *env, jobject this, jint segmentLen, jint packetsPerSegment) {
	
	decoder_data_t *decoder_data;

	decoder_data = (decoder_data_t *) malloc(sizeof(decoder_data_t));

	if (!decoder_data) {
		jclass excCls = (*env)->FindClass(env,
				"java/lang/IllegalStateException");
		if (excCls != 0)
			(*env)->ThrowNew(env, excCls, "Cannot allocate memory for decoder data");
		return 0;
	}

	decoder_data->decoder = create_packet_decoder(packetsPerSegment, segmentLen / packetsPerSegment);

	if (!decoder_data->decoder) {
		jclass excCls = (*env)->FindClass(env,
				"java/lang/IllegalStateException");
		free(decoder_data);
		if (excCls != 0)
			(*env)->ThrowNew(env, excCls, "Cannot allocate memory for decoder");
		return 0;
	}

	decoder_data->decoded_dimensions = 0;
	decoder_data->segment_len = segmentLen;
	decoder_data->packets_per_segment = packetsPerSegment;
	decoder_data->packet_len = segmentLen / packetsPerSegment;

	return (jlong)  ( (intptr_t) decoder_data) ;

}

JNIEXPORT void JNICALL Java_ch_epfl_arni_ncutils_impl_NativeDecoder_getSegmentNative (JNIEnv *env, jobject this, jlong handle, jbyteArray segment, jint offset) {

	int i;
	char* data;
	int offset2;
	decoder_data_t *decoder_data = (decoder_data_t*) (intptr_t) handle;

	if (!handle) return;

	for ( i = 0 ; i < pd_decoded_packets_get_count(decoder_data->decoder) ; i++) {
		offset2 = pd_decoded_packets_get_id(decoder_data->decoder, i) * decoder_data->packet_len;
		data = pd_decoded_packets_get_packet(decoder_data->decoder, i);
		(*env)->SetByteArrayRegion(env, segment, offset + offset2, decoder_data->packet_len, data);
	}

}

JNIEXPORT jboolean JNICALL Java_ch_epfl_arni_ncutils_impl_NativeDecoder_addPacketNative (JNIEnv *env, jobject this, jlong handle, jbyteArray packet, jint offset) {
	jboolean innovative;

	ffe* coded_packet;
	decoder_data_t *decoder_data = (decoder_data_t *) (intptr_t) handle;

	if (!handle) return;

	coded_packet = (ffe *) malloc( sizeof(ffe) * (decoder_data->packet_len + decoder_data->packets_per_segment));

	if (!coded_packet) {
		jclass excCls = (*env)->FindClass(env,
				"java/lang/IllegalStateException");
		if (excCls != 0)
			(*env)->ThrowNew(env, excCls, "Cannot allocate memory for coded packet");
		return 0;
	}

	(*env)->GetByteArrayRegion(env, packet, offset, decoder_data->packets_per_segment + decoder_data->packet_len, coded_packet );

	innovative = pd_add_packet(decoder_data->decoder, coded_packet);
	
	free(coded_packet);

	return innovative;
}

JNIEXPORT void JNICALL Java_ch_epfl_arni_ncutils_impl_NativeDecoder_getPacketNative
  (JNIEnv *env, jobject this, jlong handle, jbyteArray packet, jint offset) {
	
	ffe* coded_packet;
	decoder_data_t *decoder_data = (decoder_data_t*) (intptr_t)  handle;
        
        if (!handle) return;

	coded_packet = (ffe *) malloc( sizeof(ffe) * (decoder_data->packet_len + decoder_data->packets_per_segment));

	if (!coded_packet) {
		jclass excCls = (*env)->FindClass(env,
				"java/lang/IllegalStateException");
		if (excCls != 0)
			(*env)->ThrowNew(env, excCls, "Cannot allocate memory for coded packet");
		return;
	}

	pd_get_packet(decoder_data->decoder, coded_packet);

	(*env)->SetByteArrayRegion(env, packet, offset, decoder_data->packets_per_segment + decoder_data->packet_len, coded_packet);
	
}


JNIEXPORT void JNICALL Java_ch_epfl_arni_ncutils_impl_NativeDecoder_disposeNative (JNIEnv *env, jobject this, jlong handle) {

	decoder_data_t *decoder_data = (decoder_data_t*) (intptr_t)  handle;

	if (!handle) return;

	destroy_packet_decoder(decoder_data->decoder);
	decoder_data->decoder = 0;
	free(decoder_data);
}

