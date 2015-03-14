# Documentation #

## Description of the examples ##

The source code contains two examples in the directory `src/examples`.

The first example `BlockLevelExample` shows how to use NCUtils to implement a classic random network coding network. In this example the data to be sent is splitted in blocks (represented in the code by the class `UncodedPacket`). Blocks are linearly combined and transmitted through the network. Linear combinations of blocks are represented in the code by the `CodedPacket` class. Each coded packet contains a coding vector that describes how the linear combination has been built and the linear combination itself. Relays in the network that receive the coded packets create other random linear combinations by randomly combining `CodedPacket`s. At the destination the received linear combinations are fed to a `PacketDecoder` that will decode them to the original `UncodedPacket`s.

The second example `CodingVectorExample` is similar to the first but in this case only the coding vectors are processed.

### Block level coding example ###

The first operation is to create a `FiniteField` object that will be used to perform computations. By default the code uses the finite field of cardinality 16.

```
FiniteField ff = FiniteField.getDefaultFiniteField();
```

Then we define the parameters of our example:

```
int blockNumber = 10;
int payloadLen = 10;
int payloadLenCoeffs = 20;
```

The variable `blockNumber` is the number of blocks in which the data to be sent is divided, this is usually called _generation size_ in the network coding literature. The variable `payloadLen` is the length of each block (in bytes). The variable `payloadLenCoeffs` is the length of the finite field vectors that represent the blocks. Since we are using a finite field of size 16 each byte of payload can be represented with two field elements, therefore the number of coefficients of a vector will be twice the length of the payload in bytes.

The next step is to create the blocks that will be sent. Each block is represented by an `UncodedPacket`. Each uncoded packet has an ID that is used to identify it at the destination (to reorder the blocks that gets decoded).

```
UncodedPacket[] inputPackets = new UncodedPacket[blockNumber];
for ( int i = 0 ; i < blockNumber ; i++) {
    byte[] payload = new byte[payloadLen];
    Arrays.fill(payload, (byte) (0XA0 +  i));
    inputPackets[i] = new UncodedPacket(i, payload);
}
```

The next step is to create `CodedPacket`s that can be transmitted and combined. A coded packet is composed by a coding vector that describes which uncoded packets have been combined to create it. At this stage we create coded packets that contain only a single uncoded packet. The coding vectors will therefore consist of zeros except for one coordinate where they will contain 1.

```
CodedPacket[] codewords = new CodedPacket[blockNumber];

for ( int i = 0 ; i < blockNumber ; i++) {
    codewords[i] = new CodedPacket( inputPackets[i], blockNumber, ff);
}
```

Now we emulate what a relay node would do if it receives the coded packets that were created in the previous step. It will linearly combine them using random coefficients. We create as many random linear combinations as uncoded packets to make sure that with high probability the resulting coded packets are decodable.

```
CodedPacket[] networkOutput = new CodedPacket[blockNumber];

Random r = new Random(2131231);

for ( int i = 0 ; i < blockNumber ; i++) {
   networkOutput[i] = new CodedPacket(blockNumber, payloadLen, ff);

   for ( int j = 0 ; j < blockNumber ; j++) {
      int x = r.nextInt(ff.getCardinality());                
      CodedPacket copy = codewords[j].scalarMultiply(x);
      networkOutput[i] = networkOutput[i].add(copy);             
   }
}
```

Finally we can decode the random linear combinations using a `PacketDecoder`. This task would is performed at the destination.

```
PacketDecoder decoder = new PacketDecoder(ff, blockNumber, payloadLen);

for ( int i = 0; i < blockNumber ; i++) {
  Vector<UncodedPacket> packets = decoder.addPacket(networkOutput[i]);
}

```

Every time a packet is added the decoder tries to decode as much as it can and returns the blocks it was able to decode.