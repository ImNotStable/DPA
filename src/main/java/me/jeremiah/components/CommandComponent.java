package me.jeremiah.components;

import lombok.SneakyThrows;
import me.jeremiah.AutonomousAI;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandComponent {

  private static final Pattern MERGE_FILE_PATTERN = Pattern.compile("MERGE\\sFILE\\s\"([a-zA-Z0-9\\s_\\\\.\\-():]+(?:/[a-zA-Z0-9\\s_\\\\.\\-():]+)*)\"\\s\\{\\n?([\\s\\S]+?)\\n?};");
  private static final Pattern DELETE_FILE_PATTERN = Pattern.compile("DELETE\\sFILE\\s\"([a-zA-Z0-9\\s_\\\\.\\-():]+(?:/[a-zA-Z0-9\\s_\\\\.\\-():]+)*)\"\\s*;");

  private static final Pattern MEMORY_ADD_PATTERN = Pattern.compile("MEMORY\\sADD\\s\"([a-zA-Z0-9\\s_\\\\.\\-():]+)\"\\s\\{\\n?([\\s\\S]+?)\\n?};");
  private static final Pattern MEMORY_REMOVE_PATTERN = Pattern.compile("MEMORY\\s+REMOVE\\s+\"([^\"]+)\";");

  private static final Pattern RUN_BASH_PATTERN = Pattern.compile("RUN\\s+BASH\\s+\"([^\"]+)\";");

  private final MemoryComponent memoryComponent;
  private final WorkspaceComponent workspaceComponent;

  private final List<String> commandOutputCache = new ArrayList<>();

  public CommandComponent(AutonomousAI autonomousAI) {
    this.memoryComponent = autonomousAI.getMemoryComponent();
    this.workspaceComponent = autonomousAI.getWorkspaceComponent();
  }

  public void checkForCommands(String response) {
    runCommand(MERGE_FILE_PATTERN, response, this::mergeFile);
    runCommand(DELETE_FILE_PATTERN, response, this::deleteFile);
    runCommand(MEMORY_ADD_PATTERN, response, this::addMemory);
    runCommand(MEMORY_REMOVE_PATTERN, response, this::removeMemory);
    runCommand(RUN_BASH_PATTERN, response, this::runBash);
  }

  private void runCommand(Pattern pattern, String text, Consumer<MatchResult> action) {
    Matcher createFileMatcher = pattern.matcher(text);
    createFileMatcher.results().forEach(action);
  }

  @SneakyThrows
  private void mergeFile(MatchResult matcher) {
    String filePath = matcher.group(1);
    String content = matcher.group(2);
    File targetFile = workspaceComponent.getRelativeFile(filePath);
    if (workspaceComponent.mergeFile(targetFile, content))
      cacheOutput("MERGED FILE \"" + filePath + "\" {\n" + content + "\n};");
    else
      cacheOutput("FAILED TO MERGE FILE \"" + filePath + "\";");
  }

  private void deleteFile(MatchResult matcher) {
    String filePath = matcher.group(1);
    File targetFile = workspaceComponent.getRelativeFile(filePath);
    if (workspaceComponent.deleteFile(targetFile))
      cacheOutput("DELETED FILE \"" + filePath + "\";");
    else
      cacheOutput("FAILED TO DELETE FILE \"" + filePath + "\";");
  }

  private void addMemory(MatchResult matcher) {
    String key = matcher.group(1);
    String value = matcher.group(2);
    memoryComponent.addMemory(key, value);
    cacheOutput("ADD MEMORY \"" + key + "\" {\n" + value + "\n};");
  }

  private void removeMemory(MatchResult matcher) {
    String key = matcher.group(1);
    memoryComponent.removeMemory(key);
    cacheOutput("REMOVED MEMORY \"" + key + "\";");
  }

  private void runBash(MatchResult matcher) {
    String command = matcher.group(1);

    try {
      ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);
      pb.redirectErrorStream(true);
      Process process = pb.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      StringBuilder output = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null)
        output.append(line).append("\n");

      process.waitFor(60, TimeUnit.SECONDS);
      if (process.exitValue() != 0)
        cacheOutput("ERROR EXECUTING COMMAND \"%s\";".formatted(process.exitValue()));
      else
        cacheOutput("BASH COMMAND OUTPUT {\n" + output.toString().trim() + "\n};");
    } catch (Exception e) {
      e.printStackTrace();
      cacheOutput("ERROR EXECUTING COMMAND {\n" + e.getMessage() + "\n};");
    }
  }

  private void cacheOutput(String output) {
    System.out.println(output);
    commandOutputCache.add(output);
  }

  public List<String> getOutputCache() {
    List<String> output = new ArrayList<>(commandOutputCache);
    commandOutputCache.clear();
    return output;
  }

}
