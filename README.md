## Description ##
**WARNING: the code released has still to be considered experimental and may contain serious bugs. Use it with caution.**

This project is composed by three libraries:

  * ncutils-java and ncutils-c: these two libraries provide slow but flexible functions to implement network coding ideas both in pure Java and C.
  * ncutils-codec: this library provides an implementation of random network coding over F2^8. This library is faster than the above but is less flexible. It includes native code to accelerate both encoding and decoding.

### ncutils-java and ncutils-c ###

These libraries provide a set of functions that can be used to implement network coding techniques in applications:

  * Implementation of finite field operations over GF(p) where p is a prime and GF(2^m)
  * Coding vector decoding for linear network codes (matrix inversion over finite fields)
  * Packet decoding for linear network codes: given a set of linearly coded packets reconstruct the original uncoded packets

An example of the usage of the API is provided [here](http://code.google.com/p/ncutils/wiki/Documentation?tm=6).

### ncutils-codec ###

This Java library can be used to implement support of random network coding in a Java application. The API is very simple. If the platform is supported the library uses JNI and a native library to accelerate encoding and decoding otherwise it falls back to a pure Java implementation. The binary distributed on this website includes already compiled native code for Linux i386 and amd64. The native code should be easily compiled on other platforms too (tested on Android ARM).

An example of the usage of the library can be found [here](http://code.google.com/p/ncutils/source/browse/examples/java/Example.java?repo=ncutils-codec)

## Contributions are welcome ##

The performance of the code has not been fully optimized, any improvement is welcome. If you have any other comment about how to make this project more useful feel free to contact the project maintainer.

If you are interested in contributing some code of general interest feel free to contact the project maintainer. Keep however in mind that the code should be reasonably well documented in order to be useful to others.