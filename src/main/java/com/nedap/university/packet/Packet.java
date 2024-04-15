package com.nedap.university.packet;

import java.nio.ByteBuffer;

public class Packet {

  private final Header header;
  private final Payload payload;
  private final int size;

  public Packet(Header header, Payload payload) {
    this.header = header;
    this.payload = payload;
    this.size = Header.getSize() + payload.getSize();
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

  public byte[] getByteArray(){
    byte[] byteArray = new byte[header.getByteArray().length + payload.getByteArray().length];
    ByteBuffer buffer = ByteBuffer.wrap(byteArray);
    buffer.put(header.getByteArray());
    buffer.put(payload.getByteArray());
    return buffer.array();
  }
}
