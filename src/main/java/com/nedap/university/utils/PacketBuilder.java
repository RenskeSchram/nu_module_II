package com.nedap.university.utils;

import com.nedap.university.packet.Header;
import com.nedap.university.packet.Header.FLAG;
import com.nedap.university.packet.Packet;
import com.nedap.university.packet.Payload;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class with static functions to create standardized Packets.
 */
public class PacketBuilder {

  /**
   * Create Ack packet.
   * @param AckNr Ack number to be acknowledged.
   * @return Ack packet.
   */
  public static Packet getAckPacket(int AckNr) {
    Payload payload = new Payload(new byte[1], 0, false);
    Header header = new Header(payload);
    header.setAckNr(AckNr);
    header.setFlagByte((byte) 0b00010000);
    return new Packet(header, payload);
  }

  /**
   * Create list Packet with String containing all files in the requested directory in the provided payload.
   * @param payload payload with desired directory to obtain file and folder names from
   * @return Packet with String containing all files in the requested directory in the provided payload
   */
  public static Packet listPacket(Payload payload) {
    List<String> fileNames = new ArrayList<>();

    String src_dir = payload.getSrcPath().split("\\.")[0];

    try {
      fileNames = Files.walk(Paths.get(src_dir), 1)
          .filter(path -> !path.equals(Paths.get(src_dir)))
          .map(path -> Files.isDirectory(path) ? "[" + path.getFileName() + "]" : path.getFileName().toString())
          .collect(Collectors.toList());
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

    return new Packet(header, listPayload);
  }

  /**
   * Create Packet with HELLO and LIST flags in the header and payload with desired source dir to obtain list from.
   * @param src_dir desired source dir to obtain list from.
   * @return Packet with HELLO and LIST flags in the header and payload with desired source dir to obtain list from
   */
  public static Packet helloListPacket(String src_dir) {
    Payload payload = new Payload(src_dir.toString().getBytes(), 0 , false);
    Header header = new Header(payload);
    header.setFlagByte((byte) 0b00001100);
    return new Packet(header, payload);
  }

  /**
   * Create Packet with HELLO and GET flags in the header and payload with directories.
   * @param src_dir desired source for file to obtain
   * @param dst_dir desired destination dir to store obtained file.
   * @return Packet with HELLO and GET flags in the header and payload with directories.
   */
  public static Packet getInitBuilderPacket(String src_dir, String dst_dir) {
    Payload payload = new Payload(src_dir, dst_dir,0, false);
    Header header = new Header(payload);
    header.setFlagByte((byte) 0b00000101);

    return new Packet(header, payload);
  }
  /**
   * Create Packet with HELLO and DATA flags in the header and payload with directories.
   * @param src_path desired source for file to obtain
   * @param dst_path desired destination dir to store obtained file.
   * @return Packet with HELLO and DATA flags in the header and payload with directories.
   */
  public static Packet getInitLoaderPacket(String src_path, String dst_path, long size) {
    Payload payload = new Payload(src_path, dst_path, size, false);
    Header header = new Header(payload);
    header.setFlag(FLAG.HELLO);
    header.setFlag(FLAG.DATA);

    return new Packet(header, payload);
  }
}
