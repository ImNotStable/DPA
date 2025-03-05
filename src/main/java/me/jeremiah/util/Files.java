package me.jeremiah.util;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Files {

  @SneakyThrows
  public static String readFileContents(File file) {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    StringBuilder builder = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null)
      builder.append(line).append("\n");
    reader.close();
    return builder.toString();
  }

}
