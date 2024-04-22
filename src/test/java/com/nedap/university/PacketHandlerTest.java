package com.nedap.university;


import com.nedap.university.packet.Packet;
import com.nedap.university.packet.PacketBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PacketHandlerTest {

  private FileLoader fileLoader;
  private ServiceHandler serviceHandler;
  private List<Packet> packetList;

  static final String TEST_SRC_FILE_PATH = "example_files/tiny.pdf";
  static final String TEST_DST_FILE_PATH = "example_files/test_file.pdf";

  @BeforeEach
  public void setUp() throws IOException {
    fileLoader = new FileLoader();
    serviceHandler = new ServiceHandler();
    packetList = fileLoader.extractPackets(Paths.get(TEST_SRC_FILE_PATH));
  }

  @Test
  public void testPacketHandler() throws IOException {
    Packet initPacket = PacketBuilder.getInitLoaderPacket(TEST_SRC_FILE_PATH, TEST_DST_FILE_PATH, Files.size(Paths.get(TEST_SRC_FILE_PATH)));
    serviceHandler.handlePacket(initPacket);

    for (Packet packet: packetList) {
      serviceHandler.handlePacket(packet);
    }
  }

}
