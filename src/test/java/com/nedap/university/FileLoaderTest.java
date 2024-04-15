package com.nedap.university;

import com.nedap.university.packet.Header.FLAG;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.nedap.university.packet.Header;
import com.nedap.university.packet.Packet;
import com.nedap.university.packet.Payload;

import java.util.List;

public class FileLoaderTest {

  private FileLoader fileLoader;
  private File testFile;

  @BeforeEach
  void setUp() {
    fileLoader = new FileLoader();
    testFile = new File("example_files/tiny.pdf");
  }

  @Test
  void testExtractPackets() {
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
}