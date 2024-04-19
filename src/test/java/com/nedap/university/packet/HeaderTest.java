package com.nedap.university.packet;
import com.nedap.university.packet.Header.FLAG;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.nedap.university.utils.Parameters.HEADER_SIZE;
import static com.nedap.university.utils.Parameters.MAX_PAYLOAD_SIZE;
import static org.junit.jupiter.api.Assertions.*;

public class HeaderTest {

  private Header header;

  @BeforeEach
  void setUp() {
    header = new Header();
  }

  @Test
  void testGetSize() {
    assertEquals(HEADER_SIZE, header.getSize());
  }

  @Test
  void testSetAndGetPayloadDataSize() {
    header.setPayloadDataSize(MAX_PAYLOAD_SIZE);
    assertEquals(MAX_PAYLOAD_SIZE, header.getPayloadDataSize());
  }


  @Test
  void testSetAndGetChecksum() {
    header.setChecksum(789);
    assertEquals(789, header.getChecksum());
  }

  @Test
  void testSetAndGetFlags() {
    header.setFlagByte((byte) 0b00000001);
    assertTrue(header.isFlagSet(Header.FLAG.HELLO));

    header.setFlag(FLAG.GET);
    assertEquals((byte) 0b00000101, header.getFlagByte());
    assertTrue(header.isFlagSet(FLAG.GET));
  }

  @Test
  void testSetWithDataPayload() {
    Payload payload = new Payload(new byte[100], 0, true);
    header.setWithPayload(payload);
    assertEquals(100, header.getPayloadDataSize());
    assertTrue(header.isFlagSet(Header.FLAG.FIN));
    assertTrue(header.isFlagSet(Header.FLAG.DATA));

  }
}
