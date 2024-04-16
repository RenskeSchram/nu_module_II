package com.nedap.university;

import com.nedap.university.packet.Packet;
import com.nedap.university.utils.PacketParser;
import com.nedap.university.utils.Parameters;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class FileBuffer {
  private ByteBuffer byteBuffer;
  private Map<Integer, byte[]> savedPackets;

  private String filePath;
  private int fileSize;

  private int expectedOffsetPointer = 0;
  private int finalOffsetPointer;

  public boolean isInitialized = false;

  FileBuffer() {
    savedPackets = new HashMap<>();
  }

  public void initFileBuffer(byte[] packet) {
    String[] stringPayload = PacketParser.getStringPayload(packet);
    if (stringPayload != null & !isInitialized) {
      this.filePath = stringPayload[0];
      this.fileSize = Integer.parseInt(stringPayload[1]);
      byteBuffer = ByteBuffer.allocate(fileSize);
      isInitialized = true;
    }
  }

  public void receivePacket(byte[] packet) {
    int offsetPointer = PacketParser.getOffsetPointer(packet);
    if (expectedOffsetPointer == offsetPointer) {
      writePacketsToBuffer(PacketParser.getPayload(packet), offsetPointer);
      checkSavedPackets();
    } else {
      addPacketToSavedPackets(PacketParser.getPayload(packet), offsetPointer);
    }
  }

  private void writePacketsToBuffer(byte[] payload, int offsetPointer) {
    byteBuffer.position(offsetPointer* Parameters.MAX_PAYLOAD_SIZE);
    byteBuffer.put(payload);

    for (int offset : savedPackets.keySet()) {
      byte[] packetData = savedPackets.get(offset);
      byteBuffer.position(offset);
      byteBuffer.put(packetData);
    }

    if (expectedOffsetPointer == finalOffsetPointer ) {
      writeBufferToFile();
      resetBuffer();
    }

    expectedOffsetPointer++;
  }

  void resetBuffer() {
    byteBuffer = null;
    savedPackets = new HashMap<>();

    filePath = null;
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
      Path path = Paths.get(filePath);
      FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

      byteBuffer.flip();
      fileChannel.write(byteBuffer);

      fileChannel.close();

      System.out.println("File " + filePath + " created");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void receiveFin(byte[] packet) {
    finalOffsetPointer = PacketParser.getOffsetPointer(packet);
  }

  public int getFileSize() {
    return fileSize;
  }

  public String getFilePath() {
    return filePath;
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


