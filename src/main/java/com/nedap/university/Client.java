package com.nedap.university;

import com.nedap.university.packet.Header.FLAG;
import com.nedap.university.packet.Packet;
import com.nedap.university.utils.Parameters;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.sql.SQLOutput;
import java.util.List;

/**
 * Client side implementation of AbstractHost: can initiate the upload and downloading of Files.
 */
public class Client extends AbstractHost{
  private InetAddress dstInetAdress;
  private int dstPort;

  private int finalReceivingAck;
  private long startTime;

  Client(InetAddress dstAddress, int port) throws SocketException {
    super(port);
    this.dstPort = port;
    this.dstInetAdress = dstAddress;
    finalReceivingAck = -1;
  }

  @Override
  protected void handlePacket(DatagramPacket datagramPacket) throws IOException {
    Packet receivedPacket = new Packet(datagramPacket.getData());
    int receivedAck = receivedPacket.getHeader().getAckNr();

    long endTime = System.currentTimeMillis();
    if (receivedAck == finalReceivingAck) {
      finalReceivingAck = -1;
      updateLastFrameReceived(receivedAck);
      System.out.println("UPLOAD FINISHED in "  + (endTime - startTime)/1000 + " seconds \n");
      if (unacknowledgedPackets.size() > 1) {
        System.out.println(unacknowledgedPackets.size() + " unacked packets with nrs: ");
        for (int ack : unacknowledgedPackets.keySet()) {
          System.out.print(ack);
        }
        System.out.println();
      }
      inService = false;
      return;
    }

    if (receivedPacket.getHeader().isFlagSet(FLAG.FIN)) {
      serviceHandler.handlePacket(receivedPacket);
      finalReceivingAck = -1;
      updateLastFrameReceived(receivedAck);
      System.out.println("DOWNLOAD FINISHED in " + (endTime - startTime)/1000 + " seconds \n");
      if (unacknowledgedPackets.size() > 1) {
        System.out.println(unacknowledgedPackets.size() + " unacked packets with nrs: ");
        for (int ack : unacknowledgedPackets.keySet()) {
          System.out.print(ack);
        }
        System.out.println();
      }

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

  /**
   * Uploading file by initiating with an HELLO DATA packet.
   * @param src_dir source path of File to be sent
   * @param dst_dir destination path of File to be sent
   * @throws IOException if an I/O error occurs.
   */
  public void uploadFile(String src_dir, String dst_dir) throws IOException {
    System.out.println("UPLOAD STARTED");
    startTime = System.currentTimeMillis();

    Packet startUploadPacket = serviceHandler.startUpload(src_dir, dst_dir);
    setFinalReceivingAck(startUploadPacket);
    sendPacket(startUploadPacket, dstInetAdress, dstPort);
    service();
  }

  /**
   * Downloading file by initiating with an HELLO GET packet.
   * @param src_dir source path of File to be sent
   * @param dst_dir destination path of File to be sent
   * @throws IOException if an I/O error occurs.
   */
  public void downloadFile(String src_dir, String dst_dir) throws IOException {
    System.out.println("DOWNLOAD STARTED");
    startTime = System.currentTimeMillis();

    Packet startDownloadPacket = serviceHandler.startDownload(src_dir, dst_dir);
    sendPacket(startDownloadPacket, dstInetAdress, dstPort);
    service();
  }

  /**
   * Send Packet which asks for directory list on Server.
   * @param src_dir directory on Server.
   * @throws IOException if an I/O error occurs.
   */
  public void getList(String src_dir) throws IOException {
    System.out.println("FILE & DIRECTORY LIST of " + src_dir);
    startTime = System.currentTimeMillis();

    Packet getListPacket = serviceHandler.getHelloListPacket(src_dir);
    sendPacket(getListPacket, dstInetAdress, dstPort);
    service();
  }

  /**
   * Update to stay indicated when current service is done and new TUI input can be retrieved.
   * @param servicePacket packet to obtain ack from
   */
  public void setFinalReceivingAck(Packet servicePacket) {
    int numOfPackets = (int) Math.ceil((double) servicePacket.getPayload().getFileSize() / Parameters.MAX_PAYLOAD_SIZE);
    this.finalReceivingAck = servicePacket.getHeader().getAckNr() + numOfPackets;
  }
}