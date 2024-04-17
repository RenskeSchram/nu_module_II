package com.nedap.university;

import com.nedap.university.UDPTest.QuoteServer;
import com.nedap.university.packet.Header;
import com.nedap.university.packet.Header.FLAG;
import com.nedap.university.packet.Packet;
import com.nedap.university.packet.Payload;
import com.nedap.university.utils.Parameters;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Server extends AbstractHost {
  private final DatagramSocket socket;

  private final FileLoader fileLoader;
  private final FileBuffer fileBuffer;
  private final PacketQueue queue;

  private int expectedAck;


  Server(int port) throws SocketException {
    socket = new DatagramSocket(port);
    fileLoader = new FileLoader();
    fileBuffer = new FileBuffer();
    queue = new PacketQueue();
  }

  public void uploadFile(String src_dir, String dst_dir) throws IOException, InterruptedException {
    Path file_path = Paths.get(src_dir);

    queue.packetQueue.add(getHelloDataPacket(dst_dir, Files.size(file_path)));

    List<Packet> packetList = fileLoader.extractPackets(file_path);
    for (Packet packet : packetList) {
      queue.putPacket(packet);
    }
  }

  @Override
  public void downloadFile(String FILE_DIR) {
    // Create initial package

    // Service()

  }

  @Override
  public void getList(String DIR) {
    // Create initial package

    // Service

  }

  private void service() throws IOException, InterruptedException {
    while (true) {
      byte[] buffer = new byte[Parameters.MAX_PACKET_SIZE];
      DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
      socket.receive(receivedPacket);
      Packet packet = new Packet(receivedPacket.getData());

      if (expectedAck == packet.getHeader().getAckNr()) {
        handlePacket(receivedPacket);
      }
    }
  }

  private void sendPacket(InetAddress address, int port, int AckNr, Packet packet)
      throws IOException {
    // set ACKnr
    packet.getHeader().setAckNr(AckNr);

    // send packet
    DatagramPacket datagramPacket = new DatagramPacket(packet.getByteArray(), packet.getSize(), address, port);
    socket.send(datagramPacket);
    System.out.println("Packet send with ACK: " + AckNr);
  }

  public void handlePacket(DatagramPacket datagramPacket) throws IOException, InterruptedException {
    Packet packet = new Packet(datagramPacket.getData());
    Header header = packet.getHeader();
    Payload payload = packet.getPayload();

    byte flags = header.getFlagByte();

    switch (flags) {
      // HELLO + DATA
      case (byte) 0b00000011:
        // send ACK
        sendAck();
        // initialize new file buffer to store data
        fileBuffer.initFileBuffer(payload);

        // HELLO + GET
      case (byte) 0b00000101:
        // send ACK
        sendAck();
        // Parse File
        //uploadFile(PacketParser.getStringPayload(packetBytes)[0]);
        // Send HELLO + DATA message
        sendHelloPacket();

        // HELLO + LIST
      case (byte) 0b00001001:
        // send ACK + DATA + LIST + FIN

        // Send LIST

        // DATA
      case (byte) 0b00000010:
        sendAck();
        fileBuffer.receivePacket(payload);

        // DATA + FIN
      case (byte) 0b00100010:
        sendAck();

        fileBuffer.receivePacket(payload);
        fileBuffer.receiveFin(payload);

        // ACK
      case (byte) 0b00010000:
        expectedAck++;

        if (!queue.packetQueue.isEmpty()) {
          sendNextPacket();
        }

      default:
        // do nothing
    }
  }

  private void sendHelloPacket() {

  }

  private void sendAck(){}

  private void sendNextPacket() {

  }

  public static void main(String[] args) throws SocketException {
    int port = Integer.parseInt(args[1]);
    try {
      Server server = new Server(port);
      server.service();
    } catch (SocketException ex) {
      System.out.println("Socket error: " + ex.getMessage());
    } catch (IOException ex) {
      System.out.println("I/O error: " + ex.getMessage());
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private Packet getHelloDataPacket(String dstDir, long size) {
    byte[] dstDirBytes = dstDir.getBytes();
    ByteBuffer sizeBytes = ByteBuffer.allocate(Long.BYTES);
    sizeBytes.putLong(size);

    byte[] byteArray = new byte[dstDirBytes.length + sizeBytes.array().length];
    ByteBuffer buffer = ByteBuffer.wrap(byteArray);
    buffer.put(dstDirBytes);
    buffer.put("~".getBytes());
    buffer.put(sizeBytes.array());

    Payload payload = new Payload(buffer.array(), 0 , false);
    Header header = new Header(payload);
    header.setFlag(FLAG.HELLO);

    return new Packet(header, payload);
  }


}