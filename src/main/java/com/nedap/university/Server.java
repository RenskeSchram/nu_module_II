package com.nedap.university;

import com.nedap.university.ServiceHandler;
import com.nedap.university.packet.Header.FLAG;
import com.nedap.university.packet.Packet;
import com.nedap.university.utils.Parameters;
import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Server side implementation of AbstractHost: continuously responds to received Packets according to protocol.
 */
public class Server extends AbstractHost {

  public Server(int port) throws SocketException {
    super(port);
  }

  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Syntax: QuoteServer <port>");
      return;
    }

    int port = Integer.parseInt(args[0]);
    System.out.println("starting at port" + port);

    while (true) {
      try {
        Server server = new Server(port);
        server.service();
      } catch (SocketException ex) {
        System.out.println("Socket error: " + ex.getMessage());
      } catch (IOException ex) {
        System.out.println("I/O error: " + ex.getMessage());
      }
    }
  }

  @Override
  void handlePacket(DatagramPacket datagramPacket) throws IOException {
    Packet receivedPacket = new Packet(datagramPacket.getData());

    //Get and send response packet(s)
    List<Packet> responsePackets = serviceHandler.handlePacket(receivedPacket);

    if (!responsePackets.isEmpty()) {
      for (Packet packet : responsePackets) {
        sendPacket(packet, datagramPacket.getAddress(), datagramPacket.getPort());
      }
    }

    outOfOrderPackets.remove(receivedPacket.getHeader().getAckNr());
    lastFrameReceived = receivedPacket.getHeader().getAckNr();
    largestAcceptableFrame = lastFrameReceived + windowSize;

    System.out.println("RECEIVINGWINDOW    LFR: " + lastFrameReceived + " and LAF: " + largestAcceptableFrame);
  }
}

