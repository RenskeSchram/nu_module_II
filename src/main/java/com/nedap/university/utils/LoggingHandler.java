package com.nedap.university.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class LoggingHandler {

  public static void log(String message) throws IOException {
    FileWriter writer = new FileWriter("log.txt", false);
    writer.write(message + System.lineSeparator());
    writer.close();
  }
}
