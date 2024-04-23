package com.nedap.university;

import com.nedap.university.utils.Parameters;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.nedap.university.FileLoaderTest.*;
import static org.junit.jupiter.api.Assertions.*;


public class AbstractHostTest {

  AbstractHost abstractHost;

  @BeforeEach
  public void setUp() throws SocketException {
    abstractHost = new AbstractHost(1234) {
      @Override
      void handlePacket(DatagramPacket datagramPacket) throws IOException {

      }
    };
  }


  @Test
  void testSetTimer() {
    abstractHost.setTimer(new DatagramPacket(new byte[1], 1), 123);

  }
}