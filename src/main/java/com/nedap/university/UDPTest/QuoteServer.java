package com.nedap.university.UDPTest;

import com.nedap.university.ServiceHandler;
import com.nedap.university.packet.Packet;
import com.nedap.university.utils.Parameters;
import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class QuoteServer {

  private DatagramSocket socket;
  private ServiceHandler serviceHandler;
  private boolean inService;

  public QuoteServer(int port) throws SocketException {
    socket = new DatagramSocket(port);
    serviceHandler = new ServiceHandler();
  }

  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Syntax: QuoteServer <port>");
      return;
    }

    int port = Integer.parseInt(args[0]);
    System.out.println("starting at port" + port);

    try {
      QuoteServer server = new QuoteServer(port);
      server.service();
    } catch (SocketException ex) {
      System.out.println("Socket error: " + ex.getMessage());
    } catch (IOException ex) {
      System.out.println("I/O error: " + ex.getMessage());
    }
  }

  private void service() throws IOException {
    inService = true;

    while (inService) {
      System.out.println("service active");
      DatagramPacket request = new DatagramPacket(new byte[Parameters.MAX_PACKET_SIZE], Parameters.MAX_PACKET_SIZE);
      socket.receive(request);
      Packet receivedPacket = new Packet(request.getData());
      System.out.println("received package with flags" + receivedPacket.getHeader().getFlagByte());

      if (isValidPacket(receivedPacket)) {
        List<Packet> responsePackets = serviceHandler.handlePacket(receivedPacket);

        InetAddress clientAddress = request.getAddress();
        int clientPort = request.getPort();

        if (!responsePackets.isEmpty()) {
          for (Packet packet : responsePackets) {
            sendPacket(packet, clientAddress, clientPort);

            // if not ACK packet, set timer
            //  -> createTimer(packet, new Timer(true));
          }
        }
      }
    }
  }

  private boolean isValidPacket(Packet receivedPacket) {
    boolean correctChecksum = true;
    boolean inSlidingWindow = true;
    return correctChecksum && inSlidingWindow;
  }

  private void sendPacket(Packet packet, InetAddress dstAddress, int dstPort) throws IOException {
    DatagramPacket datagramPacket = new DatagramPacket(packet.getByteArray(), packet.getSize(), dstAddress, dstPort);
    socket.send(datagramPacket);
    System.out.println("Packet send with flags" + packet.getHeader().getFlagByte());
  }
}

