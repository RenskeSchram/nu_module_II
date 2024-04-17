package com.nedap.university;

import static com.nedap.university.utils.Parameters.HEADER_SIZE;
import static com.nedap.university.utils.Parameters.MAX_PAYLOAD_SIZE;

import com.nedap.university.packet.Header.FLAG;
import com.nedap.university.packet.Packet;
import com.nedap.university.packet.Header;
import com.nedap.university.packet.Payload;
import com.nedap.university.utils.Parameters;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class FileLoader {
  /**
   * Extract PacketList from File.
   *
   * @param file_path file to extract from.
   * @return List of Packets.
   */
  public List<Packet> extractPackets(Path file_path) throws IOException {
    List<Packet> packetList = new ArrayList<>();

    try (FileChannel fileChannel = FileChannel.open(file_path, StandardOpenOption.READ)) {
      long fileLength = fileChannel.size();
      int offsetPointer = 0;

      while (offsetPointer < Math.ceil((double) fileLength / Parameters.MAX_PAYLOAD_SIZE)) {
        int remainingBytes = (int) Math.min(fileLength - offsetPointer * Parameters.MAX_PAYLOAD_SIZE, Parameters.MAX_PAYLOAD_SIZE);

        ByteBuffer buffer = ByteBuffer.allocate(remainingBytes);
        fileChannel.read(buffer);
        buffer.flip();

        byte[] payloadByteArray = new byte[remainingBytes];
        buffer.get(payloadByteArray);

        boolean isFinalPacket = (offsetPointer + 1) * Parameters.MAX_PAYLOAD_SIZE >= fileLength;
        Payload payload = new Payload(payloadByteArray, offsetPointer, isFinalPacket);
        Header header = new Header(payload);

        packetList.add(new Packet(header, payload));

        offsetPointer++;
      }
    }

    System.out.println(packetList.size() + " packets added to the queue");
    return packetList;
  }

  public Packet getInitPacket(String dstDir, long size) {
    Payload payload = new Payload(dstDir, size, false);
    Header header = new Header(payload);
    header.setFlag(FLAG.HELLO);

    return new Packet(header, payload);
  }


  public static void main(String[] args) throws IOException {
    FileLoader fileLoader= new FileLoader();
    fileLoader.extractPackets(Paths.get("example_files/tiny.pdf"));
  }

}
