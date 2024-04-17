package com.nedap.university.packet;

public class Payload {
  private byte[] byteArray;
  private final int offsetPointer;
  public final boolean isFinalPacket;

  public Payload(String file_dir, long fileSize, boolean isFinalPacket) {
    byte[] fileDirectoryBytes = file_dir.getBytes();
    byte[] fileSizeBytes = String.valueOf(fileSize).getBytes();
    byte[] payload = new byte[fileDirectoryBytes.length + fileSizeBytes.length + 1];

    System.arraycopy(fileDirectoryBytes, 0, payload, 0, fileDirectoryBytes.length);
    payload[fileDirectoryBytes.length] = '~';
    System.arraycopy(fileSizeBytes, 0, payload, fileDirectoryBytes.length + 1, fileSizeBytes.length);

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

  public static String[] getStringArray(byte[] payload) {
      return new String(payload).split("~");
  }
  public String[] getStringArray() {
    return new String(byteArray).split("~");
  }
}
