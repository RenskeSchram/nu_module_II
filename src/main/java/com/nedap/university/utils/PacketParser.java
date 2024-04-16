package com.nedap.university.utils;

import com.nedap.university.packet.Header;
import com.nedap.university.packet.Header.FLAG;
import com.nedap.university.packet.Packet;
import com.nedap.university.packet.Payload;
import java.nio.ByteBuffer;

public class PacketParser {

  public static Packet byteArrayToPacket(byte[] data) {
    Header header = new Header();
    header.setPayloadDataSize(((data[1] & 0xFF) << 8) | (data[2] & 0xFF));
    header.setOffsetPointer(((data[2] & 0xFF) << 8) | (data[3] & 0xFF));
    header.setAckNr(((data[4] & 0xFF) << 8) | (data[5] & 0xFF));
    header.setSeqNr(((data[6] & 0xFF) << 8) | (data[7] & 0xFF));
    header.setChecksum(((data[8] & 0xFF) << 8) | (data[9] & 0xFF));
    header.setFlags(data[10]);

    byte[] payloadByteArray = new byte[data.length - Header.getSize()];
    System.arraycopy(data, Header.getSize(), payloadByteArray,
        0, data.length - Header.getSize());

    Payload payload = new Payload(payloadByteArray, header.getOffsetPointer(), header.isFlagSet(FLAG.FIN));

    return new Packet(header, payload);
  }

  public static byte[] packetToByteArray(Packet packet) {
    byte[] headerByteArray = new byte[Parameters.HEADER_SIZE];

    headerByteArray[0] = (byte) (packet.getHeader().getPayloadDataSize() >>> 8 & 0xff);
    headerByteArray[1] = (byte) (packet.getHeader().getPayloadDataSize() & 0xff);

    headerByteArray[2] = (byte) (packet.getHeader().getOffsetPointer() >>> 8 & 0xff);
    headerByteArray[3] = (byte) (packet.getHeader().getOffsetPointer() & 0xff);

    headerByteArray[4] = (byte) (packet.getHeader().getAckNr() >>> 8 & 0xff);
    headerByteArray[5] = (byte) (packet.getHeader().getAckNr() & 0xff);

    headerByteArray[6] = (byte) (packet.getHeader().getSeqNr() >>> 8 & 0xff);
    headerByteArray[7] = (byte) (packet.getHeader().getSeqNr() & 0xff);

    headerByteArray[8] = (byte) (packet.getHeader().getChecksum() >>> 8 & 0xff);
    headerByteArray[9] = (byte) (packet.getHeader().getChecksum() & 0xff);

    headerByteArray[10] = packet.getHeader().getFlags();

    byte[] byteArray = new byte[packet.getSize()];
    ByteBuffer buffer = ByteBuffer.wrap(byteArray);
    buffer.put(headerByteArray);
    buffer.put(packet.getPayload().getByteArray());
    return buffer.array();
  }

  public static int getAckNr(byte[] byteArray) {
    return ((byteArray[4] & 0xFF) << 8) | (byteArray[5] & 0xFF);
  }

  public static byte getFlagByte(byte[] byteArray) {
    return byteArray[10];
  }

  public static int getOffsetPointer(byte[] byteArray) {
    return ((byteArray[2] & 0xFF) << 8) | (byteArray[3] & 0xFF);
  }

  public static byte[] getPayload(byte[] packet) {
    byte[] payload = new byte[packet.length - Header.getSize()];
    System.arraycopy(packet, Header.getSize(), payload,
        0, packet.length - Header.getSize());
    return payload;
  }

  public static String[] getStringPayload(byte[] packet) {
    String payload = new String(PacketParser.getPayload(packet));
    if (getFlagByte(packet) == (byte) 0b00000011 || getFlagByte(packet) == (byte) 0b00000101 || getFlagByte(packet) == (byte) 0b00001001 ) {
      return payload.split("~");
    } else {
      System.out.println("Invalid parsing request, this packet type does not have a file_path");
      return null;
    }
  }

  public static byte[] getPayloadAsByteArray(String file_dir, long fileSize) {
    byte[] fileDirectoryBytes = file_dir.getBytes();
    byte[] fileSizeBytes = String.valueOf(fileSize).getBytes();
    byte[] payload = new byte[fileDirectoryBytes.length + fileSizeBytes.length + 1];

    System.arraycopy(fileDirectoryBytes, 0, payload, 0, fileDirectoryBytes.length);
    payload[fileDirectoryBytes.length] = '~';
    System.arraycopy(fileSizeBytes, 0, payload, fileDirectoryBytes.length + 1, fileSizeBytes.length);

    return payload;
  }
}
