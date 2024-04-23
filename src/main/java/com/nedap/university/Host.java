package com.nedap.university;

import com.nedap.university.packet.Packet;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public interface Host {

  default void service() throws IOException, InterruptedException {};

  default void sendPacket(Packet packet, InetAddress dstAddress, int dstPort) throws IOException {}

  boolean isValidPacket(Packet receivedPacket) throws IOException;

}
