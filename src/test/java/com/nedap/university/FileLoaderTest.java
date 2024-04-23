package com.nedap.university;

import com.nedap.university.packet.Header.FLAG;
import com.nedap.university.utils.PacketBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.nedap.university.packet.Header;
import com.nedap.university.packet.Packet;

import java.util.List;

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
    header = packetList.get(packetList.size() -1).getHeader();
    assertTrue(header.isFlagSet(FLAG.FIN));

    // PAYLOAD
    int totalPayloadSize = 0;
    for (Packet packet : packetList) {
      System.out.println(packet.getPayload().getSize());
      totalPayloadSize += packet.getPayload().getSize();
    }
    assertEquals(Files.size(Paths.get(TEST_SRC_FILE_PATH)),totalPayloadSize);
  }

  @Test
  void testGetInitPacket() throws IOException {
    Packet initPacket = PacketBuilder.getInitLoaderPacket(TEST_SRC_FILE_PATH, TEST_DST_FILE_PATH, Files.size(testFile));

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