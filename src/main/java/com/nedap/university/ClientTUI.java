package com.nedap.university;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class ClientTUI {
  boolean runTui = true;
  Client client;
  String CLIENT_HOME = "example_files/";
  String SERVER_HOME =  "home/pi/PiServer/";

  ClientTUI() {}

  public void runTUI() throws IOException, InterruptedException {
    Scanner scanner = new Scanner(System.in);

    // retrieve host name
    InetAddress hostname = null;
    boolean validHostname = false;
    while (!validHostname) {
      System.out.print("PI Server IP address:     \n");
      try {
        hostname = InetAddress.getByName(scanner.nextLine());
        validHostname = true;
      } catch (UnknownHostException e) {
        System.err.println("Invalid hostname. Please enter a valid hostname.");
      }
    }

    // retrieve port number
    int port = -1;
    boolean validPort = false;
    while (!validPort) {
      System.out.print("PI Server port:       \n");
      try {
        port = scanner.nextInt();
        //port = 8080;
        validPort = true;
      } catch (InputMismatchException e) {
        System.err.println("Invalid port. Please enter a valid port.");
        scanner.nextLine();
      }
    }

    client = new Client(hostname, port);


//
//    try {
//      DatagramSocket socket = new DatagramSocket();
//      DatagramPacket request = new DatagramPacket(new byte[1], 1, hostname, port);
//      socket.send(request);
//      System.out.println("request is send");
//
//      byte[] buffer = new byte[512];
//      DatagramPacket response = new DatagramPacket(buffer, buffer.length);
//      socket.receive(response);
//
//      System.out.println("PI Server is ACTIVE");
//
//    } catch (SocketTimeoutException ex) {
//      System.out.println("Timeout error: " + ex.getMessage());
//      ex.printStackTrace();
//    } catch (IOException ex) {
//      System.out.println("Client error: " + ex.getMessage());
//      ex.printStackTrace();
//    }
    System.out.println(this);

    while (runTui) {
      String systemTuiInput = scanner.nextLine();

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

      switch (protocol[0].toLowerCase()) {
        case "send":
          if (protocol.length == 3) {
            client.uploadFile(CLIENT_HOME + protocol[1], SERVER_HOME + protocol[2]);
          } else {
            System.out.println("Invalid input length, try again.");
          }
          break;
        case "get":
          if (protocol.length == 3) {
            client.downloadFile(SERVER_HOME + protocol[1], CLIENT_HOME + protocol[2]);
          } else {
            System.out.println("Invalid input length, try again.");
          }
          break;
        case "list":
          if (protocol.length == 1) {
            client.getList(SERVER_HOME);
          } else if (protocol.length == 2) {
            client.getList(SERVER_HOME + protocol[1]);
          } else {
            System.out.println("Invalid input length, try again.");
          }
          break;

      }
  }

  @Override
  public String toString() {
    return
        "Client TUI commands:\n" +
            "   SEND <src_dir> <dst_dir>.... send file \n" +
            "   GET  <src_dir> <dst_dir>.... get file \n" +
            "   LIST <src_dir> ............. get filenames stored directory \n" +
            "   DISCONNECT ................. disconnect and stop \n" +
            "   HELP ....................... help (this menu) \n";
  }
}
