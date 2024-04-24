package com.nedap.university.utils;

import com.nedap.university.packet.Packet;
import com.nedap.university.packet.Payload;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class PacketBuilderTest {

  @Test
  void testGetAckPacket() {
    int AckNr = 2;
    Packet ackPacket = PacketBuilder.getAckPacket(AckNr);
    assertEquals(ackPacket.getSize(), Parameters.HEADER_SIZE + 1);
    assertArrayEquals(ackPacket.getPayload().getByteArray(), new byte[1]);
    assertEquals(ackPacket.getHeader().getAckNr(), AckNr);
    // ACK
    assertEquals(ackPacket.getHeader().getFlagByte(), 0b00010000);
  }

  @Test
  void testGetListPacket() {
    // not allow traveling back in dir
    Payload payload = new Payload("example_files/".getBytes(), 0, false);
    Packet listPacket = PacketBuilder.listPacket(payload);
    payload = new Payload("example_files/...".getBytes(), 0, false);
    Packet listPacketII = PacketBuilder.listPacket(payload);
    assertArrayEquals(listPacket.getByteArray(), listPacketII.getByteArray());
    // GET + LIST
    assertEquals(listPacket.getHeader().getFlagByte(), 0b00101000);

  }

  @Test
  void testGetHelloListPacket() {
    Packet hellolistPacket = PacketBuilder.helloListPacket("example_files/");
    assertEquals(hellolistPacket.getSize(),
        Parameters.HEADER_SIZE + "example_files/".getBytes().length);
    assertArrayEquals(hellolistPacket.getPayload().getByteArray(), "example_files/".getBytes());
    // HELLO + LIST
    assertEquals(hellolistPacket.getHeader().getFlagByte(), 0b00001100);
  }

  @Test
  void testGetInitBuilderPacket() {
    Packet initbuilderPacket = PacketBuilder.getInitBuilderPacket("example_files/tiny.pdf",
        "example_files//tiny.pdf");
    assertArrayEquals(initbuilderPacket.getPayload().getByteArray(),
        "example_files/tiny.pdf~example_files//tiny.pdf~0".getBytes());
    // HELLO + GET
    assertEquals(initbuilderPacket.getHeader().getFlagByte(), 0b00000101);
  }

  @Test
  void testGetInitLoaderPacket() throws IOException {
    Packet initloaderPacket = PacketBuilder.getInitLoaderPacket("example_files/tiny.pdf",
        "example_files//tiny.pdf",
        Files.size(Paths.get("example_files/tiny.pdf")));
    System.out.println(new String(initloaderPacket.getPayload().getByteArray()));
    assertArrayEquals(initloaderPacket.getPayload().getByteArray(),
        "example_files/tiny.pdf~example_files//tiny.pdf~24286".getBytes());
    // HELLO + DATA
    assertEquals(initloaderPacket.getHeader().getFlagByte(), 0b00000011);
  }

  @Test
  void testNoSuchFilePacket() {
    Packet noSuchFilePacket = PacketBuilder.getNoSuchFilePacket();
    assertArrayEquals(noSuchFilePacket.getPayload().getByteArray(), "NoSuchFile".getBytes());
    // ERROR + FIN
    assertEquals(noSuchFilePacket.getHeader().getFlagByte(), 0b01100000);
  }
}
