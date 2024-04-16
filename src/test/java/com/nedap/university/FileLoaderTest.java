package com.nedap.university;

import com.nedap.university.packet.Header.FLAG;
import com.nedap.university.utils.PacketParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.nedap.university.packet.Header;
import com.nedap.university.packet.Packet;
import com.nedap.university.packet.Payload;

import java.util.List;

public class FileLoaderTest {

  private FileLoader fileLoader;
  private Path testFile;
  private static final String TEST_SRC_FILE_PATH = "example_files/tiny.pdf";
  private static final String TEST_DST_FILE_PATH = "example_files/test_file.pdf";

  @BeforeEach
  void setUp() {
    fileLoader = new FileLoader();
    testFile = Paths.get("example_files/tiny.pdf");
  }

  @Test
  void testExtractPackets() throws IOException {
    List<Packet> packetList = fileLoader.extractPackets(testFile);

    // assert List not empty
    assertNotNull(packetList);
    assertFalse(packetList.isEmpty());

    // HEADER
    // assert setting a FLAG
    Header header = packetList.get(0).getHeader();
    header.setFlag(FLAG.HELLO);
    assertTrue(header.isFlagSet(FLAG.HELLO));

    // PAYLOAD
  }

  @Test
  void testGetInitPacket() throws IOException {
    Packet initPacket = fileLoader.getInitPacket(TEST_DST_FILE_PATH, Files.size(testFile));

    assertTrue(initPacket.getHeader().isFlagSet(FLAG.HELLO));
    assertTrue(initPacket.getHeader().isFlagSet(FLAG.DATA));

    String[] payloadStrings = PacketParser.getStringPayload(PacketParser.packetToByteArray(initPacket));
    assertEquals(TEST_SRC_FILE_PATH, payloadStrings[0]);
  }
}