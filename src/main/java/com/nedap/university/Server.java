package com.nedap.university;

import com.nedap.university.packet.Packet;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;

public class Server extends AbstractHost {
  private final FileLoader fileLoader;
  private final PacketQueue queue;
  private DatagramSocket socket;
  static final int port = 8080;


  Server() throws SocketException {
    fileLoader = new FileLoader();
    queue = new PacketQueue();
    socket = new DatagramSocket(port);
  }


  @Override
  public void uploadFile(String FILE_DIR) throws IOException, InterruptedException {
    File file = new File(FILE_DIR);

    List<Packet> packetList = fileLoader.extractPackets(file);
    for (Packet packet : packetList) {
      queue.putPacket(packet);
    }

    service();
  }

  private void service() {
  }


  @Override
  public void downloadFile(String FILE_DIR) {

  }

  @Override
  public void getList(String DIR) {

  }
}
