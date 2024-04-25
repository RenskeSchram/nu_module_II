package com.nedap.university.networkhost.handlers;

import com.nedap.university.packet.Payload;
import com.nedap.university.utils.Parameters;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to store incoming payloads with data and write to File.
 */
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

  /**
   * Initiate Class using the retrieved payload with incoming File information.
   *
   * @param payload retrieved payload with incoming File information.
   */
  public void initFileBuffer(Payload payload) {
    String[] stringArray = payload.getStringArray();
    if (stringArray != null & !isInitialized) {
      this.dst_path = stringArray[1];
      this.fileSize = Integer.parseInt(stringArray[2]);
      byteBuffer = ByteBuffer.allocate(fileSize);
      isInitialized = true;
    }
  }

  /**
   * Handle a received DATA Packet. If next in order -> write to Buffer, otherwise -> store until
   * next in order.
   *
   * @param payload payload with data.
   */
  public void receivePacket(Payload payload) {
    int offsetPointer = payload.getOffsetPointer();
    if (expectedOffsetPointer == offsetPointer) {
      writePacketsToBuffer(payload.getByteArray(), offsetPointer);
      checkSavedPackets();
    } else {
      addPacketToSavedPackets(payload.getByteArray(), offsetPointer);
    }
  }

  /**
   * Write Packet data to the buffer.
   *
   * @param payload       payload with data.
   * @param offsetPointer offset pointer with location of data in the buffer.
   */
  private void writePacketsToBuffer(byte[] payload, int offsetPointer) {
    byteBuffer.position(offsetPointer * Parameters.MAX_PAYLOAD_SIZE);
    try {
      byteBuffer.put(payload);
    } catch (RuntimeException e) {
      System.err.println("Could not add file");
      System.err.println("Current position: " + byteBuffer.position());
      System.err.println("Current capacity: " + byteBuffer.capacity());
      System.err.println("Payload length: " + payload.length);
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

  /**
   * Reset Buffer.
   */
  void resetBuffer() {
    byteBuffer = null;
    savedPackets = new HashMap<>();

    dst_path = null;
    fileSize = -1;

    expectedOffsetPointer = 0;
    finalOffsetPointer = -1;

    isInitialized = false;
  }

  /**
   * Check if one of the saved Packets is next in order to write to the Buffer.
   */
  private void checkSavedPackets() {
    if (savedPackets.containsKey(expectedOffsetPointer)) {
      writePacketsToBuffer(savedPackets.get(expectedOffsetPointer), expectedOffsetPointer);
      savedPackets.remove(expectedOffsetPointer - 1);
      checkSavedPackets();
    }
  }

  /**
   * Add Packet to the saved out of order Packets.
   *
   * @param payload       payload with data.
   * @param offsetPointer offset-pointer for location of data in final buffer
   */
  private void addPacketToSavedPackets(byte[] payload, int offsetPointer) {
    savedPackets.put(offsetPointer, payload);
  }

  /**
   * Write Buffer to File.
   */
  void writeBufferToFile() {
    try {
      Path path = Paths.get(dst_path);
      Files.createDirectories(path.getParent());

      try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.CREATE,
          StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
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
