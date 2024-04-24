package com.nedap.university.networkhost.impl;

import com.nedap.university.networkhost.impl.AbstractHost;
import com.nedap.university.utils.Parameters;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class AbstractHostTest {

  AbstractHost abstractHost;
  private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

  @BeforeEach
  public void setUp() throws SocketException {
    //System.setOut(new PrintStream(outputStreamCaptor));
    abstractHost = new AbstractHost(1234) {
      @Override
      protected void handlePacket(DatagramPacket datagramPacket) throws IOException {}
    };
  }
  @AfterEach
  public void reset() {
    System.setOut(System.out);
  }


  @Test
  void testSetTimer() throws InterruptedException, UnknownHostException {
    abstractHost.setTimer(new DatagramPacket(new byte[1], 1, InetAddress.getByName("localhost"), 8080), 123);
    Thread.sleep(Parameters.MAX_RETRIES * Parameters.TIMEOUT_DURATION + 10);
    assertTrue(outputStreamCaptor.toString().trim().contains("timer run out"));
  }
}