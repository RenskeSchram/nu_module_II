package com.nedap.university;

import com.nedap.university.ServiceHandler;
import com.nedap.university.packet.Packet;
import com.nedap.university.utils.Parameters;
import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Server extends AbstractHost {

  private boolean inService;

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

    while(true) {

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

  public void service() throws IOException {
    inService = true;

    while (inService) {
      DatagramPacket request = new DatagramPacket(new byte[Parameters.MAX_PACKET_SIZE], Parameters.MAX_PACKET_SIZE);
      socket.receive(request);
      Packet receivedPacket = new Packet(request.getData());
      System.out.println("received package with flags " + receivedPacket.getHeader().getFlagByte());

      if (isValidPacket(receivedPacket)) {
        // cancel timer
        cancelTimer(receivedPacket.getHeader().getAckNr());

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
}

