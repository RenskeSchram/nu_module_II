package com.nedap.university;

import com.nedap.university.packet.Header.FLAG;
import com.nedap.university.packet.Packet;
import com.nedap.university.utils.Parameters;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class NewClient {
  private static final int port = 8080;
  private static final String host = "172.16.1.1";
  private boolean inService;

  private DatagramSocket socket;
  private ServiceHandler serviceHandler;
  private int finalServiceAck;

  NewClient() throws SocketException {
    socket = new DatagramSocket(port);
    serviceHandler = new ServiceHandler();
    inService = false;
    finalServiceAck = -1;
  }

  public void uploadFile(String src_dir, String dst_dir) throws IOException, InterruptedException {
    System.out.println("UPLOAD STARTED");
    Packet startUploadPacket = serviceHandler.startUpload(src_dir, dst_dir);
    setFinalServiceAck(startUploadPacket);
    sendPacket(startUploadPacket, InetAddress.getByName(host), port);
    service();
  }

  public void downloadFile(String src_dir, String dst_dir) throws IOException, InterruptedException {
    System.out.println("DOWNLOAD STARTED");
    Packet startDownloadPacket = serviceHandler.startDownload(src_dir, dst_dir);
    sendPacket(startDownloadPacket, InetAddress.getByName(host), port);
    service();
  }

  private void service() throws IOException {
    inService = true;

    while (inService) {
      DatagramPacket request = new DatagramPacket(new byte[Parameters.MAX_PACKET_SIZE], Parameters.MAX_PACKET_SIZE);
      socket.receive(request);
      Packet receivedPacket = new Packet(request.getData());

      if (isValidPacket(receivedPacket)) {
        //TODO: send ACK

        //Check if final packet
        if (receivedPacket.getHeader().getAckNr() == finalServiceAck) {
          finalServiceAck = -1;
          System.out.println("UPLOAD FINISHED");
          break;
        }

        if (receivedPacket.getHeader().isFlagSet(FLAG.FIN)) {
          serviceHandler.handlePacket(receivedPacket);
          finalServiceAck = -1;
          System.out.println("DOWNLOAD FINISHED");
          break;
        }

        //Get and send response packet(s)
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
    }

  public void getList(String src_dir) {

  }

  public void setFinalServiceAck(Packet servicePacket) {
    String fileSize = servicePacket.getPayload().getStringArray()[1];
    int numOfPackets = (int) Math.ceil((double) servicePacket.getPayload().getFileSize() / Parameters.MAX_PAYLOAD_SIZE);
    this.finalServiceAck = servicePacket.getHeader().getAckNr() + numOfPackets;
  }

  public int getFinalServiceAck() {
    return finalServiceAck;
  }
}


