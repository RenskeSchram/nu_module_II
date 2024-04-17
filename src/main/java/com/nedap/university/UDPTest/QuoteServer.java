package com.nedap.university.UDPTest;

import com.nedap.university.PacketHandler;
import com.nedap.university.packet.Header;
import com.nedap.university.packet.Packet;
import com.nedap.university.packet.Payload;
import com.nedap.university.utils.Parameters;
import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
 
/**
 * This program demonstrates how to implement a UDP server program.
 *
 *
 * @author www.codejava.net
 */
public class QuoteServer {

  private DatagramSocket socket;
  private List<String> listQuotes = new ArrayList<String>();
  private Random random;
  private PacketHandler packetHandler;
  private List<Packet> packetBuffer;

  public QuoteServer(int port) throws SocketException {
    socket = new DatagramSocket(port);
    random = new Random();
    packetHandler = new PacketHandler();
  }

  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println("Syntax: QuoteServer <file> <port>");
      return;
    }

    int port = Integer.parseInt(args[1]);

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
    Path currentRelativePath = Paths.get("");
    String s = currentRelativePath.toAbsolutePath().toString();
    System.out.println("Current absolute path is: " + s);

    while (true) {
      DatagramPacket request = new DatagramPacket(new byte[Parameters.MAX_PACKET_SIZE], Parameters.MAX_PACKET_SIZE);
      socket.receive(request);
      System.out.println("received package");
      System.out.println(new String(request.getData(), 0, request.getLength()));

      InetAddress clientAddress = request.getAddress();
      int clientPort = request.getPort();

      Packet receivedPacket = new Packet(request.getData());
      Packet responsePacket = packetHandler.getAckPacket(receivedPacket.getHeader().getAckNr());
      socket.send(new DatagramPacket(responsePacket.getByteArray(), responsePacket.getSize(), clientAddress, clientPort));

      packetHandler.handlePacket(receivedPacket);
    }
  }
}

