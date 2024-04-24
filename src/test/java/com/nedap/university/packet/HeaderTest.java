package com.nedap.university.packet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nedap.university.packet.Header.FLAG;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HeaderTest {

  private Header header;

  @BeforeEach
  void setUp() {
    header = new Header();
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
  }

  @Test
  void testFlags() {
    header.setFlag(FLAG.FIN);
    assertTrue(header.isFlagSet(Header.FLAG.FIN));
    // repetition should not remove
    header.setFlag(FLAG.FIN);
    assertTrue(header.isFlagSet(Header.FLAG.FIN));
    // add extra
    header.setFlag(FLAG.HELLO);
    assertEquals(header.getFlagByte(), (byte) 0b00100001);
  }
}
