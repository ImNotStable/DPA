package me.jeremiah.components;

import lombok.SneakyThrows;
import me.jeremiah.AutonomousAI;
import me.jeremiah.util.Exceptions;
import me.jeremiah.util.Formatting;

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

  private static final Pattern MERGE_FILE_PATTERN = Pattern.compile("MERGE\\sFILE\\s\"([a-zA-Z0-9\\s_\\\\.\\-():]+(?:/[a-zA-Z0-9\\s_\\\\.\\-():]+)*)\"\\s\\{\\n?([\\s\\S]+?)\\n};");
  private static final Pattern DELETE_FILE_PATTERN = Pattern.compile("DELETE\\sFILE\\s\"([a-zA-Z0-9\\s_\\\\.\\-():]+(?:/[a-zA-Z0-9\\s_\\\\.\\-():]+)*)\"\\s*;");

  private static final Pattern MEMORY_ADD_PATTERN = Pattern.compile("MEMORY\\sADD\\s\"([a-zA-Z0-9\\s_\\\\.\\-():]+)\"\\s\\{\\n?([\\s\\S]+?)\\n};");
  private static final Pattern MEMORY_REMOVE_PATTERN = Pattern.compile("MEMORY\\s+REMOVE\\s+\"([^\"]+)\";");

  private static final Pattern RUN_BASH_PATTERN = Pattern.compile("RUN\\s+BASH\\s+\"([^\"]+)\";");

  private final WorkspaceComponent workspaceComponent;
  private final MemoryComponent memoryComponent;

  private final List<String> commandOutputCache = new ArrayList<>();

  public CommandComponent(AutonomousAI autonomousAI) {
    this.workspaceComponent = autonomousAI.getWorkspaceComponent();
    this.memoryComponent = autonomousAI.getMemoryComponent();
  }

  public void checkForCommands(String response) {
    runCommand(MERGE_FILE_PATTERN, response, this::mergeFile);
    runCommand(DELETE_FILE_PATTERN, response, this::deleteFile);
    runCommand(MEMORY_ADD_PATTERN, response, this::addMemory);
    runCommand(MEMORY_REMOVE_PATTERN, response, this::removeMemory);
    runCommand(RUN_BASH_PATTERN, response, this::runBash);
  }

  private void runCommand(Pattern pattern, String text, Consumer<MatchResult> action) {
    Matcher matcher = pattern.matcher(text);
    matcher.results().forEach(action);
  }

  @SneakyThrows
  private void mergeFile(MatchResult matcher) {
    String filePath = matcher.group(1);
    String content = matcher.group(2);
    File targetFile = workspaceComponent.getRelativeFile(filePath);
    if (workspaceComponent.mergeFile(targetFile, content))
      cacheOutput("MERGED FILE", filePath, content);
    else
      cacheOutput("FAILED TO MERGE FILE", filePath);
  }

  private void deleteFile(MatchResult matcher) {
    String filePath = matcher.group(1);
    File targetFile = workspaceComponent.getRelativeFile(filePath);
    if (workspaceComponent.deleteFile(targetFile))
      cacheOutput("DELETED FILE", filePath);
    else
      cacheOutput("FAILED TO DELETE FILE", filePath);
  }

  private void addMemory(MatchResult matcher) {
    String key = matcher.group(1);
    String value = matcher.group(2);
    memoryComponent.addMemory(key, value);
    cacheOutput("ADDED MEMORY", key);
  }

  private void removeMemory(MatchResult matcher) {
    String key = matcher.group(1);
    memoryComponent.removeMemory(key);
    cacheOutput("REMOVED MEMORY", key);
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
        cacheOutput("ERROR EXECUTING BASH COMMAND", String.valueOf(process.exitValue()), null);
      else
        cacheOutput("BASH COMMAND OUTPUT", null, output.toString().trim());
    } catch (Exception e) {
      cacheOutput("ERROR EXECUTING BASH COMMAND", null, Exceptions.getPrintable(e));
    }
  }

  private void cacheOutput(String title, String main) {
    cacheOutput(Formatting.format(title, main, null));
  }

  private void cacheOutput(String title, String main, String content) {
    cacheOutput(Formatting.format(title, main, content));
  }

  private void cacheOutput(String output) {
    commandOutputCache.add(output);
  }

  public List<String> getOutputCache() {
    List<String> output = new ArrayList<>(commandOutputCache);
    commandOutputCache.clear();
    return output;
  }

}
