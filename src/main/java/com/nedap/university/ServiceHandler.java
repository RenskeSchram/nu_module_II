package com.nedap.university;

import com.nedap.university.packet.Header;
import com.nedap.university.packet.Packet;
import com.nedap.university.packet.PacketBuilder;
import com.nedap.university.packet.Payload;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServiceHandler {
  FileBuffer fileBuffer;
  FileLoader fileLoader;

  // Sending Window
  int windowSize = 1;
  int lastAckReceived = -1;
  int lastFrameInWindow = 0;

  private int lastFrameSent = -1;

  public ServiceHandler(){
    fileBuffer = new FileBuffer();
    fileLoader = new FileLoader();
  }

  public List<Packet> handlePacket(Packet receivedPacket) throws IOException {
    List<Packet> packetsToSend = new ArrayList<>();
    Header header = receivedPacket.getHeader();
    Payload payload = receivedPacket.getPayload();
    lastAckReceived = header.getAckNr();

    byte flags = header.getFlagByte();

    switch (flags) {
      default:
        System.out.println("INVALID Flags, could not handle packet");

        // Receiving
      case (byte) 0b00000011:   // HELLO + DATA
        fileBuffer.initFileBuffer(payload);
        break;

      case (byte) 0b00000010:   // DATA
        fileBuffer.receivePacket(payload);
        break;

      case (byte) 0b00100010:   // DATA + FIN
        fileBuffer.receiveFin(payload);
        fileBuffer.receivePacket(payload);
        lastFrameSent = lastAckReceived;
        break;

      case (byte) 0b00101000:   // LIST + FIN
        printListPacket(payload);
        lastFrameSent = lastAckReceived;
        break;

      // Sending
      case (byte) 0b00000101:   // HELLO + GET
        Packet helloGetPacket = startUpload(payload.getSrcPath(), payload.getDstPath());
        helloGetPacket.getHeader().setAckNr(lastFrameSent + 1);
        packetsToSend.add(helloGetPacket);
        break;

      case (byte) 0b00010000:   // ACK
        if (fileLoader.isInitiated()) {
          Packet packet = fileLoader.extractNextPacket();
          packet.getHeader().setAckNr(lastFrameSent + 1);
          packetsToSend.add(packet);
        }
        break;

      case (byte) 0b00001100:   // HELLO + LIST
        Packet listPacket = getListPacket(payload);
        listPacket.getHeader().setAckNr(lastFrameSent + 1);
        packetsToSend.add(getListPacket(payload));
        break;
    }

    for (Packet packet : packetsToSend) {
      lastFrameSent = packet.getHeader().getAckNr();
    }

    //System.out.println("SENDING       LAR: " + lastAckReceived + ", LSF: " + lastFrameSent + " and LFIW: " + lastFrameInWindow);
    return packetsToSend;
  }

  public Packet startUpload(String src_path, String dst_path) throws IOException {
    Packet initPacket = fileLoader.initFileLoading(src_path, dst_path);
    initPacket.getHeader().setAckNr(lastFrameSent + 1);
    lastFrameSent++;
    return initPacket;
  }

  public Packet startDownload(String src_path, String dst_path) {
    Packet initPacket = PacketBuilder.getInitBuilderPacket(src_path, dst_path);
    initPacket.getHeader().setAckNr(lastFrameSent + 1);
    lastFrameSent++;
    return initPacket;
  }

  private void printListPacket(Payload payload) {
    String[] fileNames = payload.getStringArray();
    System.out.println("The following files are in the requested folder on the PiServer:");
    for (String fileName : fileNames) {
      System.out.println("   - " + fileName);
    }
  }

  private Packet getListPacket(Payload payload) {
    Packet listPacket = PacketBuilder.listPacket(payload);
    listPacket.getHeader().setAckNr(lastFrameSent + 1);
    lastFrameSent++;
    return listPacket;
  }

  public Packet getHelloListPacket(String src_dir) {
    Packet helloListPacket = PacketBuilder.helloListPacket(src_dir);
    helloListPacket.getHeader().setAckNr(lastFrameSent + 1);
    lastFrameSent++;
    return helloListPacket;
  }
}
