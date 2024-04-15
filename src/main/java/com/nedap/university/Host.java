package com.nedap.university;

import java.io.IOException;

public interface Host {

  void uploadFile(String FILE_DIR) throws InterruptedException, IOException;

  void downloadFile(String FILE_DIR);

  void getList(String DIR);

}
