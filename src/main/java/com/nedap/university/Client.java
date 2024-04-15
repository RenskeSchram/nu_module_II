package com.nedap.university;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

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
    // get packets
    fileLoader.loadFile(FILE_DIR, queue);
    // add packets to queue

    // start sending from queue
    service();
  }

  public void downloadFile(String FILE_DIR) {
    // send GET packet

    // retrieve packets

    // correctly assemble packets in order
  }

  private void service() throws IOException, InterruptedException {
    // send initial packet
    DatagramPacket initialPacket = new DatagramPacket(new byte[queue.packetQueue.size()], queue.packetQueue.size(),  InetAddress.getByName(host), port);
    socket.send(initialPacket);
    System.out.println("initial file send");

    while (!queue.packetQueue.isEmpty()) {
      System.out.println("waiting to receive message");
      byte[] buffer = new byte[512];
      DatagramPacket response = new DatagramPacket(buffer, buffer.length);

      socket.receive(response);
      String quote = new String(buffer, 0, response.getLength());
      System.out.println("received message: " + quote);
      Thread.sleep(2000);

      // TODO: if ack is received || timer run out
      byte[] data = queue.removePacket().getByteArray();
      InetAddress clientAddress = response.getAddress();
      int clientPort = response.getPort();
      DatagramPacket packet = new DatagramPacket(data, data.length, clientAddress, clientPort);
      socket.send(packet);
      System.out.println("send packet with data length: " + data.length);
    }
  }


  public FileLoader getFileLoader() {
    return fileLoader;
  }

}

