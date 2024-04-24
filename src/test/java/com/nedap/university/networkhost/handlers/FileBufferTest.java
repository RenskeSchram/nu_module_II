package com.nedap.university.networkhost.handlers;

import com.nedap.university.packet.Header.FLAG;
import com.nedap.university.packet.Packet;
import com.nedap.university.utils.PacketBuilder;
import com.nedap.university.packet.Payload;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.nedap.university.networkhost.handlers.FileLoaderTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileBufferTest {

  private FileBuffer fileBuffer;
  private FileLoader fileLoader;
  private List<Packet> packetList;

  @BeforeEach
  public void setUp() throws IOException {
    fileLoader = new FileLoader();
    fileBuffer = new FileBuffer();
    packetList = fileLoader.extractPackets(Paths.get(TEST_SRC_FILE_PATH));
  }

  @AfterEach
  public void tearDown() {
    Path path = Paths.get(TEST_DST_FILE_PATH + "not");
    try {
      Files.deleteIfExists(path);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test

  public void testInitFileBuffer() throws IOException {
    Path src_path = Paths.get(TEST_SRC_FILE_PATH);
    Packet initPacket = PacketBuilder.getInitLoaderPacket(TEST_SRC_FILE_PATH, TEST_DST_FILE_PATH, Files.size(
        src_path));
    fileBuffer.initFileBuffer(initPacket.getPayload());
    assertTrue(initPacket.getHeader().isFlagSet(FLAG.HELLO));
    assertTrue(initPacket.getHeader().isFlagSet(FLAG.DATA));

    assertTrue(fileBuffer.isInitialized);
    assertEquals(TEST_DST_FILE_PATH, fileBuffer.getDst_path());
    assertEquals(Files.size(src_path), fileBuffer.getFileSize());
    assertNotNull(fileBuffer.getByteBuffer());
  }

  @Test
  public void testReceivePacket() throws IOException {
    testInitFileBuffer();
    Packet packet = packetList.get(0);
    fileBuffer.receivePacket(packet.getPayload());

    byte[] bufferPayload = new byte[packet.getPayload().getSize()];
    System.arraycopy(fileBuffer.getByteBuffer().array(), 0, bufferPayload, 0, packet.getPayload().getSize());

    // Assert that the payloads are equal
    assertArrayEquals(packet.getPayload().getByteArray(), bufferPayload);
  }

  @Test
  public void testReceiveFin() throws IOException {
    testInitFileBuffer();
    Payload payload = packetList.get(packetList.size()-1).getPayload();
    fileBuffer.receiveFin(payload);
    assertEquals(payload.getOffsetPointer(), fileBuffer.getFinalOffsetPointer());
  }

  @Test
  public void testResetBuffer() throws IOException {
    testInitFileBuffer();
    fileBuffer.resetBuffer();
    assertNull(fileBuffer.getByteBuffer());
    assertFalse(fileBuffer.isInitialized);
    assertNull(fileBuffer.getDst_path());
    assertEquals(-1, fileBuffer.getFileSize());
    assertEquals(0, fileBuffer.getExpectedOffsetPointer());
    assertEquals(-1, fileBuffer.getFinalOffsetPointer());
  }

  @Test
  public void testWriteBufferToFile() throws IOException {
    testInitFileBuffer();
    int writtenPayloadSize = 0;

    for (Packet packet : packetList) {
      fileBuffer.receivePacket(packet.getPayload());
      writtenPayloadSize += packet.getPayload().getSize();
    }

    fileBuffer.writeBufferToFile();

    Path src_path = Paths.get(TEST_SRC_FILE_PATH);
    Path dst_path = Paths.get(TEST_DST_FILE_PATH);

    assertTrue(Files.exists(dst_path));
    System.out.println(fileBuffer.getByteBuffer().capacity());
    System.out.println(fileBuffer.getFileSize());
    System.out.println(writtenPayloadSize);
    assertArrayEquals(Files.readAllBytes(src_path), Files.readAllBytes(dst_path));
  }

  @Test
  public void testWriteBufferToFileRandomized() throws IOException {
    testInitFileBuffer();
    int writtenPayloadSize = 0;

    Collections.shuffle(packetList);

    for (Packet packet : packetList) {
      fileBuffer.receivePacket(packet.getPayload());
      writtenPayloadSize += packet.getPayload().getSize();
    }
    fileBuffer.writeBufferToFile();

    Path src_path = Paths.get(TEST_SRC_FILE_PATH);
    Path dst_path = Paths.get(TEST_DST_FILE_PATH);

    assertTrue(Files.exists(dst_path));
    System.out.println(fileBuffer.getByteBuffer().capacity());
    System.out.println(fileBuffer.getFileSize());
    System.out.println(writtenPayloadSize);
    assertArrayEquals(Files.readAllBytes(src_path), Files.readAllBytes(dst_path));
  }

}
