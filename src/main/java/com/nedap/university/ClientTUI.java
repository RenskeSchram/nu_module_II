package com.nedap.university;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class ClientTUI {
  boolean runTui = true;
  Client client;

  ClientTUI() throws SocketException {
    client = new Client();
  }

  public void runTUI() throws IOException, InterruptedException {
    Scanner scanner = new Scanner(System.in);

    // retrieve host name
    InetAddress hostname = null;
    boolean validHostname = false;
    while (!validHostname) {
      System.out.print("hostname:     \n");
      try {
        //hostname = InetAddress.getByName(scanner.nextLine());
        hostname = InetAddress.getByName("172.16.1.1");
        validHostname = true;
      } catch (UnknownHostException e) {
        System.err.println("Invalid hostname. Please enter a valid hostname.");
      }
    }

    // retrieve port number
    int port = -1;
    boolean validPort = false;
    while (!validPort) {
      System.out.print("port:       \n");
      try {
        //port = scanner.nextInt();
        port = 8080;
        validPort = true;
      } catch (InputMismatchException e) {
        System.err.println("Invalid port. Please enter a valid port.");
        scanner.nextLine();
      }
    }

    try {
      DatagramSocket socket = new DatagramSocket();
      DatagramPacket request = new DatagramPacket(new byte[1], 1, hostname, port);
      socket.send(request);

      byte[] buffer = new byte[512];
      DatagramPacket response = new DatagramPacket(buffer, buffer.length);
      socket.receive(response);

      String quote = new String(buffer, 0, response.getLength());

      System.out.println(quote);
      System.out.println();

    } catch (SocketTimeoutException ex) {
      System.out.println("Timeout error: " + ex.getMessage());
      ex.printStackTrace();
    } catch (IOException ex) {
      System.out.println("Client error: " + ex.getMessage());
      ex.printStackTrace();
    }

    while (runTui) {
      String systemTuiInput = scanner.nextLine();

      // TUI options beside regular protocol
      if (systemTuiInput.equalsIgnoreCase("help")) {
        System.out.println(this);
      } else if (systemTuiInput.equalsIgnoreCase("disconnect")) {
        runTui = false;
      } else {
        handleClientInput(systemTuiInput);
      }
    }
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    ClientTUI clientTUI = new ClientTUI();
    clientTUI.runTUI();
  }

  private void handleClientInput(String systemTuiInput) throws InterruptedException, IOException {
      String[] protocol = systemTuiInput.split(" ");
      System.out.println(systemTuiInput);

      switch (protocol[0]) {
        case "send":
          if (protocol.length == 2) {
            System.out.println("sending file");
            protocol[1] = "example_files/tiny.pdf";
            client.uploadFile(protocol[1]);
          } else {
            System.out.println("Invalid input length, try again.");
          }
      }
  }

  @Override
  public String toString() {
    return
        "Client TUI commands:\n" +
            "   DISCONNECT ..................disconnect and stop \n" +
            "   HELP ....................... help (this menu) \n";
  }
}
