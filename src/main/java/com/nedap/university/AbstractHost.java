package com.nedap.university;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

public abstract class AbstractHost implements Host{
  private final FileLoader fileLoader;
  private final PacketQueue queue;
  private DatagramSocket socket;
  static final int port = 8080;

  AbstractHost() throws SocketException {
    fileLoader = new FileLoader();
    queue = new PacketQueue();
    socket = new DatagramSocket(port);
  }

  @Override
  public void uploadFile(String FILE_DIR) throws InterruptedException, IOException {
  }

  @Override
  public void downloadFile(String FILE_DIR) {
  }

  @Override
  public void getList(String DIR) {
  }
}
