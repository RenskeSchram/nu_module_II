package com.nedap.university.packet;

public class Payload {
  private final byte[] byteArray;
  private final int offsetPointer;
  public final boolean isFinalPacket;

  public Payload(byte[] byteArray, int offsetPointer, boolean isFinalPacket) {
    this.byteArray = byteArray;
    this.offsetPointer = offsetPointer;
    this.isFinalPacket = isFinalPacket;
  }

  public byte[] getByteArray() {
    return byteArray;
  }

  public int getOffsetPointer() {
    return offsetPointer;
  }

  public int getSize() {
    return byteArray.length;
  }
}
