package com.nedap.university.packet;
import com.nedap.university.packet.Header;
import com.nedap.university.packet.Header.FLAG;
import com.nedap.university.packet.Payload;
import com.nedap.university.utils.Parameters;
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
    header.setFlags((byte) 0b00000001);
    assertTrue(header.isFlagSet(Header.FLAG.HELLO));

    header.setFlag(FLAG.GET);
    assertEquals((byte) 0b00000101, header.getFlags());
    assertTrue(header.isFlagSet(FLAG.GET));
  }

  @Test
  void testSetWithDataPayload() {
    Payload payload = new Payload(new byte[100], 0, true);
    header.setWithDataPayload(payload);
    assertEquals(100, header.getPayloadDataSize());
    assertTrue(header.isFlagSet(Header.FLAG.FIN));
    assertTrue(header.isFlagSet(Header.FLAG.DATA));

  }
}
