package com.nedap.university.utils;

/**
 * File with set parameters.
 */
public class Parameters {

  //Packet parameters
  public static final int MAX_PACKET_SIZE = 16384;
  public static final int HEADER_SIZE = 12;
  public static final int MAX_PAYLOAD_SIZE = MAX_PACKET_SIZE - HEADER_SIZE;

  // TimeOut
  public static final int TIMEOUT_DURATION = 400;
  public static final int MAX_RETRIES = 10;

  // Test file Paths
  public static final String TEST_SRC_FILE_PATH = "example_files/medium.pdf";
  public static final String TEST_DST_FILE_PATH = "example_files/test_file.pdf";

}
