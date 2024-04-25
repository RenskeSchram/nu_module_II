package com.nedap.university.networkhost.handlers;

import com.nedap.university.packet.Header;
import com.nedap.university.packet.Packet;
import com.nedap.university.utils.PacketBuilder;
import com.nedap.university.packet.Payload;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Layer which handles the ordered and accepted Packets by referring them to the next correct layer
 * and returning the response Packets to the Host layer.
 */
public class ServiceHandler {

  FileBuffer fileBuffer;
  FileLoader fileLoader;

  // Sending Window
  private int lastAckReceived = -1;
  private int lastFrameSent = -1;

  public ServiceHandler() {
    fileBuffer = new FileBuffer();
    fileLoader = new FileLoader();
  }

  /**
   * Base action of Packet on the Flags.
   *
   * @param receivedPacket received Packet to handle.
   * @return List of possible to be sent Packets.
   * @throws IOException is I/O error occurs.
   */
  public List<Packet> handlePacket(Packet receivedPacket) throws IOException {
    List<Packet> packetsToSend = new ArrayList<>();
    Header header = receivedPacket.getHeader();
    Payload payload = receivedPacket.getPayload();
    lastAckReceived = header.getAckNr();
    byte flags = header.getFlagByte();

    switch (flags) {
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

      case (byte) 0b01100000:    // ERROR + FIN
        printError(payload);
        lastFrameSent = lastAckReceived;
        break;

      case (byte) 0b10100000:    // DELETE + FIN
        if (Arrays.equals(payload.getByteArray(), new byte[]{(byte) 1})) {
          System.out.println("SUCCESSFUL");
        } else {
          System.out.println("FAILED");
        }
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
        Packet listPacket = getFinListPacket(payload);
        listPacket.getHeader().setAckNr(lastFrameSent + 1);
        packetsToSend.add(getFinListPacket(payload));
        break;

      case (byte) 0b10000001: // HELLO + DELETE
        Packet deletePacket = getFinDeletePacket(payload);
        deletePacket.getHeader().setAckNr(lastFrameSent + 1);
        packetsToSend.add(deletePacket);
        break;

      default:
        System.out.println("INVALID Flags, could not handle packet");
        break;
    }

    for (Packet packet : packetsToSend) {
      lastFrameSent = packet.getHeader().getAckNr();
    }
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

  private void printError(Payload payload) {
    System.err.println("ERROR " + new String(payload.getByteArray()));
  }

  public Packet startList(String src_dir) {
    Packet helloListPacket = PacketBuilder.helloListPacket(src_dir);
    helloListPacket.getHeader().setAckNr(lastFrameSent + 1);
    lastFrameSent++;
    return helloListPacket;
  }

  private Packet getFinListPacket(Payload payload) {
    Packet listPacket = PacketBuilder.listPacket(payload);
    listPacket.getHeader().setAckNr(lastFrameSent + 1);
    lastFrameSent++;
    return listPacket;
  }

  private void printListPacket(Payload payload) {
    String[] fileNames = payload.getStringArray();
    for (String fileName : fileNames) {
      System.out.println("  " + fileName);
    }
    System.out.println();
  }


  public Packet startDelete(String src_dir) {
    Packet deletePacket = PacketBuilder.helloDeletePacket(src_dir);
    deletePacket.getHeader().setAckNr(lastFrameSent + 1);
    lastFrameSent++;
    return deletePacket;
  }

  private Packet getFinDeletePacket(Payload payload) {
    Packet listPacket = deletePacket(payload);
    listPacket.getHeader().setAckNr(lastFrameSent + 1);
    lastFrameSent++;
    return listPacket;
  }

  private Packet deletePacket(Payload payload) {
    String src_dir = payload.getSrcPath().split("~")[0];
    File file = new File(src_dir);

    if (file.delete()) {
      return PacketBuilder.finDeletePacket(true);
    } else {
      return PacketBuilder.finDeletePacket(false);
    }
  }
}
