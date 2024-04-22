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

      if (isValidPacket(receivedPacket)) {
        InetAddress clientAddress = request.getAddress();
        int clientPort = request.getPort();

        // cancel timer
        cancelTimer(receivedPacket.getHeader().getAckNr());
        //send ACK
        if (receivedPacket.getHeader().getFlagByte() != 0b00010000) {
          sendPacket(ServiceHandler.getAckPacket(receivedPacket.getHeader().getAckNr()), clientAddress, clientPort);
        }

        if (!outOfOrderPackets.containsKey(receivedPacket.getHeader().getAckNr())) {
          if (receivedPacket.getHeader().getAckNr() == lastFrameReceived + 1) {
            handlePacket(request);
            checkOutOfOrderPackets();
          } else {
            System.out.println("packet put on hold");
            outOfOrderPackets.put(receivedPacket.getHeader().getAckNr(), request);
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
  public boolean isValidPacket(Packet receivedPacket) {
    System.out.println("PACKET received with ACK nr: " + receivedPacket.getHeader().getAckNr() + " and flags " + receivedPacket.getHeader().getFlagByte());

    boolean correctChecksum = Checksum.verifyChecksum(receivedPacket);
    boolean inReceivingWindow = withinWindow(receivedPacket.getHeader().getAckNr());
    return correctChecksum && inReceivingWindow;
  }

  private boolean withinWindow(int receivedAck) {
    if (largestAcceptableFrame < lastFrameReceived){
      return 0 <= receivedAck && receivedAck <= largestAcceptableFrame || lastFrameReceived <= receivedAck && receivedAck <= 255;
    }else{
      return lastFrameReceived <= receivedAck && receivedAck <= largestAcceptableFrame;
    }
  }

  void checkOutOfOrderPackets() throws IOException {
    if (outOfOrderPackets.containsKey(lastFrameReceived + 1)) {
      handlePacket(outOfOrderPackets.get(lastFrameReceived + 1));
      checkOutOfOrderPackets();
    }
  }

  void handlePacket(DatagramPacket datagramPacket) throws IOException {}




  public synchronized void setTimer(DatagramPacket datagramPacket, int ackNr) {
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
      private int retries = 0;

      @Override
      public void run() {
        try {
          if (retries < MAX_RETRIES) {
            System.out.println("TIMER RUN OUT");
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