package com.nedap.university;

import com.nedap.university.packet.Packet;
import com.nedap.university.packet.Payload;
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
  private int finalOffsetPointer = -1;

  public boolean isInitialized = false;

  FileBuffer() {
    savedPackets = new HashMap<>();
  }

  public void initFileBuffer(Payload payload) {
    String[] stringArray = payload.getStringArray();
    if (stringArray != null & !isInitialized) {
      this.filePath = stringArray[0];
      this.fileSize = Integer.parseInt(stringArray[1]);
      byteBuffer = ByteBuffer.allocate(fileSize);
      isInitialized = true;
    }
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
      try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
        System.out.println(byteBuffer.array().length);
        byteBuffer.flip();
        while (byteBuffer.hasRemaining()) {
          fileChannel.write(byteBuffer);
          System.out.println(fileChannel.size());
        }
      }

      System.out.println("File " + filePath + " created");
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


