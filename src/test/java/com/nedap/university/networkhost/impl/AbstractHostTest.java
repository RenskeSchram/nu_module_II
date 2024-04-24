package com.nedap.university.networkhost.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nedap.university.utils.LoggingHandler;
import com.nedap.university.utils.PacketBuilder;
import com.nedap.university.utils.Parameters;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class AbstractHostTest {

  AbstractHost abstractHost;

  private static final String TEST_FILE_PATH = "test_log.txt";

  @BeforeEach
  public void setUp() throws SocketException {
    File testLogFile = new File(TEST_FILE_PATH);
    if (testLogFile.exists()) {
      testLogFile.delete();
    }
    LoggingHandler.redirectSystemErrToFile(TEST_FILE_PATH);

    abstractHost = new AbstractHost(1234) {
      @Override
      protected void handlePacket(DatagramPacket datagramPacket) {
      }
    };
  }

  @Test
  void testSetTimer() throws InterruptedException, UnknownHostException {
    List<String> lines;

    abstractHost.setTimer(
        new DatagramPacket(new byte[1], 1, InetAddress.getByName("localhost"), 8080), 123);
    Thread.sleep(Parameters.MAX_RETRIES * Parameters.TIMEOUT_DURATION);

    try (BufferedReader reader = new BufferedReader(new FileReader(TEST_FILE_PATH))) {
      lines = reader.lines().toList();
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    assertTrue(lines.get(0).contains("TIMER RUN OUT"));
    assertTrue(lines.get(lines.size() - 1).contains("RETRIES EXCEEDED"));

    abstractHost.setTimer(
        new DatagramPacket(PacketBuilder.getAckPacket(1).getByteArray(),
            PacketBuilder.getAckPacket(1).getSize(), InetAddress.getByName("localhost"), 8080),
        123);
    Thread.sleep(Parameters.TIMEOUT_DURATION + 10);

    abstractHost.cancelTimer(123);

    Thread.sleep(Parameters.TIMEOUT_DURATION * 2 + 10);

    try (BufferedReader reader = new BufferedReader(new FileReader(TEST_FILE_PATH))) {
      lines = reader.lines().toList();
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    assertTrue(lines.get(0).contains("TIMER RUN OUT"));
    assertEquals(lines.get(lines.size() - 1), lines.get(0));
  }
}