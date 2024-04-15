package com.nedap.university;

import com.nedap.university.packet.Header.FLAG;
import com.nedap.university.packet.Packet;
import com.nedap.university.packet.Header;
import com.nedap.university.packet.Payload;
import com.nedap.university.utils.Parameters;
import java.util.ArrayList;
import java.util.List;

public class FileLoader {
  private final int HEADER_SIZE = Header.getSize();
  private final int MAX_PAYLOAD_SIZE = Parameters.MAX_PACKET_SIZE - HEADER_SIZE;

  /**
   * Extract PacketList from File.
   *
   * @param file file to extract from.
   * @return List of Packets.
   */
  public List<Packet> extractPackets(File file) {
    List<Packet> packetList = new ArrayList<>();

    packetList.add(getHelloPacket(file));

    int offsetPointer = 0;

    while (offsetPointer < file.getFileData().length / MAX_PAYLOAD_SIZE) {
      // add packet payload
      byte[] payloadByteArray;
      boolean isFinalPacket;

      if (offsetPointer * MAX_PAYLOAD_SIZE <= file.getFileData().length) {
        payloadByteArray = new byte[HEADER_SIZE + MAX_PAYLOAD_SIZE];
        isFinalPacket = false;
      } else {
        payloadByteArray = new byte[HEADER_SIZE + file.getFileData().length % MAX_PAYLOAD_SIZE];
        isFinalPacket = true;
      }

      System.arraycopy(file.getFileData(), offsetPointer * MAX_PAYLOAD_SIZE, payloadByteArray,
          0, payloadByteArray.length);

      Payload payload = new Payload(payloadByteArray, offsetPointer, isFinalPacket);
      Header header = new Header(payload);

      packetList.add(new Packet(header, payload));

      offsetPointer ++;
    }

    System.out.println(packetList.size() + " added packets to the queue");
    return packetList;
  }

  private Packet getHelloPacket(File file) {
    Payload payload = new Payload(file.getFILE_DIR().getBytes(), 0 , false);
    Header header = new Header(payload);
    header.setFlag(FLAG.HELLO);

    return new Packet(header, payload);
  }

  public static void main(String[] args) {
    FileLoader fileLoader= new FileLoader();
    File file = new File("example_files/tiny.pdf");
    fileLoader.extractPackets(file);
  }

}
