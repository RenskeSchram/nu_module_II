package com.nedap.university;

import com.nedap.university.packet.Header;
import com.nedap.university.packet.Packet;
import com.nedap.university.packet.Payload;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class PacketHandler {
  FileBuffer fileBuffer;

  public PacketHandler(){
    fileBuffer = new FileBuffer();
  }

  public void handlePacket(Packet receivedPacket) {
    Header header = receivedPacket.getHeader();
    Payload payload = receivedPacket.getPayload();

    byte flags = header.getFlagByte();
    switch (flags) {
      // HELLO + DATA
      case (byte) 0b00000011:
        // initialize new file buffer to store data
        fileBuffer.initFileBuffer(payload);
        break;

        // DATA
      case (byte) 0b00000010:
        fileBuffer.receivePacket(payload);
        break;

        // DATA + FIN
      case (byte) 0b00100010:
        fileBuffer.receiveFin(payload);
        fileBuffer.receivePacket(payload);
        break;
    }
  }

  public Packet getAckPacket(int AckNr) {
    Payload payload = new Payload(new byte[1], 0, true);

    Header header = new Header(payload);
    header.setAckNr(AckNr);

    System.out.println("sending package with Ack: " + AckNr);

    return new Packet(header, payload);
  }

}
