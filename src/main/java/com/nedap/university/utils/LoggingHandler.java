package com.nedap.university.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

public class LoggingHandler {

  /**
   * Redirect System error messages to keep Host application console clean.
   * @param filePath file directory to store log.
   */

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
    try (FileWriter fw = new FileWriter(filePath, false)) {
      fw.write("");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
