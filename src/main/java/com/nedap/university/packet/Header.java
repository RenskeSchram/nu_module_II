package com.nedap.university.packet;

import com.nedap.university.utils.Parameters;

public class Header {
  private byte[] byteArray;

  public Header() {
    byteArray = new byte[Parameters.HEADER_SIZE];
  }

  public Header(Payload payload) {
    byteArray = new byte[Parameters.HEADER_SIZE];
    setWithDataPayload(payload);
  }

  public int getSize() {
    return byteArray.length;
  }

  public void setByteArray(byte[] byteArray) {
    if (this.byteArray.length == byteArray.length) {
      this.byteArray = byteArray;
    } else {
      System.out.println("Invalid provided Header byte-array.");
    }
  }

  public byte[] getByteArray() {
    return byteArray;
  }

  public void setPayloadDataSize(int payloadDataSize) {
    byteArray[0] = (byte) (payloadDataSize >>> 8 & 0xff);
    byteArray[1] = (byte) (payloadDataSize & 0xff);
  }

  public int getPayloadDataSize() {
    return ((byteArray[0] & 0xFF) << 8) | (byteArray[1] & 0xFF);
  }

  public void setOffsetPointer(int offsetPointer) {
    byteArray[2] = (byte) (offsetPointer >>> 8 & 0xff);
    byteArray[3] = (byte) (offsetPointer & 0xff);
  }

  public int getOffsetPointer() {
    return ((byteArray[2] & 0xFF) << 8) | (byteArray[3] & 0xFF);
  }

  public void setAckNr(int ACK) {
    byteArray[4] = (byte) (ACK >>> 8 & 0xff);
    byteArray[5] = (byte) (ACK & 0xff);
  }
  public int getAckNr() {
    return ((byteArray[4] & 0xFF) << 8) | (byteArray[5] & 0xFF);
  }

  public void setSeqNr(int SEQ) {
    byteArray[6] = (byte) (SEQ >>> 8 & 0xff);
    byteArray[7] = (byte) (SEQ & 0xff);
  }

  public int getSeqNr() {
    return ((byteArray[6] & 0xFF) << 8) | (byteArray[7] & 0xFF);

  }

  public void setChecksum(int checksum) {
    byteArray[8] = (byte) (checksum >>> 8 & 0xff);
    byteArray[9] = (byte) (checksum & 0xff);
  }

  public int getChecksum() {
    return ((byteArray[8] & 0xFF) << 8) | (byteArray[9] & 0xFF);

  }

  public void setFlagByte(byte flagByte) {
    byteArray[10] = flagByte;
  }

  public byte getFlagByte() {
    return byteArray[10];
  }

  public void setFlag(FLAG flag) {
    byteArray[10] |= (byte) (1 << flag.byteLocation);
  }

  public boolean isFlagSet(FLAG flag) {
    return (byteArray[10] & (1 << flag.byteLocation)) != 0;
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
