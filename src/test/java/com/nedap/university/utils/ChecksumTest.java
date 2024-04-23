package com.nedap.university.utils;
import com.nedap.university.packet.Packet;
import com.nedap.university.packet.Payload;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.nedap.university.FileLoaderTest.*;
import static org.junit.jupiter.api.Assertions.*;


public class ChecksumTest {

  @Test
  void testCalculateChecksumConsistency() {
    Packet testAckPacket = PacketBuilder.getAckPacket(2);
    int checksum1 = Checksum.calculateChecksum(testAckPacket);
    int checksum2 = Checksum.calculateChecksum(testAckPacket);
    assertEquals(checksum1, checksum2);
    testAckPacket = PacketBuilder.getAckPacket(4);
    int checksum3 = Checksum.calculateChecksum(testAckPacket);
    assertNotEquals(checksum2, checksum3);
  }

  @Test
  void testVerifyChecksum() {
    Packet testAckPacket2 = PacketBuilder.getAckPacket(2);
    Packet testAckPacket3 = PacketBuilder.getAckPacket(3);
    int checksum2 = Checksum.calculateChecksum(testAckPacket2);
    testAckPacket2.getHeader().setChecksum(checksum2);
    testAckPacket3.getHeader().setChecksum(checksum2);
    assertTrue(Checksum.verifyChecksum(testAckPacket2));
    assertFalse(Checksum.verifyChecksum(testAckPacket3));
  }
}
