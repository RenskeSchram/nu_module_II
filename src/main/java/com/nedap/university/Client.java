package com.nedap.university;

import com.nedap.university.packet.Header.FLAG;
import com.nedap.university.packet.Packet;
import com.nedap.university.utils.Parameters;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;

public class Client extends AbstractHost{
  private InetAddress dstInetAdress;
  private int dstPort;

  private int finalReceivingAck;

  private long startTime;
  private long endTime;

  Client(InetAddress dstAddress, int port) throws SocketException {
    super(port);
    this.dstPort = port;
    this.dstInetAdress = dstAddress;
    finalReceivingAck = -1;
  }

  protected void handlePacket(DatagramPacket datagramPacket) throws IOException {
    Packet receivedPacket = new Packet(datagramPacket.getData());
    int receivedAck = receivedPacket.getHeader().getAckNr();

    //Check if final packet
    if (receivedAck == finalReceivingAck) {
      finalReceivingAck = -1;
      updateLastFrameReceived(receivedAck);
      //System.out.println("RECEIVING    LFR: " + lastFrameReceived + " and LAF: " + largestAcceptableFrame);
      endTime = System.currentTimeMillis();
      System.out.println("UPLOAD FINISHED in " + (endTime - startTime) + " milliseconds");
      inService = false;
      return;
    }

    if (receivedPacket.getHeader().isFlagSet(FLAG.FIN)) {
      serviceHandler.handlePacket(receivedPacket);
      finalReceivingAck = -1;
      updateLastFrameReceived(receivedAck);
      endTime = System.currentTimeMillis();
      System.out.println("DOWNLOAD FINISHED in " + (endTime - startTime) + " milliseconds");
      inService = false;
      return;
    }

    //Get and send response packet(s)
    List<Packet> responsePackets = serviceHandler.handlePacket(receivedPacket);

    if (!responsePackets.isEmpty()) {
      for (Packet packet : responsePackets) {
        sendPacket(packet, datagramPacket.getAddress(), datagramPacket.getPort());
      }
    }

    outOfOrderPackets.remove(receivedAck);
    updateLastFrameReceived(receivedAck);
  }

  public void uploadFile(String src_dir, String dst_dir) throws IOException {
    System.out.println("UPLOAD STARTED");
    startTime = System.currentTimeMillis();

    Packet startUploadPacket = serviceHandler.startUpload(src_dir, dst_dir);
    setFinalReceivingAck(startUploadPacket);
    sendPacket(startUploadPacket, dstInetAdress, dstPort);
    service();
  }

  public void downloadFile(String src_dir, String dst_dir) throws IOException {
    System.out.println("DOWNLOAD STARTED");
    startTime = System.currentTimeMillis();

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

  public void setFinalReceivingAck(Packet servicePacket) {
    int numOfPackets = (int) Math.ceil((double) servicePacket.getPayload().getFileSize() / Parameters.MAX_PAYLOAD_SIZE);
    this.finalReceivingAck = servicePacket.getHeader().getAckNr() + numOfPackets;
  }
}