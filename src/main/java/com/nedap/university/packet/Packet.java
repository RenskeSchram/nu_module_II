package com.nedap.university.packet;

import java.nio.ByteBuffer;

public class Packet {

  private Header header;
  private Payload payload;

  public byte[] getByteArray(){
    byte[] byteArray = new byte[header.getByteArray().length + payload.getByteArray().length];
    ByteBuffer buffer = ByteBuffer.wrap(byteArray);
    buffer.put(header.getByteArray());
    buffer.put(payload.getByteArray());
    return buffer.array();
  }

  public void setHeader(Header header) {
    this.header = header;
  }

  public void setPayload(byte[] payload) {
    this.payload = new Payload(payload);
  }
}
