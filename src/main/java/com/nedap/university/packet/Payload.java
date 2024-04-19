package com.nedap.university.packet;

public class Payload {
  private byte[] byteArray;
  private final int offsetPointer;
  public final boolean isFinalPacket;

  public Payload(String src_dir, String dst_dir, long fileSize, boolean isFinalPacket) {
    byte[] fileSrcBytes = src_dir.getBytes();
    byte[] fileDstBytes = dst_dir.getBytes();
    byte[] fileSizeBytes = String.valueOf(fileSize).getBytes();
    byte[] payload = new byte[fileSrcBytes.length + 1 + fileDstBytes.length  + 1 + fileSizeBytes.length];

    System.arraycopy(fileSrcBytes, 0, payload, 0, fileSrcBytes.length);
    payload[fileSrcBytes.length] = '~';
    System.arraycopy(fileDstBytes, 0, payload, fileSrcBytes.length + 1, fileDstBytes.length);
    payload[fileSrcBytes.length + 1 + fileDstBytes.length] = '~';
    System.arraycopy(fileSizeBytes, 0, payload, fileSrcBytes.length + 1 + fileDstBytes.length + 1, fileSizeBytes.length);

    byteArray = payload;
    offsetPointer = 0;
    this.isFinalPacket = isFinalPacket;
  }

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

  public String[] getStringArray() {
    return new String(byteArray).split("~");
  }

  public String getSrcPath() {
    return getStringArray()[0];
  }

  public String getDstPath() {
    return getStringArray()[1];
  }

  public int getFileSize() {
    return Integer.parseInt(getStringArray()[2]);
  }
}
