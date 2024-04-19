package com.nedap.university.utils;

public class Parameters {

  public static final int MAX_PACKET_SIZE = 16384;

  public static final int HEADER_SIZE = 12;

  public static final int MAX_PAYLOAD_SIZE = MAX_PACKET_SIZE - HEADER_SIZE;

  public static final int TIMEOUTDURATION = 1000;

}
