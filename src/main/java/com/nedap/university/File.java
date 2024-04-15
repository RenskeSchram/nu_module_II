package com.nedap.university;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class File {
  private final String FILE_DIR;
  private byte[] fileData;

  File(String FILE_DIR) {
    this.FILE_DIR = FILE_DIR;
    getByteArrayFromFile();
  }

  private void getByteArrayFromFile() {
    java.io.File file = new java.io.File(FILE_DIR);
    fileData = new byte[(int) file.length()];
    try (FileInputStream inputStream = new FileInputStream(file)) {
      inputStream.read(fileData);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public byte[] getFileData() {
    return fileData;
  }

  public static void main(String[] args) {
    File fileData = new File("example_files/tiny.pdf");
    System.out.println(fileData.fileData.length);
  }

}
