package com.nedap.university;

import static com.nedap.university.utils.Parameters.MAX_RETRIES;
import static com.nedap.university.utils.Parameters.TIMEOUT_DURATION;

import com.nedap.university.packet.Header.FLAG;
import com.nedap.university.packet.Packet;
import com.nedap.university.utils.Checksum;
import com.nedap.university.utils.Parameters;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public abstract class AbstractHost implements Host {

  protected DatagramSocket socket;
  protected ServiceHandler serviceHandler;
  protected boolean inService;
  protected HashMap<Integer, Timer> unacknowledgedPackets;
  private int finalServiceAck;

  public AbstractHost(int port) throws SocketException {
    socket = new DatagramSocket(port);
    serviceHandler = new ServiceHandler();
    inService = false;
    unacknowledgedPackets = new HashMap<>();
  }

  @Override
  public void service() throws IOException {}

  @Override
  public void sendPacket(Packet packet, InetAddress dstAddress, int dstPort) throws IOException {
    packet.getHeader().setChecksum(Checksum.calculateChecksum(packet));
    DatagramPacket datagramPacket = new DatagramPacket(packet.getByteArray(), packet.getSize(), dstAddress, dstPort);

    if (!packet.getHeader().isFlagSet(FLAG.ACK)) {
      setTimer(datagramPacket, packet.getHeader().getAckNr());
    }

    socket.send(datagramPacket);
  }

  @Override
  public boolean isValidPacket(Packet receivedPacket) {
    boolean correctChecksum = Checksum.verifyChecksum(receivedPacket);
    boolean inSlidingWindow = true;
    return correctChecksum && inSlidingWindow;
  }

  public synchronized void setTimer(DatagramPacket datagramPacket, int ackNr) {
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
      private int retries = 0;

      @Override
      public void run() {
        try {
          if (retries < MAX_RETRIES) {
            socket.send(datagramPacket);
            retries++;
          } else {
            // Handle maximum retries exceeded
            System.out.println("Maximum retries exceeded for packet with ACK number: " + ackNr);
            cancelTimer(ackNr);
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    };
    timer.schedule(task, TIMEOUT_DURATION);
    unacknowledgedPackets.put(ackNr, timer);
  }

  public synchronized void cancelTimer(int ackNr) {
    Timer timer = unacknowledgedPackets.get(ackNr);
    if (timer != null) {
      timer.cancel();
      unacknowledgedPackets.remove(ackNr);
    }
  }
}