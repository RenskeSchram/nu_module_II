package com.nedap.university.networkhost;

import com.nedap.university.packet.Packet;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Host Interface for (1) sending/retrieving DatagramPackets and (2) acknowledging valid Packets.
 */
public interface Host {

  /**
   * Service function for continuous handling of the networking(service) of this host.
   * @throws IOException if I/0 exception occurs for this host
   */
  void service() throws IOException;

  /**
   * Send Packet as DatagramPacket.
   * @param packet packet to send
   * @param dstAddress IP of destination host
   * @param dstPort port of destination host
   * @throws IOException if I/0 exception occurs
   */
  void sendPacket(Packet packet, InetAddress dstAddress, int dstPort) throws IOException;

  /**
   * Verify if packet is valid and should be acknowledged.
   * @param receivedPacket packet to be verified.
   * @return true of valid packet.
   * @throws IOException if I/0 exception occurs
   */
  boolean isValidPacket(Packet receivedPacket) throws IOException;

}
