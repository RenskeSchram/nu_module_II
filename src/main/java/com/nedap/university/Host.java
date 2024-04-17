package com.nedap.university;

import com.nedap.university.packet.Packet;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public interface Host {

  void uploadFile(String FILE_DIR) throws InterruptedException, IOException;

  void downloadFile(String FILE_DIR);

  void getList(String DIR);

  default void sendPacket(DatagramPacket datagramPacket) {

  }

}
