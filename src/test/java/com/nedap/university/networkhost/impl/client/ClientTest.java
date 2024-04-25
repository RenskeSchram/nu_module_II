package com.nedap.university.networkhost.impl.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nedap.university.packet.Packet;
import com.nedap.university.utils.PacketBuilder;
import com.nedap.university.utils.Parameters;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClientTest {

  Client client;

  @BeforeEach
  void setup() throws UnknownHostException, SocketException {
    client = new Client(InetAddress.getByName("localhost"), 6534);
  }

  @Test
  void testSetFinalReceivingAck() throws IOException {
    Packet packet = PacketBuilder.getInitLoaderPacket("example_files/tiny.pdf", "tiny.pdf",
        Files.size(Paths.get("example_files/tiny.pdf")));
    packet.getHeader().setAckNr(123);
    client.setFinalReceivingAck(packet);
    assertEquals((int) Math.ceil(
            (double) packet.getPayload().getFileSize() / Parameters.MAX_PAYLOAD_SIZE) + 123,
        client.finalReceivingAck);

    packet = PacketBuilder.getNoSuchFilePacket();
    packet.getHeader().setAckNr(123);
    client.setFinalReceivingAck(packet);
    assertEquals(client.finalReceivingAck, 123);
  }
}
