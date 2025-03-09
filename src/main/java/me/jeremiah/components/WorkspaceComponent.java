package me.jeremiah.components;

import lombok.SneakyThrows;
import me.jeremiah.util.Files;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
      if (!file.exists()) {
        file.getParentFile().mkdirs();
        file.createNewFile();
      }
      FileWriter writer = new FileWriter(file);
      writer.write(content);
      writer.close();
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean deleteFile(File file) {
    return file.delete();
  }

  @SneakyThrows
  public String getWorkspaceState() {
    Map<String, String> fileContents = new LinkedHashMap<>();

    for (File file : getFiles(workspaceDir))
      fileContents.put(file.getName(), Files.readFileContents(file));

    return fileContents.entrySet().stream()
      .map(entry -> "FILE \"%s\" {\n%s\n};\n".formatted(entry.getKey(), entry.getValue()))
      .collect(Collectors.joining("\n"));
  }

  public List<File> getFiles(File dir) {
    List<File> files = new ArrayList<>();
    collectFiles(dir, files);
    return files;
  }

  private void collectFiles(File dir, List<File> files) {
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
