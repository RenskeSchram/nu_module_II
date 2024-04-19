package com.nedap.university.utils;

import com.nedap.university.packet.Packet;

public class Checksum {
  public static int calculateChecksum(Packet packet) {
    // Extracting 16-bit elements from the array for checksum calculations
    byte[] packetBytes = packet.getByteArray();
    int[] checksumElements = new int[(packetBytes.length / 2) + 1];

    for (int i = 0; i < packetBytes.length - 1; i += 2) {
      int highByte;
      int lowByte;
      if (i == 6) {
        highByte = 0x00;
        lowByte = packetBytes[i] & 0xFF; // Ensure byte is treated as unsigned
      } else {
        highByte = (packetBytes[i] << 8) & 0xFF00; // Shift byte to higher bits
        lowByte = packetBytes[i + 1] & 0xFF; // Ensure byte is treated as unsigned
      }
      checksumElements[i / 2] = (highByte | lowByte);
    }

    // Set Checksum element to 0x0000
    checksumElements[4] = 0x0000;

    // Summation
    int sum = 0;
    for (int checksumElement : checksumElements) {
      sum += checksumElement & 0xFFFF;
    }

    while ((sum & 0xFFFF0000) != 0) {
      sum = (sum & 0xFFFF) + (sum >>> 16);
    }

    return ~sum & 0xFFFF;
  }

  public static boolean verifyChecksum(Packet packet) {
    return Checksum.calculateChecksum(packet) == packet.getHeader().getChecksum();
  }
}