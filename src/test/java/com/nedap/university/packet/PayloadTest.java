package com.nedap.university.packet;

import static com.nedap.university.utils.Parameters.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PayloadTest {
  private Payload payload;

  @Test
  public void testPayloadFromPaths() throws IOException {
    boolean isFinalPacket = false;
    Path src_path = Paths.get(TEST_SRC_FILE_PATH);
    payload = new Payload(TEST_SRC_FILE_PATH, TEST_DST_FILE_PATH, Files.size(src_path), isFinalPacket);

    assertEquals(TEST_SRC_FILE_PATH, payload.getSrcPath());
    assertEquals(TEST_DST_FILE_PATH, payload.getDstPath());
    long fileSize = Files.size(src_path);
    assertEquals(fileSize, payload.getFileSize());
    assertEquals(isFinalPacket, payload.isFinalPacket());
    assertEquals(TEST_SRC_FILE_PATH.getBytes().length + TEST_DST_FILE_PATH.getBytes().length + String.valueOf(fileSize).getBytes().length + 2, payload.getSize());
    assertEquals(0, payload.getOffsetPointer());
  }

  @Test
  public void testPayloadFromByteArray()  {
    boolean isFinalPacket = false;
    byte[] text = "test~test2~1243".getBytes();
    payload = new Payload(text, 123, isFinalPacket);

    assertEquals(text, payload.getByteArray() );
    assertEquals(isFinalPacket, payload.isFinalPacket());
    assertEquals(123, payload.getOffsetPointer());
  }

  @Test
  public void testGetStringArray() {
    boolean isFinalPacket = false;
    byte[] text = "test~test2~1243".getBytes();
    payload = new Payload(text, 123, isFinalPacket);

    String[] testArray = payload.getStringArray();
    assertEquals(3, testArray.length);
    assertEquals("test", testArray[0]);
    assertEquals("test2", testArray[1]);
    assertEquals("1243", testArray[2]);
  }
}
