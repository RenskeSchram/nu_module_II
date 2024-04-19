package com.nedap.university;

import com.nedap.university.packet.Header.FLAG;
import com.nedap.university.packet.Packet;
import com.nedap.university.utils.Parameters;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;

public abstract class AbstractHost implements Host {
  protected DatagramSocket socket;
  protected ServiceHandler serviceHandler;
  protected boolean inService;
  private int finalServiceAck;

  public AbstractHost(int port) throws SocketException {
    socket = new DatagramSocket(port);
    serviceHandler = new ServiceHandler();
    inService = false;
  }

  @Override
  public void service() throws IOException {}

  @Override
  public void sendPacket(Packet packet, InetAddress dstAddress, int dstPort) throws IOException {
    DatagramPacket datagramPacket = new DatagramPacket(packet.getByteArray(), packet.getSize(), dstAddress, dstPort);
    socket.send(datagramPacket);
    System.out.println("packet send");
  }

  @Override
  public boolean isValidPacket(Packet receivedPacket) {
    boolean correctChecksum = true;
    boolean inSlidingWindow = true;
    return correctChecksum && inSlidingWindow;
  }

}
