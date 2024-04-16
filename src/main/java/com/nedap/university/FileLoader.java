package com.nedap.university;

import com.nedap.university.packet.Header.FLAG;
import com.nedap.university.packet.Packet;
import com.nedap.university.packet.Header;
import com.nedap.university.packet.Payload;
import com.nedap.university.utils.PacketParser;
import com.nedap.university.utils.Parameters;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileLoader {
  private final int HEADER_SIZE = Header.getSize();
  private final int MAX_PAYLOAD_SIZE = Parameters.MAX_PACKET_SIZE - HEADER_SIZE;


  /**
   * Extract PacketList from File.
   *
   * @param file_path file to extract from.
   * @return List of Packets.
   */
  public List<Packet> extractPackets(Path file_path) throws IOException {
    List<Packet> packetList = new ArrayList<>();
    int offsetPointer = 0;

    long fileLength = Files.size(file_path);

    while (offsetPointer < fileLength / MAX_PAYLOAD_SIZE) {
      // add packet payload
      byte[] payloadByteArray;
      boolean isFinalPacket;

      if (offsetPointer * MAX_PAYLOAD_SIZE <= fileLength) {
        payloadByteArray = new byte[HEADER_SIZE + MAX_PAYLOAD_SIZE];
        isFinalPacket = false;
      } else {
        payloadByteArray = new byte[(int) (HEADER_SIZE + fileLength % MAX_PAYLOAD_SIZE)];
        isFinalPacket = true;
      }

      System.arraycopy(Files.readAllBytes(file_path), offsetPointer * MAX_PAYLOAD_SIZE, payloadByteArray,
          0, payloadByteArray.length);

      Payload payload = new Payload(payloadByteArray, offsetPointer, isFinalPacket);
      Header header = new Header(payload);

      packetList.add(new Packet(header, payload));

      offsetPointer ++;
    }

    System.out.println(packetList.size() + " added packets to the queue");
    return packetList;
  }


  public Packet getInitPacket(String dstDir, long size) {
    Payload payload = new Payload(PacketParser.getPayloadAsByteArray(dstDir, size), 0 , false);
    Header header = new Header(payload);
    header.setFlag(FLAG.HELLO);

    return new Packet(header, payload);
  }


  public static void main(String[] args) throws IOException {
    FileLoader fileLoader= new FileLoader();
    fileLoader.extractPackets(Paths.get("example_files/tiny.pdf"));
  }

}