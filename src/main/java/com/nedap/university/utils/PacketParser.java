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
    byte[] headerByteArray = new byte[packet.getSize()];

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
}
