package com.nedap.university;

import com.nedap.university.packet.Packet;

import com.nedap.university.utils.Parameters;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;

public class Client {

  private final FileLoader fileLoader;
  private final PacketQueue queue;

  static final int port = 8080;
  static final String host = "172.16.1.1";
  private DatagramSocket socket;

  private List<Integer> receivingWindow;
  private ServiceHandler serviceHandler;

  Client() throws SocketException {
    fileLoader = new FileLoader();
    queue = new PacketQueue();
    socket = new DatagramSocket(port);
    serviceHandler = new ServiceHandler();
    receivingWindow = new ArrayList<>();
    }

  public void uploadFile(String src_dir, String dst_dir) throws IOException, InterruptedException {
    Path file_path = Paths.get(src_dir);

    queue.packetQueue.add(fileLoader.getInitPacket(src_dir, dst_dir, Files.size(file_path)));

    List<Packet> packetList = fileLoader.extractPackets(file_path);
    for (Packet packet : packetList) {
      queue.putPacket(packet);
    }

    service();
  }

  public void downloadFile(String FILE_DIR, String s) {
    // send GET packet

    // retrieve packets

    // correctly assemble packets in order
  }

  private void service() throws IOException, InterruptedException {
    int currentAck = 0;
    receivingWindow = new ArrayList<>();
    receivingWindow.add(currentAck);

    sendNextPacket(InetAddress.getByName(host), port, currentAck);
    
    while (true) {
      byte[] buffer = new byte[Parameters.MAX_PACKET_SIZE];
      DatagramPacket response = new DatagramPacket(buffer, buffer.length);
      socket.receive(response);

      Packet receivedPacket = new Packet(response.getData());
      System.out.println("Received packet with ACK: " + receivedPacket.getHeader().getAckNr());

      if (isValidAck(receivedPacket.getHeader().getAckNr())) {
        currentAck++;
        receivingWindow = new ArrayList<>();
        receivingWindow.add(currentAck);

        InetAddress clientAddress = response.getAddress();
        int clientPort = response.getPort();

        // get next packet
        if (queue.packetQueue.isEmpty()) {
          System.out.println("All packets are send");
          break;
        } else {
          sendNextPacket(clientAddress, clientPort, currentAck);
        }

      } else {
        System.out.println("Incorrect package received");
      }
    }
  }

  private void sendNextPacket(InetAddress address, int port, int AckNr) throws IOException {
    Packet packet = queue.removePacket();
    packet.getHeader().setAckNr(AckNr);

    DatagramPacket datagramPacket = new DatagramPacket(packet.getByteArray(), packet.getSize(), address, port);
    socket.send(datagramPacket);
    System.out.println("Packet send with ACK: " + AckNr);
  }

  public void getList(String DIR) {
  }

  public boolean isValidAck(int receivedAck) {
    return receivingWindow.contains(receivedAck);
  }
}

