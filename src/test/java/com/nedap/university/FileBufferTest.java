package com.nedap.university;

import com.nedap.university.packet.Header.FLAG;
import com.nedap.university.packet.Packet;
import com.nedap.university.utils.*;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class FileBufferTest {

  private FileBuffer fileBuffer;
  private FileLoader fileLoader;
  private List<Packet> packetList;
  private static final String TEST_SRC_FILE_PATH = "example_files/tiny.pdf";
  private static final String TEST_DST_FILE_PATH = "example_files/test_file.pdf";

  @BeforeEach
  public void setUp() throws IOException {
    fileLoader = new FileLoader();
    fileBuffer = new FileBuffer();
    packetList = fileLoader.extractPackets(Paths.get(TEST_SRC_FILE_PATH));
  }

  @AfterEach
  public void tearDown() {
    Path path = Paths.get(TEST_DST_FILE_PATH);
    try {
      Files.deleteIfExists(path);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testInitFileBuffer() throws IOException {
    Packet initPacket = fileLoader.getInitPacket(TEST_DST_FILE_PATH, Files.size(Paths.get(TEST_SRC_FILE_PATH)));
    fileBuffer.initFileBuffer(PacketParser.packetToByteArray(initPacket));
    assertTrue(initPacket.getHeader().isFlagSet(FLAG.HELLO));
    assertTrue(initPacket.getHeader().isFlagSet(FLAG.DATA));

    assertTrue(fileBuffer.isInitialized);
    assertEquals(TEST_DST_FILE_PATH, fileBuffer.getFilePath());
    assertEquals(Files.size(Paths.get(TEST_SRC_FILE_PATH)), fileBuffer.getFileSize());
    assertNotNull(fileBuffer.getByteBuffer());
  }

  @Test
  public void testReceivePacket() throws IOException {
    testInitFileBuffer();
    byte[] packet = PacketParser.packetToByteArray(packetList.get(0));
    fileBuffer.receivePacket(packet);
    assertArrayEquals(PacketParser.getPayload(packet), fileBuffer.getByteBuffer().array());
  }

  @Test
  public void testReceiveFin() throws IOException {
    testInitFileBuffer();
    byte[] packet = PacketParser.packetToByteArray(packetList.get(packetList.size()-1));
    fileBuffer.receiveFin(packet);
    assertEquals(PacketParser.getOffsetPointer(packet), fileBuffer.getFinalOffsetPointer());
  }

  @Test
  public void testResetBuffer() throws IOException {
    testInitFileBuffer();
    fileBuffer.resetBuffer();
    assertNull(fileBuffer.getByteBuffer());
    assertFalse(fileBuffer.isInitialized);
    assertNull(fileBuffer.getFilePath());
    assertEquals(-1, fileBuffer.getFileSize());
    assertEquals(0, fileBuffer.getExpectedOffsetPointer());
    assertEquals(-1, fileBuffer.getFinalOffsetPointer());
  }

  @Test
  public void testWriteBufferToFile() throws IOException {
    testInitFileBuffer();
    for (Packet packet : packetList) {
      fileBuffer.receivePacket(PacketParser.packetToByteArray(packet));
    }
    fileBuffer.writeBufferToFile();

    Path path = Paths.get(TEST_SRC_FILE_PATH);
    assertTrue(Files.exists(path));

  }
}