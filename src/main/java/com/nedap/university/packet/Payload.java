package com.nedap.university.packet;

public class Payload {
  private final byte[] byteArray;

  public Payload(byte[] byteArray) {
    this.byteArray = byteArray;
  }

  public byte[] getByteArray() {
    return byteArray;
  }
}
