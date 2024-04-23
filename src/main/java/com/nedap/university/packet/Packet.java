package com.nedap.university.packet;

import static com.nedap.university.utils.Parameters.HEADER_SIZE;

import com.nedap.university.packet.Header.FLAG;
import com.nedap.university.utils.Checksum;
import com.nedap.university.utils.Parameters;
import java.nio.ByteBuffer;

/**
 * Packet object used troughout the differnt layers for processing, has a header and payload.
 */

public class Packet {

  private final Header header;
  private final Payload payload;
  private final int size;

  /**
   * Constructor for a Packet from a Header and Payload.
   * @param header header of the packet
   * @param payload payload of the packet
   */
  public Packet(Header header, Payload payload) {
    this.header = header;
    this.payload = payload;
    this.size = Parameters.HEADER_SIZE + payload.getSize();
  }

  /**
   * Constructor for a Packet from a (received) byte array according to the set protocol.
   * @param byteArray input array to construct Packet from.
   */
  public Packet(byte[] byteArray) {
    header = new Header();
    byte[] headerByteArray = new byte[HEADER_SIZE];
    System.arraycopy(byteArray, 0, headerByteArray, 0, HEADER_SIZE);
    header.setByteArray(headerByteArray);

    byte[] payloadByteArray = new byte[header.getPayloadDataSize()];
    System.arraycopy(byteArray, HEADER_SIZE, payloadByteArray,
        0, header.getPayloadDataSize());
    payload = new Payload(payloadByteArray, header.getOffsetPointer(), header.isFlagSet(FLAG.FIN));
    size = Parameters.HEADER_SIZE + payload.getSize();
  }

  public Header getHeader() {
    return header;
  }

  public Payload getPayload() {
    return payload;
  }

  public int getSize() {
    return size;
  }

  public byte[] getByteArray() {
    byte[] byteArray = new byte[size];
    ByteBuffer buffer = ByteBuffer.wrap(byteArray);
    buffer.put(header.getByteArray());
    buffer.put(payload.getByteArray());
    return buffer.array();
  }
}