package com.nedap.university.packet;

import com.nedap.university.utils.Parameters;

/**
 * Protocol header for UDP file exchange.
 *       __ Element __       __ Bytes __     __ Length __
 *        PayloadDataSize       [0-1]           2
 *        OffsetPointer         [2-3]           2
 *        Ack number            [4-5]           2
 *        Sequence number       [6-7]           2     (currently not in use)
 *        Checksum              [8-9]           2
 *        Flags                 [10]            1
 *        Unassigned            [11]            1     (currently not in use)
 */

public class Header {

  private byte[] byteArray;

  public Header() {
    byteArray = new byte[Parameters.HEADER_SIZE];
  }

  public Header(Payload payload) {
    byteArray = new byte[Parameters.HEADER_SIZE];
    setWithPayload(payload);
  }

  public void setByteArray(byte[] byteArray) {
    if (this.byteArray.length == byteArray.length) {
      this.byteArray = byteArray;
    } else {
      System.err.println("Invalid provided Header byte-array.");
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


  // SEQ num created for sliding window protocol, currently not in use.
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

  /**
   * Use payload to fill bytes of the header with the Payload size, offset-pointer and FIN flag.
   * @param payload payload of the packet.
   */
  public void setWithPayload(Payload payload) {
    setPayloadDataSize(payload.getByteArray().length);
    setOffsetPointer(payload.getOffsetPointer());

    if (payload.isFinalPacket) {
      setFlag(FLAG.FIN);
    }
  }

  public enum FLAG {
    HELLO(0), DATA(1), GET(2), LIST(3), ACK(4), FIN(5), ERROR(6), DELETE(6);

    private final int byteLocation;

    FLAG(int byteLocation) {
      this.byteLocation = byteLocation;
    }
  }
}
