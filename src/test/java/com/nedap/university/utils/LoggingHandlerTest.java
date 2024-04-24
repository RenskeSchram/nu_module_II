package com.nedap.university.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoggingHandlerTest {

  private static final String TEST_FILE_PATH = "test_log.txt";

  @BeforeEach
  public void setUp() {
    File testLogFile = new File(TEST_FILE_PATH);
    if (testLogFile.exists()) {
      testLogFile.delete();
    }
  }

  @Test
  public void testRedirectSystemErrToFile() {
    boolean error = false;
    LoggingHandler.redirectSystemErrToFile(TEST_FILE_PATH);

    System.err.println("error message");
    System.err.println("error message II");
    System.out.println("system out message");

    // Check if the error message is written to the log file
    try (BufferedReader reader = new BufferedReader(new FileReader(TEST_FILE_PATH))) {
      String line = reader.readLine();
      assertNotNull(line);
      assertTrue(line.contains("error message"));
      assertFalse(line.contains("system out message"));
    } catch (IOException e) {
      error = true;
    }
    assertFalse(error);
  }

  @Test
  public void testResetFile() {
    boolean error = false;
    LoggingHandler.redirectSystemErrToFile(TEST_FILE_PATH);
    System.err.println("old error message");

    LoggingHandler.resetFile(TEST_FILE_PATH);

    File testLogFile = new File(TEST_FILE_PATH);
    LoggingHandler.redirectSystemErrToFile(TEST_FILE_PATH);
    assertTrue(testLogFile.exists());
    System.err.println("new error message");
    System.err.println("new error message");
    System.err.println("new error message");

    try (BufferedReader reader = new BufferedReader(new FileReader(TEST_FILE_PATH))) {
      String line = reader.readLine();
      assertNotNull(line);
      System.out.println(line);
      assertTrue(line.contains("new error message"));
      assertFalse(line.contains("old error message"));
    } catch (IOException e) {
      error = true;
    }
    assertFalse(error);
  }
}
