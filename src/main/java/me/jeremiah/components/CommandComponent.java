package me.jeremiah.components;

import me.jeremiah.AutonomousAI;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandComponent {

  private static final Pattern MERGE_FILE_PATTERN = Pattern.compile("^MERGE\\sFILE\\s\"((?:[\\w\\-_]+/)*)([a-zA-Z0-9\\s_\\\\.\\-():]+)\"\\s\\{\\s?([\\s\\S]+?)\\s?};");
  private static final Pattern DELETE_FILE_PATTERN = Pattern.compile("DELETE\\sFILE\\s\"((?:[\\w\\-_]+[\\\\/])*)([a-zA-Z0-9\\s_\\\\.\\-():]+)\";");

  private static final Pattern MEMORY_ADD_PATTERN = Pattern.compile("^MEMORY\\sADD\\s\"([a-zA-Z0-9\\s_\\\\.\\-():]+)\"\\s\\{\\s?([\\s\\S]+?)\\s?};");
  private static final Pattern MEMORY_REMOVE_PATTERN = Pattern.compile("MEMORY\\s+REMOVE\\s+\"([^\"]+)\";");

  private static final Pattern RUN_BASH_PATTERN = Pattern.compile("RUN\\s+BASH\\s+\"([^\"]+)\";");

  private final MemoryComponent memoryComponent;
  private final WorkspaceComponent workspaceComponent;

  private List<String> commandOutputCache = new ArrayList<>();

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

  private void runCommand(Pattern pattern, String text, Consumer<Matcher> action) {
    Matcher createFileMatcher = pattern.matcher(text);
    while (createFileMatcher.find())
      action.accept(createFileMatcher);
  }

  private void mergeFile(Matcher matcher) {
    String path = matcher.group(1);
    String fileName = matcher.group(2);
    String content = matcher.group(3);
    File targetFile = workspaceComponent.getRelativeFile(path + fileName);
    System.out.println("Attempting to merge file: " + targetFile.getAbsolutePath());
    if (workspaceComponent.mergeFile(targetFile, content))
      cacheOutput("MERGED FILE \"" + path + fileName + "\" {\n" + content + "\n};");
    else
      cacheOutput("FAILED TO MERGE FILE \"" + path + fileName + "\";");
  }

  private void deleteFile(Matcher matcher) {
    String path = matcher.group(1);
    String fileName = matcher.group(2);
    File targetFile = workspaceComponent.getRelativeFile(path + fileName);
    if (workspaceComponent.deleteFile(targetFile))
      cacheOutput("DELETED FILE \"" + path + fileName + "\";");
    else
      cacheOutput("FAILED TO DELETE FILE \"" + path + fileName + "\";");
  }

  private void addMemory(Matcher matcher) {
    String key = matcher.group(1);
    String value = matcher.group(2);
    memoryComponent.addMemory(key, value);
    cacheOutput("ADD MEMORY \"" + key + "\" {\n" + value + "\n};");
  }

  private void removeMemory(Matcher matcher) {
    String key = matcher.group(1);
    memoryComponent.removeMemory(key);
    cacheOutput("REMOVED MEMORY \"" + key + "\";");
  }

  private void runBash(Matcher matcher) {
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
        cacheOutput("ERROR EXECUTING COMMAND;");
      else
        cacheOutput("BASH COMMAND OUTPUT {\n" + output.toString().trim() + "\n};");
    } catch (Exception e) {
      e.printStackTrace();
      cacheOutput("ERROR EXECUTING COMMAND {\n" + e.getMessage() + "\n};");
    }
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
