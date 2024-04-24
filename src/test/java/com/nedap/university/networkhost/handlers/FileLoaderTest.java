package com.nedap.university.networkhost.handlers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nedap.university.packet.Header;
import com.nedap.university.packet.Header.FLAG;
import com.nedap.university.packet.Packet;
import com.nedap.university.utils.PacketBuilder;
import com.nedap.university.utils.Parameters;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FileLoaderTest {

  private FileLoader fileLoader;
  private Path testFile;
  static final String TEST_SRC_FILE_PATH = "example_files/large.pdf";
  static final String TEST_DST_FILE_PATH = "example_files/test_file.pdf";

  @BeforeEach
  void setUp() {
    fileLoader = new FileLoader();
    testFile = Paths.get(TEST_SRC_FILE_PATH);
  }

  @Test
  void testExtractPackets() throws IOException {
    List<Packet> packetList = fileLoader.extractPackets(testFile);

    // List not empty
    assertNotNull(packetList);
    assertFalse(packetList.isEmpty());

    // HEADER
    // assert setting a FLAG
    Header header = packetList.get(0).getHeader();
    header.setFlag(FLAG.HELLO);
    assertTrue(header.isFlagSet(FLAG.HELLO));
    header = packetList.get(packetList.size() - 1).getHeader();
    assertTrue(header.isFlagSet(FLAG.FIN));

    // PAYLOAD
    int totalPayloadSize = 0;
    for (Packet packet : packetList) {
      System.out.println(packet.getPayload().getSize());
      totalPayloadSize += packet.getPayload().getSize();
    }
    assertEquals(Files.size(Paths.get(TEST_SRC_FILE_PATH)), totalPayloadSize);
  }

  @Test
  void testInitFileLoading() throws IOException {
    Packet packet = fileLoader.initFileLoading("wrong", "dst");
    assertArrayEquals(packet.getByteArray(), PacketBuilder.getNoSuchFilePacket().getByteArray());

    packet = fileLoader.initFileLoading("example_files/tiny.pdf", "dst");
    assertTrue(fileLoader.isInitiated());
    assertEquals(fileLoader.src_path, Paths.get("example_files/tiny.pdf"));

  }

  @Test
  void testExtractNextPacket() throws IOException {
    Packet packet = fileLoader.initFileLoading("example_files/tiny.pdf", "dst");
    Packet packet0 = fileLoader.extractNextPacket();
    assertEquals(0, packet0.getHeader().getOffsetPointer());
    Packet packet1 = fileLoader.extractNextPacket();
    assertEquals(1, packet1.getHeader().getOffsetPointer());
    assertEquals(Parameters.MAX_PAYLOAD_SIZE, packet1.getHeader().getPayloadDataSize());
    while (fileLoader.isInitiated()) {
      packet = fileLoader.extractNextPacket();
    }
    assertTrue(Parameters.MAX_PAYLOAD_SIZE > packet.getHeader().getPayloadDataSize());
  }

  @Test
  void testGetInitPacket() throws IOException {
    Packet initPacket = PacketBuilder.getInitLoaderPacket(TEST_SRC_FILE_PATH, TEST_DST_FILE_PATH,
        Files.size(testFile));

    assertEquals((byte) 0b00000011, initPacket.getHeader().getFlagByte());

    assertTrue(initPacket.getHeader().isFlagSet(FLAG.HELLO));
    assertTrue(initPacket.getHeader().isFlagSet(FLAG.DATA));

    String[] payloadStrings = initPacket.getPayload().getStringArray();
    assert payloadStrings != null;
    assertEquals(TEST_SRC_FILE_PATH, payloadStrings[0]);
    assertEquals(TEST_DST_FILE_PATH, payloadStrings[1]);
    assertEquals(Files.size(testFile), Integer.parseInt(payloadStrings[2]));
  }
}