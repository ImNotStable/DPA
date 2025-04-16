package me.jeremiah.components;

import lombok.SneakyThrows;
import me.jeremiah.util.Files;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class WorkspaceComponent {

  private final File workspaceDir = new File("./dpa/workspace/");

  public WorkspaceComponent() {
    if (!Files.createDirs(workspaceDir))
      throw new RuntimeException("Failed to create workspace directory");
  }

  public File getRelativeFile(String path) {
    return new File(workspaceDir, path);
  }

  public boolean mergeFile(File file, String content) {
    if (!Files.createFile(file))
      return false;
    return Files.setContent(file, content);
  }

  public boolean deleteFile(File file) {
    return file.delete();
  }

  @SneakyThrows
  public String getWorkspaceState() {
    Map<String, String> fileContents = new LinkedHashMap<>();

    for (File file : Files.getFiles(workspaceDir))
      fileContents.put(file.getName(), Files.getContents(file));

    return fileContents.entrySet().stream()
      .map(entry -> "FILE \"%s\" {\n%s\n};\n".formatted(entry.getKey(), entry.getValue()))
      .collect(Collectors.joining("\n"));
  }

}
