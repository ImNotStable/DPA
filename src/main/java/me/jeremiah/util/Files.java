package me.jeremiah.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Files {

  public static boolean createDirs(File dir) {
    return dir.exists() || dir.mkdirs();
  }

  public static boolean createFile(File file) {
    if (file.exists())
      return true;
    File dir = file.getParentFile();
    if (!dir.exists() && !dir.mkdirs())
      return false;
    try {
      return file.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  public static boolean setContent(File file, String content) {
    try (FileWriter writer = new FileWriter(file)) {
      writer.write(content);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public static String getContents(File file) {
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      StringBuilder builder = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null)
        builder.append(line).append("\n");
      return builder.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }

  public static List<File> getFiles(File dir) {
    List<File> files = new ArrayList<>();
    collectFiles(dir, files);
    return files;
  }

  private static void collectFiles(File dir, List<File> files) {
    if (!dir.exists()) {
      return;
    }

    File[] fileList = dir.listFiles();
    if (fileList == null) {
      return;
    }

    for (File file : fileList) {
      if (file.isDirectory()) {
        collectFiles(file, files);
      } else {
        files.add(file);
      }
    }
  }

}
