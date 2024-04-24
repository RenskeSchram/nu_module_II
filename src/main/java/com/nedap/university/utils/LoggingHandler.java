package com.nedap.university.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;

public class LoggingHandler {

  public static synchronized void redirectSystemErrToFile(String filePath) {
    File errorLogFile = new File(filePath);

    try {
      FileOutputStream fos = new FileOutputStream(errorLogFile, true);
      PrintStream ps = new PrintStream(fos);
      System.setErr(ps);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public static synchronized void resetFile(String filePath) {
    File errorLogFile = new File(filePath);
  }
}
