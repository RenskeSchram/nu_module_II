package com.nedap.university;

import com.nedap.university.packet.Header;
import com.nedap.university.packet.Packet;
import com.nedap.university.packet.Payload;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
      ///////////////////////////////////
      //          Receiving            //
      ///////////////////////////////////

      // HELLO + DATA
      case (byte) 0b00000011:
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
        lastFrameSent = lastAckReceived;
        break;

      // LIST + FIN
      case (byte) 0b00101000:
        printListPacket(payload);
        lastFrameSent = lastAckReceived;
        break;

      ///////////////////////////////////
      //           Sending             //
      ///////////////////////////////////

        // HELLO + GET
      case (byte) 0b00000101:
        Packet helloGetPacket = startUpload(payload.getSrcPath(), payload.getDstPath());
        helloGetPacket.getHeader().setAckNr(lastFrameSent + 1);
        packetsToSend.add(helloGetPacket);
        break;

        // ACK
      case (byte) 0b00010000:
        if (fileLoader.isInitiated()) {
          Packet packet = fileLoader.extractNextPacket();
          packet.getHeader().setAckNr(lastFrameSent + 1);
          packetsToSend.add(packet);
        }
        break;

      // HELLO + LIST
      case (byte) 0b00001100:
        Packet listPacket = getListPacket(payload);
        listPacket.getHeader().setAckNr(lastFrameSent + 1);
        packetsToSend.add(getListPacket(payload));
        break;

      default:
        System.out.println("INVALID Flags, could not handle packet");

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
    Packet initPacket = fileBuffer.getInitPacket(src_path, dst_path);
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
    List<String> fileNames = null;

    System.out.println(Paths.get(payload.getSrcPath()));

    try (var directoryStream = Files.list(Paths.get(payload.getSrcPath()))) {
      fileNames = directoryStream.map(Path::getFileName).map(Path::toString).collect(Collectors.toList());
    } catch (IOException e) {
      e.printStackTrace();
    }

    StringBuilder fileNameString = new StringBuilder();
    if (fileNames != null) {
      for (int i = 0; i < fileNames.size(); i++) {
        fileNameString.append(fileNames.get(i));
        if (i < fileNames.size() - 1) {
          fileNameString.append("~");
        }
      }} else {fileNameString.append("");
    }
    Payload listPayload = new Payload(fileNameString.toString().getBytes(), 0 , true);
    Header header = new Header(listPayload);
    header.setFlagByte((byte) 0b00101000);
    header.setAckNr(lastFrameSent + 1);
    lastFrameSent++;

    return new Packet(header, listPayload);
  }

  public Packet getHelloListPacket(String src_dir) {
    Payload payload = new Payload(src_dir.toString().getBytes(), 0 , false);
    Header header = new Header(payload);
    header.setFlagByte((byte) 0b00001100);
    header.setAckNr(lastFrameSent + 1);
    lastFrameSent++;
    return new Packet(header, payload);
  }

  public static Packet getAckPacket(int AckNr) {
    Payload payload = new Payload(new byte[1], 0, false);
    Header header = new Header(payload);
    header.setAckNr(AckNr);
    header.setFlagByte((byte) 0b00010000);
    return new Packet(header, payload);
  }
}
