package com.nedap.university.packet;

public class Header {
  private byte[] byteArray;
  // TODO: zitten al in DatagramPacket header.
  // PacketDataSize
  // OffsetPointer

  // ARQ flags
  // ACK/SEQ nums

  // Checksum

  // File Dir
  // FileName

  public Header(int headerSize) {
    byteArray = new byte[headerSize];
  }

  public byte[] getByteArray() {
    return byteArray;
  }

  public void setPacketDataSize(int packetDataSize) {
    byteArray[0] = (byte) (packetDataSize >>> 8 & 0xff);
    byteArray[1] = (byte) (packetDataSize & 0xff);
  }

  public void setOffsetPointer(int offsetPointer) {
    byteArray[2] = (byte) (offsetPointer >>> 8 & 0xff);
    byteArray[3] = (byte) (offsetPointer & 0xff);
  }

  public void setFlag(FLAG flag) {
    switch(flag) {
      case DATA:
        //System.out.println("set dataFlag");
        break;
      case ACK:
        //System.out.println("set ackFlag");
        break;
      default:
        //System.out.println("Invalid flag input.");
        break;
    }
  }

  public enum FLAG {
    HELLO, DATA, GET, LIST, ACK, FIN;
  }
}
