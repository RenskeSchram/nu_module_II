package com.nedap.university.networkhost;

import com.nedap.university.packet.Packet;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Host Interface.
 */
public interface Host {
  void service() throws IOException;
  void sendPacket(Packet packet, InetAddress dstAddress, int dstPort) throws IOException;
  boolean isValidPacket(Packet receivedPacket) throws IOException;

}
