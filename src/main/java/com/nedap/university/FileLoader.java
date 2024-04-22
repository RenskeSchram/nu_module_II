package com.nedap.university;

import static com.nedap.university.utils.Parameters.HEADER_SIZE;
import static com.nedap.university.utils.Parameters.MAX_PAYLOAD_SIZE;

import com.nedap.university.packet.Header.FLAG;
import com.nedap.university.packet.Packet;
import com.nedap.university.packet.Header;
import com.nedap.university.packet.PacketBuilder;
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
  private Path src_path;
  private int offsetPointer;
  private boolean initiated;
  int finalOffsetPointer;
  public Packet initFileLoading(String src_path, String dst_path) throws IOException {
    this.initiated = true;
    this.src_path = Paths.get(src_path);
    this.offsetPointer = 0;
    this.finalOffsetPointer = (int) Math.ceil((double) Files.size(this.src_path) / Parameters.MAX_PAYLOAD_SIZE);
    return PacketBuilder.getInitLoaderPacket(src_path, dst_path, Files.size(this.src_path));
  }

  public Packet extractNextPacket() {
    if (initiated) {
      try (FileChannel fileChannel = FileChannel.open(src_path, StandardOpenOption.READ)) {
        long fileLength = fileChannel.size();

        int remainingBytes = (int) Math.min(
            fileLength - offsetPointer * Parameters.MAX_PAYLOAD_SIZE, Parameters.MAX_PAYLOAD_SIZE);

        ByteBuffer buffer = ByteBuffer.allocate(remainingBytes);
        fileChannel.position((long) offsetPointer * Parameters.MAX_PAYLOAD_SIZE);
        fileChannel.read(buffer);
        buffer.flip();

        byte[] payloadByteArray = new byte[remainingBytes];
        buffer.get(payloadByteArray);
        boolean isFinalPacket = (offsetPointer + 1) * Parameters.MAX_PAYLOAD_SIZE >= fileLength;

        Payload payload = new Payload(payloadByteArray, offsetPointer, isFinalPacket);
        Header header = new Header(payload);
        header.setFlag(FLAG.DATA);

        offsetPointer++;

        if (isFinalPacket) {
          reset();
        }

        return new Packet(header, payload);

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      return null;
    }
  }

  public boolean isInitiated() {
    return initiated;
  }

  private void reset() {
    this.initiated = false;
    this.src_path = null;
    this.offsetPointer = -1;
    this.finalOffsetPointer = -1;
  }


  ///////////////////////////////////////////////////////////////////////
  //                               TIJDELIJK                           //
  ///////////////////////////////////////////////////////////////////////

  public static void main(String[] args) throws IOException {
    FileLoader fileLoader= new FileLoader();
    fileLoader.extractPackets(Paths.get("example_files/tiny.pdf"));
  }


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
        header.setFlag(FLAG.DATA);

        packetList.add(new Packet(header, payload));

        offsetPointer++;
      }
    }

    return packetList;
  }

  public Packet extractPacket(Path file_path, int offsetPointer) throws IOException {
    try (FileChannel fileChannel = FileChannel.open(file_path, StandardOpenOption.READ)) {
      long fileLength = fileChannel.size();

      int remainingBytes = (int) Math.min(fileLength - offsetPointer * Parameters.MAX_PAYLOAD_SIZE, Parameters.MAX_PAYLOAD_SIZE);

      ByteBuffer buffer = ByteBuffer.allocate(remainingBytes);
      fileChannel.read(buffer);
      buffer.flip();

      byte[] payloadByteArray = new byte[remainingBytes];
      buffer.get(payloadByteArray);

      boolean isFinalPacket = (offsetPointer + 1) * Parameters.MAX_PAYLOAD_SIZE >= fileLength;

      if (isFinalPacket) {
        reset();
      }

      Payload payload = new Payload(payloadByteArray, offsetPointer, isFinalPacket);
      Header header = new Header(payload);
      header.setFlag(FLAG.DATA);
      return new Packet(header, payload);
    }
  }



}
