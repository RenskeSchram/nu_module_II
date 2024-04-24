package com.nedap.university.packet;

import com.nedap.university.utils.PacketBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.nedap.university.utils.Parameters.HEADER_SIZE;
import static com.nedap.university.utils.Parameters.MAX_PAYLOAD_SIZE;
import static org.junit.jupiter.api.Assertions.*;

public class PacketTest {
  @Test
  void testPacketFromArray() {
    Packet ackPacket = PacketBuilder.getAckPacket(1);
    byte[] ackPacketArray = ackPacket.getByteArray();

    Packet testPacket = new Packet(ackPacketArray);
    assertArrayEquals(ackPacket.getByteArray(), testPacket.getByteArray());
    assertEquals(ackPacket.getSize(), testPacket.getSize());
    assertArrayEquals(ackPacket.getHeader().getByteArray(), testPacket.getHeader().getByteArray());
    assertArrayEquals(ackPacket.getPayload().getByteArray(), testPacket.getPayload().getByteArray());

  }
  @Test
  void testPacketFromHeaderPayload() {
    Packet ackPacket = PacketBuilder.getAckPacket(1);
    Packet testPacket = new Packet(ackPacket.getHeader(), ackPacket.getPayload());
    assertArrayEquals(ackPacket.getByteArray(), testPacket.getByteArray());
    assertEquals(ackPacket.getSize(), testPacket.getSize());
    assertArrayEquals(ackPacket.getHeader().getByteArray(), testPacket.getHeader().getByteArray());
    assertArrayEquals(ackPacket.getPayload().getByteArray(), testPacket.getPayload().getByteArray());
  }

}

