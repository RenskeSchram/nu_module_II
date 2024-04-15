package com.nedap.university;

import com.nedap.university.packet.Packet;
import com.nedap.university.packet.Header;
import com.nedap.university.packet.Header.FLAG;

public class FileLoader {
  private int PACKET_DATA_SIZE = 5000;
  private int PROTOCOL_HEADER_SIZE = 10;

  public void loadFile(String FILE_DIR, PacketQueue queue) throws InterruptedException {
    File file = new File(FILE_DIR);

    int offsetPointer = 0;
    while (offsetPointer < file.getFileData().length / PACKET_DATA_SIZE) {
      Packet packet = new Packet();

      // add packet data
      byte[] packetData = new byte[getPacketSize()];
      System.arraycopy(file.getFileData(), offsetPointer * PACKET_DATA_SIZE, packetData, getHeaderSize(), PACKET_DATA_SIZE);
      packet.setPayload(packetData);

      // add protocol header
      Header header = new Header(PROTOCOL_HEADER_SIZE);
      header.setPacketDataSize(PACKET_DATA_SIZE);
      header.setOffsetPointer(offsetPointer);
      header.setFlag(FLAG.DATA);
      packet.setHeader(header);

      System.out.println("adding file to queue");
      queue.putPacket(packet);

      offsetPointer ++;
    }
  }

  private int getHeaderSize() {
    return PROTOCOL_HEADER_SIZE;
  }

  private int getPacketSize() {
    return PROTOCOL_HEADER_SIZE + PACKET_DATA_SIZE;
  }

  public static void main(String[] args) throws InterruptedException {
    FileLoader fileLoader= new FileLoader();
    PacketQueue queue = new PacketQueue();
    fileLoader.loadFile("example_files/tiny.pdf", queue);

  }

}
