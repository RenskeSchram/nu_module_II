package com.nedap.university.UDPTest;

import com.nedap.university.packet.Header;
import com.nedap.university.packet.Packet;
import com.nedap.university.packet.Payload;
import com.nedap.university.utils.PacketParser;
import com.nedap.university.utils.Parameters;
import java.io.*;
import java.net.*;
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

  public QuoteServer(int port) throws SocketException {
    socket = new DatagramSocket(port);
    random = new Random();
  }

  public static void main(String[] args) {

    if (args.length < 2) {
      System.out.println("Syntax: QuoteServer <file> <port>");
      return;
    }

    String quoteFile = args[0];
    int port = Integer.parseInt(args[1]);

    try {
      QuoteServer server = new QuoteServer(port);
      server.loadQuotesFromFile(quoteFile);
      server.service();
    } catch (SocketException ex) {
      System.out.println("Socket error: " + ex.getMessage());
    } catch (IOException ex) {
      System.out.println("I/O error: " + ex.getMessage());
    }
  }

  private void service() throws IOException {
    while (true) {
      DatagramPacket request = new DatagramPacket(new byte[Parameters.MAX_PACKET_SIZE], Parameters.MAX_PACKET_SIZE);
      socket.receive(request);
      System.out.println("received package");

      Packet packet = PacketParser.byteArrayToPacket(request.getData());

      InetAddress clientAddress = request.getAddress();
      int clientPort = request.getPort();

      Packet repsonsePacket = getAckPacket(packet);

      DatagramPacket response = new DatagramPacket(packet.getByteArray(), packet.getByteArray().length, clientAddress, clientPort);
      socket.send(response);

    }
  }

  private Packet getAckPacket(Packet request) {
    Payload payload = new Payload(new byte[1], 0, true);

    Header header = new Header(payload);
    header.setAckNr(request.getHeader().getAckNr());
    System.out.println("sending package with Ack: " + request.getHeader().getAckNr());

    return new Packet(header, payload);
  }

  private void loadQuotesFromFile(String quoteFile) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(quoteFile));
    String aQuote;

    while ((aQuote = reader.readLine()) != null) {
      listQuotes.add(aQuote);
    }

    reader.close();
  }

  private String getRandomQuote() {
    int randomIndex = random.nextInt(listQuotes.size());
    return listQuotes.get(randomIndex);
  }
}

