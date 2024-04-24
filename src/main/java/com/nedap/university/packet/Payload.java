package com.nedap.university.packet;

/**
 * Payload class, contains the payload of the Packet and the offset-pointer of the data within the
 * payload, as well as whether this is the final payload of the stack.
 */
public class Payload {

  private final byte[] byteArray;
  private final int offsetPointer;
  public final boolean isFinalPacket;

  /**
   * Constructor for Payload using a source and destination directory, filesize and boolean for
   * final packet for messages to notify the other host for upcoming packets/actions.
   *
   * @param src_dir       source directory of file
   * @param dst_dir       destination directory of file
   * @param fileSize      file size in bytes
   * @param isFinalPacket boolean if this is the final packet
   */
  public Payload(String src_dir, String dst_dir, long fileSize, boolean isFinalPacket) {
    byte[] fileSrcBytes = src_dir.getBytes();
    byte[] fileDstBytes = dst_dir.getBytes();
    byte[] fileSizeBytes = String.valueOf(fileSize).getBytes();
    byte[] payload = new byte[fileSrcBytes.length + 1 + fileDstBytes.length + 1
        + fileSizeBytes.length];

    System.arraycopy(fileSrcBytes, 0, payload, 0, fileSrcBytes.length);
    payload[fileSrcBytes.length] = '~';
    System.arraycopy(fileDstBytes, 0, payload, fileSrcBytes.length + 1, fileDstBytes.length);
    payload[fileSrcBytes.length + 1 + fileDstBytes.length] = '~';
    System.arraycopy(fileSizeBytes, 0, payload, fileSrcBytes.length + 1 + fileDstBytes.length + 1,
        fileSizeBytes.length);

    byteArray = payload;
    offsetPointer = 0;
    this.isFinalPacket = isFinalPacket;
  }

  /**
   * Constructor for Payload using a byte array, offset-pointer and boolean for final packet to
   * extract payload from received byte array.
   *
   * @param byteArray     byte array to construct Payload from
   * @param offsetPointer offset-pointer of data in the payload
   * @param isFinalPacket boolean if this is the final packet
   */
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

  /**
   * Spit String Payload using the tilde ~.
   *
   * @return array of different Strings in the payload
   */
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

  public boolean isFinalPacket() {
    return isFinalPacket;
  }
}
