package com.nedap.university;

import com.nedap.university.packet.Header.FLAG;
import com.nedap.university.packet.Packet;
import com.nedap.university.utils.Parameters;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;

public class Client extends AbstractHost{
  private static InetAddress dstInetAdress;
  private static int dstPort;
  private boolean inService;

  private int finalServiceAck;

  Client(InetAddress dstAddress, int port) throws SocketException {
    super(port);
    this.dstPort = port;
    this.dstInetAdress = dstAddress;
    finalServiceAck = -1;
  }

  public void service() throws IOException {
    inService = true;

    while (inService) {
      DatagramPacket request = new DatagramPacket(new byte[Parameters.MAX_PACKET_SIZE], Parameters.MAX_PACKET_SIZE);
      socket.receive(request);
      Packet receivedPacket = new Packet(request.getData());

      if (isValidPacket(receivedPacket)) {
        // cancel timer
        cancelTimer(receivedPacket.getHeader().getAckNr());

        //TODO: send ACK

        //Check packet order -> save packets for later

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
          }
        }
      }
    }
  }


  public void uploadFile(String src_dir, String dst_dir) throws IOException, InterruptedException {
    System.out.println("UPLOAD STARTED");
    Packet startUploadPacket = serviceHandler.startUpload(src_dir, dst_dir);
    setFinalServiceAck(startUploadPacket);
    sendPacket(startUploadPacket, dstInetAdress, dstPort);
    service();
  }

  public void downloadFile(String src_dir, String dst_dir) throws IOException, InterruptedException {
    System.out.println("DOWNLOAD STARTED");
    Packet startDownloadPacket = serviceHandler.startDownload(src_dir, dst_dir);
    sendPacket(startDownloadPacket, dstInetAdress, dstPort);
    service();
  }

  public void getList(String src_dir) throws IOException {
    System.out.println("ASKED FOR FILE LIST " + src_dir);
    Packet getListPacket = serviceHandler.getHelloListPacket(src_dir);
    sendPacket(getListPacket, dstInetAdress, dstPort);
    service();
  }

  public void setFinalServiceAck(Packet servicePacket) {
    int numOfPackets = (int) Math.ceil((double) servicePacket.getPayload().getFileSize() / Parameters.MAX_PAYLOAD_SIZE);
    this.finalServiceAck = servicePacket.getHeader().getAckNr() + numOfPackets;
  }


}


