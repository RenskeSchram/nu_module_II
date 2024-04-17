package com.nedap.university.packet;

import com.nedap.university.utils.Parameters;
import java.nio.ByteBuffer;

public class Packet {

  private final Header header;
  private final Payload payload;
  private final int size;

  public Packet(Header header, Payload payload) {
    this.header = header;
    this.payload = payload;
    this.size = Parameters.HEADER_SIZE + payload.getSize();
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
}
