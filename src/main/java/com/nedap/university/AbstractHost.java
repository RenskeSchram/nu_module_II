package com.nedap.university;

import static com.nedap.university.utils.Parameters.TIMEOUTDURATION;

import com.nedap.university.packet.Header.FLAG;
import com.nedap.university.packet.Packet;
import com.nedap.university.utils.Checksum;
import com.nedap.university.utils.Parameters;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
    packet.getHeader().setChecksum(Checksum.calculateChecksum(packet));
    DatagramPacket datagramPacket = new DatagramPacket(packet.getByteArray(), packet.getSize(), dstAddress, dstPort);
    socket.send(datagramPacket);
  }

  @Override
  public boolean isValidPacket(Packet receivedPacket) {
    boolean correctChecksum = Checksum.verifyChecksum(receivedPacket);
    boolean inSlidingWindow = true;
    return correctChecksum && inSlidingWindow;
  }

}
