package com.nedap.university;

import com.nedap.university.packet.Packet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PacketQueue {
  BlockingQueue<Packet> packetQueue;

  PacketQueue() {
    packetQueue = new LinkedBlockingQueue<>();
  }

  public synchronized void putPacket(Packet packet) throws InterruptedException {
    packetQueue.put(packet);
  }
  public synchronized Packet removePacket() {
    return packetQueue.remove();
  }

}
