package com.nedap.university;

import com.nedap.university.packet.Packet;
import com.nedap.university.utils.PacketParser;
import com.nedap.university.utils.Parameters;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;

public class Client {

  private final FileLoader fileLoader;
  private final PacketQueue queue;

  static final int port = 8080;
  static final String host = "172.16.1.1";
  private DatagramSocket socket;

  Client() throws SocketException {
    fileLoader = new FileLoader();
    queue = new PacketQueue();
    socket = new DatagramSocket(port);
  }

  public void uploadFile(String FILE_DIR) throws InterruptedException, IOException {
    File file = new File(FILE_DIR);

    List<Packet> packetList = fileLoader.extractPackets(file);
    for (Packet packet : packetList) {
      queue.putPacket(packet);
    }

    service();
  }

  public void downloadFile(String FILE_DIR) {
    // send GET packet

    // retrieve packets

    // correctly assemble packets in order
  }

  private void service() throws IOException, InterruptedException {
    int AckNr = 0;

    sendNextPacket(InetAddress.getByName(host), port, AckNr);
    
    while (true) {
      byte[] buffer = new byte[Parameters.MAX_PACKET_SIZE];
      DatagramPacket response = new DatagramPacket(buffer, buffer.length);

      socket.receive(response);
      Thread.sleep(500);

      Packet receivedPacket = PacketParser.byteArrayToPacket(response.getData());
      System.out.println("Received packet with ACK: " + receivedPacket.getHeader().getAckNr());

      if (AckNr == receivedPacket.getHeader().getAckNr()) {
        AckNr++;

        InetAddress clientAddress = response.getAddress();
        int clientPort = response.getPort();

        // get next packet
        if (queue.packetQueue.isEmpty()) {
          System.out.println("All packets are send");
          break;
        } else {
          sendNextPacket(clientAddress, clientPort, AckNr);
        }

      } else {
        System.out.println("Incorrect package received");
      }


    }
  }

  private void sendNextPacket(InetAddress address, int port, int AckNr) throws IOException {
    // get packet from queue
    Packet packet = queue.removePacket();

    // set ack
    packet.getHeader().setAckNr(AckNr);

    // send packet
    DatagramPacket datagramPacket = new DatagramPacket(packet.getByteArray(), packet.getByteArray().length, address, port);
    socket.send(datagramPacket);
    System.out.println("Packet send with ACK: " + AckNr);
  }

  public FileLoader getFileLoader() {
    return fileLoader;
  }

  public void getList(String s) {
  }
}

