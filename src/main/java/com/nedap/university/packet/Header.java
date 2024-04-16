package com.nedap.university.packet;

public class Header {
  private static final int SIZE = 12;
  private byte[] byteArray;

  private int payloadDataSize;
  private int offsetPointer;

  private int ACK;
  private int SEQ;

  private int checksum;

  private byte flags;

  public Header() {
    byteArray = new byte[SIZE];
  }

  public Header(Payload payload) {
    byteArray = new byte[SIZE];
    setWithDataPayload(payload);
  }

  public static int getSize() {
    return SIZE;
  }

  public byte[] getByteArray() {
    return byteArray;
  }

  public void setPayloadDataSize(int payloadDataSize) {
    this.payloadDataSize = payloadDataSize;
    byteArray[0] = (byte) (payloadDataSize >>> 8 & 0xff);
    byteArray[1] = (byte) (payloadDataSize & 0xff);
  }

  public int getPayloadDataSize() {
    return payloadDataSize;
  }

  public void setOffsetPointer(int offsetPointer) {
    this.offsetPointer = offsetPointer;
    byteArray[2] = (byte) (offsetPointer >>> 8 & 0xff);
    byteArray[3] = (byte) (offsetPointer & 0xff);
  }

  public int getOffsetPointer() {
    return offsetPointer;
  }

  public void setAckNr(int ACK) {
    this.ACK = ACK;
    byteArray[4] = (byte) (ACK >>> 8 & 0xff);
    byteArray[5] = (byte) (ACK & 0xff);
  }
  public int getAckNr() {
    return ACK;
  }

  public void setSeqNr(int SEQ) {
    this.SEQ = SEQ;
    byteArray[6] = (byte) (SEQ >>> 8 & 0xff);
    byteArray[7] = (byte) (SEQ & 0xff);
  }

  public int getSeqNr() {
    return SEQ;
  }

  public void setChecksum(int checksum) {
    this.checksum = checksum;
    byteArray[8] = (byte) (checksum >>> 8 & 0xff);
    byteArray[9] = (byte) (checksum & 0xff);
  }

  public int getChecksum() {
    return checksum;
  }

  public void setFlags(byte flags) {
    this.flags = flags;
  }

  public byte getFlags() {
    return flags;
  }

  public void setFlag(FLAG flag) {
    flags |= (byte) (1 << flag.byteLocation);
    byteArray[10] |= (byte) (1 << flag.byteLocation);
  }

  public boolean isFlagSet(FLAG flag) {
    return (flags & (1 << flag.byteLocation)) != 0;
  }

  public void setWithDataPayload(Payload payload) {
    setPayloadDataSize(payload.getByteArray().length);
    setOffsetPointer(payload.getOffsetPointer());

    setFlag(FLAG.DATA);

    if (payload.isFinalPacket) {
      setFlag(FLAG.FIN);
    }
  }

  public enum FLAG {

    HELLO(0), DATA(1), GET(2), LIST(3), ACK(4), FIN(5);

    private final int byteLocation;

    FLAG(int byteLocation) {
      this.byteLocation = byteLocation;
    }

  }
}
