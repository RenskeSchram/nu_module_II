package com.nedap.university.packet;

import com.nedap.university.packet.Header.FLAG;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class PacketBuilder {

  public static Packet getAckPacket(int AckNr) {
    Payload payload = new Payload(new byte[1], 0, false);
    Header header = new Header(payload);
    header.setAckNr(AckNr);
    header.setFlagByte((byte) 0b00010000);
    return new Packet(header, payload);
  }

  public static Packet listPacket(Payload payload) {
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

    return new Packet(header, listPayload);
  }

  public static Packet helloListPacket(String src_dir) {
    Payload payload = new Payload(src_dir.toString().getBytes(), 0 , false);
    Header header = new Header(payload);
    header.setFlagByte((byte) 0b00001100);
    return new Packet(header, payload);
  }

  public static Packet getInitBuilderPacket(String src_dir, String dst_dir) {
    Payload payload = new Payload(src_dir, dst_dir,0, false);
    Header header = new Header(payload);
    header.setFlagByte((byte) 0b00000101);

    return new Packet(header, payload);
  }

  public static Packet getInitLoaderPacket(String src_path, String dst_path, long size) {
    Payload payload = new Payload(src_path, dst_path, size, false);
    Header header = new Header(payload);
    header.setFlag(FLAG.HELLO);
    header.setFlag(FLAG.DATA);

    return new Packet(header, payload);
  }

}
