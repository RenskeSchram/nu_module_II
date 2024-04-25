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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServiceHandlerTest {
  ServiceHandler serviceHandler;

  @BeforeEach
  void setup() throws IOException {
    serviceHandler = new ServiceHandler();
    File src_file = new File(Parameters.TEST_SRC_FILE_PATH);
    File dst_file = new File("example_files/test.pdf");
    copyFile(src_file, dst_file);
  }

  @Test
  void testHELLODELETE() throws IOException, InterruptedException {
    // correct
    List<Packet> packetList = serviceHandler.handlePacket(PacketBuilder.helloDeletePacket("example_files/test.pdf"));
    byte[] succeeded = packetList.get(0).getPayload().getByteArray();
    assertArrayEquals(new byte[]{(byte) 1}, succeeded);
    assertFalse(Files.exists(Paths.get("example_files/test.pdf")));

    // incorrect: file does not exist
    boolean error = false;
    try {
      packetList = serviceHandler.handlePacket(PacketBuilder.helloDeletePacket("example_files/test.pdf"));
    } catch (Exception e) {
      error = true;
    }
    succeeded = packetList.get(0).getPayload().getByteArray();
    assertArrayEquals(new byte[]{(byte) 0}, succeeded);
    assertFalse(error);
  }

  private static void copyFile(File src_file, File dst_file) throws IOException {
    try (InputStream is = new FileInputStream(src_file); OutputStream os = new FileOutputStream(
        dst_file)) {
      byte[] buffer = new byte[1024];
      int length;
      while ((length = is.read(buffer)) > 0) {
        os.write(buffer, 0, length);
      }
    }
  }

}
