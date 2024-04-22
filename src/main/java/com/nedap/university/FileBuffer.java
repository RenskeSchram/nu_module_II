package com.nedap.university;

import com.nedap.university.packet.Header;
import com.nedap.university.packet.Header.FLAG;
import com.nedap.university.packet.Packet;
import com.nedap.university.packet.Payload;
import com.nedap.university.utils.Parameters;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FileBuffer {
  private ByteBuffer byteBuffer;
  private Map<Integer, byte[]> savedPackets;

  private String dst_path;
  private int fileSize;

  private int expectedOffsetPointer = 0;
  private int finalOffsetPointer = -1;

  public boolean isInitialized = false;

  FileBuffer() {
    savedPackets = new HashMap<>();
  }

  public void initFileBuffer(Payload payload) {
    System.out.println("BUFFER initiated");
    String[] stringArray = payload.getStringArray();
    if (stringArray != null & !isInitialized) {
      this.dst_path = stringArray[1];
      this.fileSize = Integer.parseInt(stringArray[2]);
      byteBuffer = ByteBuffer.allocate(fileSize);
      isInitialized = true;
    }
  }

  public Packet getInitPacket(String src_dir, String dst_dir) {
    Payload payload = new Payload(src_dir, dst_dir,0, false);
    Header header = new Header(payload);
    header.setFlagByte((byte) 0b00000101);

    return new Packet(header, payload);
  }

  public void receivePacket(Payload payload) {
    int offsetPointer = payload.getOffsetPointer();
    if (expectedOffsetPointer == offsetPointer) {
      writePacketsToBuffer(payload.getByteArray(), offsetPointer);
      checkSavedPackets();
    } else {
      addPacketToSavedPackets(payload.getByteArray(), offsetPointer);
    }
  }

  private void writePacketsToBuffer(byte[] payload, int offsetPointer) {
    byteBuffer.position(offsetPointer* Parameters.MAX_PAYLOAD_SIZE);
    try {
      byteBuffer.put(payload);
    } catch (Exception e ) {
      System.out.println("Could not add file");
      System.out.println("Current position: " + byteBuffer.position());
      System.out.println("Current capacity: " + byteBuffer.capacity());
      System.out.println("Payload length: " + payload.length);
    }

    for (int offset : savedPackets.keySet()) {
      byte[] packetData = savedPackets.get(offset);
      byteBuffer.position(offset);
      byteBuffer.put(packetData);
    }

    if (expectedOffsetPointer == finalOffsetPointer) {
      writeBufferToFile();
      resetBuffer();
    }

    expectedOffsetPointer++;
  }

  void resetBuffer() {
    byteBuffer = null;
    savedPackets = new HashMap<>();

    dst_path = null;
    fileSize = -1;

    expectedOffsetPointer = 0;
    finalOffsetPointer = -1;

    isInitialized = false;
  }

  private void checkSavedPackets() {
    if (savedPackets.containsKey(expectedOffsetPointer)) {
      writePacketsToBuffer(savedPackets.get(expectedOffsetPointer), expectedOffsetPointer);
      savedPackets.remove(expectedOffsetPointer - 1);
      checkSavedPackets();
    }
  }

  private void addPacketToSavedPackets(byte[] payload, int offsetPointer) {
    savedPackets.put(offsetPointer, payload);
  }


  void writeBufferToFile() {
    try {
      Path path = Paths.get(dst_path);
      try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
        byteBuffer.rewind();
        while (byteBuffer.hasRemaining()) {
          fileChannel.write(byteBuffer);
        }
      }

      String s = Paths.get(dst_path).toAbsolutePath().toString();
      System.out.println("Created file " + s + " of " + Files.size(Paths.get(dst_path)) + " bytes");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void receiveFin(Payload payload) {
    finalOffsetPointer = payload.getOffsetPointer();
  }

  public int getFileSize() {
    return fileSize;
  }

  public String getDst_path() {
    return dst_path;
  }

  public int getExpectedOffsetPointer() {
    return expectedOffsetPointer;
  }

  public ByteBuffer getByteBuffer() {
    return byteBuffer;
  }

  public int getFinalOffsetPointer() {
    return finalOffsetPointer;
  }
}



