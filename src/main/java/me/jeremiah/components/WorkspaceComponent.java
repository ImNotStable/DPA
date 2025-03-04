package me.jeremiah.components;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WorkspaceComponent {

  private final File workspaceDir = new File("./dpa/workspace/");

  public WorkspaceComponent() {
    if (!workspaceDir.exists() && !workspaceDir.mkdirs())
      throw new RuntimeException("Failed to create workspace directory");
  }

  public File getRelativeFile(String path) {
    return new File(workspaceDir, path);
  }

  public boolean mergeFile(File file, String content) {
    try {
      System.out.println("Attempting to merge file: " + file.getAbsolutePath());
      Files.writeString(file.toPath(), content);
      if (!file.exists())
        System.out.println("Failed to create: " + file.getAbsolutePath());
      else
        System.out.println("File merged: " + file.getAbsolutePath());
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean deleteFile(File file) {
    return file.delete();
  }

  public String getWorkspaceState() {
    List<File> files = getFiles(workspaceDir);

    Map<String, String> fileContents = new LinkedHashMap<>();

    for (File file : files) {
      if (file.isDirectory())
        continue;
      try {
        fileContents.put(file.getName(), Files.readString(file.toPath()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    StringBuilder sb = new StringBuilder();

    for (Map.Entry<String, String> entry : fileContents.entrySet()) {
      sb.append(entry.getKey())
        .append("{\n")
        .append(entry.getValue())
        .append("\n}");
    }

    return sb.toString();
  }

  private List<File> getFiles(File dir) {
    List<File> files = new ArrayList<>();

    if (!dir.exists())
      return files;

    File[] fileList = dir.listFiles();

    if (fileList == null)
      return files;

    for (File file : fileList) {
      files.add(file);
      if (file.isDirectory())
        files.addAll(getFiles(file));
    }

    return files;
  }

}
