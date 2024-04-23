package com.nedap.university;

import static com.nedap.university.utils.Parameters.MAX_RETRIES;
import static com.nedap.university.utils.Parameters.TIMEOUT_DURATION;

import com.nedap.university.packet.Header.FLAG;
import com.nedap.university.packet.Packet;
import com.nedap.university.utils.LoggingHandler;
import com.nedap.university.utils.PacketBuilder;
import com.nedap.university.utils.Checksum;
import com.nedap.university.utils.Parameters;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


/**
 * Abstract Host class, handles retrieving and sending of Packets.
 * Receiving packets with
 *  (1) acknowledging of all correct incoming packets (valid checksum and withing receiving window)
 *  (2) storing out of order packets before pushing them to the next layer (ServiceHandler)
 *  (3) pushing correct packets to the ServiceHandler
 *
 * Sending packets with
 *  (1) adding the correct Checksum, InetAdress and Port to create and send the Packet as DatagramPacket.
 *  (2) handling the timer to resend timed out packets
 */
public abstract class AbstractHost implements Host {

  protected DatagramSocket socket;
  protected ServiceHandler serviceHandler;
  protected boolean inService;

  int windowSize = 3;
  int lastFrameReceived = -1;
  int largestAcceptableFrame = lastFrameReceived + windowSize;
  HashMap<Integer, DatagramPacket> outOfOrderPackets = new HashMap<>();

  protected HashMap<Integer, Timer> unacknowledgedPackets;

  public AbstractHost(int port) throws SocketException {
    socket = new DatagramSocket(port);
    serviceHandler = new ServiceHandler();
    inService = false;
    unacknowledgedPackets = new HashMap<>();
  }


  public void service() throws IOException {
    inService = true;

    while (inService) {
      DatagramPacket request = new DatagramPacket(new byte[Parameters.MAX_PACKET_SIZE], Parameters.MAX_PACKET_SIZE);
      socket.receive(request);
      Packet receivedPacket = new Packet(request.getData());
      int receivedAck = receivedPacket.getHeader().getAckNr();

      if (isValidPacket(receivedPacket)) {
        InetAddress clientAddress = request.getAddress();
        int clientPort = request.getPort();

        cancelTimer(receivedAck);
        // only send Ack for !Ack-packets.
        if (receivedPacket.getHeader().getFlagByte() != 0b00010000) {
          sendPacket(PacketBuilder.getAckPacket(receivedAck), clientAddress, clientPort);
        }

        if (!outOfOrderPackets.containsKey(receivedAck)) {
          if (receivedAck == lastFrameReceived + 1) {
            handlePacket(request);
            checkOutOfOrderPackets();
          } else {
            System.out.println("packet put on hold with ack:" + receivedAck);
            outOfOrderPackets.put(receivedAck, request);
          }
        }
      } else {
        System.out.println("Invalid packet");
      }
    }
  }

  @Override
  public void sendPacket(Packet packet, InetAddress dstAddress, int dstPort) throws IOException {
    packet.getHeader().setChecksum(Checksum.calculateChecksum(packet));
    DatagramPacket datagramPacket = new DatagramPacket(packet.getByteArray(), packet.getSize(), dstAddress, dstPort);

    if (!packet.getHeader().isFlagSet(FLAG.ACK)) {
      setTimer(datagramPacket, packet.getHeader().getAckNr());
    }
    System.out.println("PACKET send with ACK nr: " + packet.getHeader().getAckNr()+ " and flags " + packet.getHeader().getFlagByte());

    socket.send(datagramPacket);
  }

  @Override
  public boolean isValidPacket(Packet receivedPacket) throws IOException {
    System.out.println("PACKET received with ACK nr: " + receivedPacket.getHeader().getAckNr() + " and flags " + receivedPacket.getHeader().getFlagByte());

    boolean correctChecksum = Checksum.verifyChecksum(receivedPacket);
    if (!correctChecksum) {
      System.out.println("false checksum");
    }
    boolean inReceivingWindow = withinWindow(receivedPacket.getHeader().getAckNr());
    if (!inReceivingWindow) {
      System.out.println("false inReceivingWindow");
    }
    return correctChecksum && inReceivingWindow;
  }

  private boolean withinWindow(int receivedAck) {
      return lastFrameReceived <= receivedAck && receivedAck <= largestAcceptableFrame;
  }

  void updateLastFrameReceived(int AckNr) throws IOException {
    lastFrameReceived = AckNr;
    largestAcceptableFrame = lastFrameReceived + windowSize;
    //System.out.println("RECEIVINGWINDOW    LFR: " + lastFrameReceived + " and LAF: " + largestAcceptableFrame);
  }

  void checkOutOfOrderPackets() throws IOException {
    if (outOfOrderPackets.containsKey(lastFrameReceived + 1)) {
      handlePacket(outOfOrderPackets.get(lastFrameReceived + 1));
      checkOutOfOrderPackets();
    }
  }

  abstract void handlePacket(DatagramPacket datagramPacket) throws IOException;

  /**
   * Timer function to resend lost or delayed Packets.
   * @param datagramPacket packet to follow and possibly resend
   * @param ackNr Ack Nr of packet to follow
   */
  public synchronized void setTimer(DatagramPacket datagramPacket, int ackNr) {
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
      private int retries = 0;

      @Override
      public void run() {
        try {
          if (retries < MAX_RETRIES) {
            System.out.println("TIMER RUN OUT for packet with ack: " + ackNr);
            socket.send(datagramPacket);
            System.out.println("resending packet to " + datagramPacket.getAddress() + " " + datagramPacket.getPort());
            retries++;
          } else {
            System.out.println("MAX RETRIES EXCEEDED for packet with ACK number: " + ackNr);
            cancelTimer(ackNr);
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    };
    timer.scheduleAtFixedRate(task,  TIMEOUT_DURATION, TIMEOUT_DURATION);
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