package com.nedap.university;


import com.nedap.university.packet.Packet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PacketHandlerTest {

  private FileLoader fileLoader;
  private PacketHandler packetHandler;
  private List<Packet> packetList;

  static final String TEST_SRC_FILE_PATH = "example_files/tiny.pdf";
  static final String TEST_DST_FILE_PATH = "example_files/test_file.pdf";

  @BeforeEach
  public void setUp() throws IOException {
    fileLoader = new FileLoader();
    packetHandler = new PacketHandler();
    packetList = fileLoader.extractPackets(Paths.get(TEST_SRC_FILE_PATH));
  }

  @Test
  public void testPacketHandler() throws IOException {
    Packet initPacket = fileLoader.getInitPacket(TEST_DST_FILE_PATH, Files.size(Paths.get(TEST_SRC_FILE_PATH)));
    packetHandler.handlePacket(initPacket);

    for (Packet packet: packetList) {
      packetHandler.handlePacket(packet);
    }
  }

}
