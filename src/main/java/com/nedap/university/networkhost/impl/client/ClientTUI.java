package com.nedap.university.networkhost.impl.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Client TUI application to run the Client side of the connection.
 */
public class ClientTUI {

  private Client client;
  private String CLIENT_HOME = "example_files/";
  private String SERVER_HOME = "home/pi/PiServer/";
  boolean runTui = true;

  public void runTUI() throws IOException, InterruptedException {
    Scanner scanner = new Scanner(System.in);

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

    int port = -1;
    boolean validPort = false;
    while (!validPort) {
      System.out.print("PI Server port:       \n");
      try {
        port = scanner.nextInt();
        if (port > 999 && port < 65535) {
          validPort = true;
        }
      } catch (InputMismatchException e) {
        System.err.println("Invalid port. Please enter a valid port.");
        scanner.nextLine();
      }
    }

    client = new Client(hostname, port);
    System.out.println("\n" + this);

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

  private void handleClientInput(String systemTuiInput) throws IOException {
    String[] protocol = systemTuiInput.split(" ");

    switch (protocol[0].toLowerCase()) {
      case "send":
        if (protocol.length == 3) {
          client.uploadFile(CLIENT_HOME + protocol[1], SERVER_HOME + protocol[2]);
        } else {
          System.err.println("Invalid input length, try again.");
        }
        break;
      case "get":
        if (protocol.length == 3) {
          client.downloadFile(SERVER_HOME + protocol[1], CLIENT_HOME + protocol[2]);
        } else {
          System.err.println("Invalid input length, try again.");
        }
        break;
      case "list":
        if (protocol.length == 1) {
          client.getList(SERVER_HOME);
        } else if (protocol.length == 2) {
          client.getList(SERVER_HOME + protocol[1]);
        } else {
          System.err.println("Invalid input length, try again.");
        }
        break;
    }
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    ClientTUI clientTUI = new ClientTUI();
    clientTUI.runTUI();
  }

  @Override
  public String toString() {
    return
        "Client TUI commands:\n" +
            "   send <src_dir> <dst_dir>.... send file \n" +
            "   get  <src_dir> <dst_dir>.... get file \n" +
            "   list <src_dir> ............. get filenames stored directory \n" +
            "   disconnect ................. disconnect and stop \n" +
            "   help ....................... help (this menu) \n";
  }
}
