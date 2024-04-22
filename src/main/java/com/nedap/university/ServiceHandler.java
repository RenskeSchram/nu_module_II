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
  List<Integer> sendingWindow;
  private int currentAckNr;

  public ServiceHandler(){
    fileBuffer = new FileBuffer();
    fileLoader = new FileLoader();
  }

  public List<Packet> handlePacket(Packet receivedPacket) throws IOException {
    List<Packet> packetsToSend = new ArrayList<>();
    Header header = receivedPacket.getHeader();
    Payload payload = receivedPacket.getPayload();

    byte flags = header.getFlagByte();

    // If not Flag.ACK, send ACK
    if (flags != 0b00010000) {
      packetsToSend.add(getAckPacket(header.getAckNr()));
    }

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

        // HELLO + GET
      case (byte) 0b00000101:
        Packet helloGetPacket = startUpload(payload.getSrcPath(), payload.getDstPath());
        packetsToSend.add(helloGetPacket);
        break;

        // ACK
      case (byte) 0b00010000:
        // updateSendingWindow()
        int availableAckNrs = updateSendingWindow(header.getAckNr());
        // if (new windowSpotsAvailable)-> fileLoader.extractFile(based on updated sending window)

          for (int i = 0; i < availableAckNrs; i++) {
            if (fileLoader.isInitiated()) {
              Packet packet = fileLoader.extractNextPacket();
              packet.getHeader().setAckNr(currentAckNr);
              packetsToSend.add(packet);
            } else {
              resetSendingWindow();
            }
          }
        break;

      // HELLO + LIST
      case (byte) 0b00001100:
        // getListPacket(payload)
        packetsToSend.add(getListPacket(payload));
        break;


      // LIST + FIN
      case (byte) 0b00101000:
        // getListPacket(payload)
        printListPacket(payload);
        break;

      default:
        System.out.println("could not handle packet");

    }

    for (Packet packet : packetsToSend) {
      // set AckNr
      packet.getHeader().setAckNr(currentAckNr);

      // Ack nr ++
      currentAckNr++;
    }

    return packetsToSend;
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
    header.setAckNr(currentAckNr);

    return new Packet(header, listPayload);
  }

  Packet getHelloListPacket(String src_dir) {
    Payload payload = new Payload(src_dir.toString().getBytes(), 0 , false);
    Header header = new Header(payload);
    header.setFlagByte((byte) 0b00001100);
    header.setAckNr(currentAckNr);
    return new Packet(header, payload);
  }

  public static Packet getAckPacket(int AckNr) {
    Payload payload = new Payload(new byte[1], 0, false);
    Header header = new Header(payload);
    header.setAckNr(AckNr);
    header.setFlagByte((byte) 0b00010000);

    return new Packet(header, payload);
  }

  public Packet startUpload(String src_path, String dst_path) throws IOException {
    Packet initPacket = fileLoader.initFileLoading(src_path, dst_path);
    setSendingWindow(0);
    initPacket.getHeader().setAckNr(currentAckNr);

    return initPacket;
  }

  public Packet startDownload(String src_path, String dst_path) {
    Packet initPacket = fileBuffer.getInitPacket(src_path, dst_path);
    setSendingWindow(0);
    initPacket.getHeader().setAckNr(currentAckNr);

    return initPacket;
  }

  private void setSendingWindow(int ackNr) {
    setCurrentAckNr(ackNr);
    this.sendingWindow = new ArrayList<>();
    sendingWindow.add(currentAckNr);
  }

  private int updateSendingWindow(int receivedAck) {
    this.currentAckNr = receivedAck + 1;
    this.sendingWindow = new ArrayList<>();
    sendingWindow.add(currentAckNr);

    // return nr of new available acks
    return 1;
  }

  private void resetSendingWindow() {
    setSendingWindow(0);
  }

  private void setCurrentAckNr(int AckNr) {
    this.currentAckNr = AckNr;
  }


}
