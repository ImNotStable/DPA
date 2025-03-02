package me.jeremiah;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandDispatcher {

  private static final Pattern MERGE_FILE_PATTERN = Pattern.compile("^MERGE\\sFILE\\s\"((?:[\\w\\-_]+/)*)([a-zA-Z0-9\\s_\\\\.\\-():]+)\"\\s\\{\\s?([\\s\\S]+?)\\s?}(;?)");
  private static final Pattern DELETE_FILE_PATTERN = Pattern.compile("DELETE\\sFILE\\s\"((?:[\\w\\-_]+[\\\\/])*)([a-zA-Z0-9\\s_\\\\.\\-():]+)\"(;?)");

  private static final Pattern MEMORY_ADD_PATTERN = Pattern.compile("^MEMORY\\sADD\\s\"([a-zA-Z0-9\\s_\\\\.\\-():]+)\"\\s\\{\\s?([\\s\\S]+?)\\s?}(;?)");
  private static final Pattern MEMORY_REMOVE_PATTERN = Pattern.compile("MEMORY\\s+REMOVE\\s+\"([^\"]+)\"(;?)");

  private static final Pattern RUN_BASH_PATTERN = Pattern.compile("RUN\\s+BASH\\s+\"([^\"]+)\"(;?)");

  private static final Pattern RUN_THINK_MODE = Pattern.compile("THINK\\s+\"([^\"]+)\"(;?)");
  private static final Pattern EXIT_THINK_MODE = Pattern.compile("STOP\\sTHINKING(;?)");

  private final File dataFolder;
  private final AIModel model;
  private final MemoryController memoryController;

  public CommandDispatcher(AutonomousAI autonomousAI) {
    this.dataFolder = autonomousAI.getDataFolder();
    this.model = autonomousAI.getModel();
    this.memoryController = autonomousAI.getMemoryController();
  }

  public List<String> checkForCommands(String response) {
    List<String> output = new ArrayList<>();
    output.addAll(runCommand(MERGE_FILE_PATTERN, response, this::mergeFile));
    output.addAll(runCommand(DELETE_FILE_PATTERN, response, this::deleteFile));
    output.addAll(runCommand(MEMORY_ADD_PATTERN, response, this::addMemory));
    output.addAll(runCommand(MEMORY_REMOVE_PATTERN, response, this::removeMemory));
    output.addAll(runCommand(RUN_BASH_PATTERN, response, this::runBash));
    output.addAll(runCommand(RUN_THINK_MODE, response, this::runThinkMode));
    return output;
  }

  private List<String> runCommand(Pattern pattern, String text, Function<Matcher, String> action) {
    Matcher createFileMatcher = pattern.matcher(text);
    List<String> output = new ArrayList<>();
    while (createFileMatcher.find())
      output.add(action.apply(createFileMatcher));
    return output;
  }

  private String mergeFile(Matcher matcher) {
    String path = matcher.group(1);
    String fileName = matcher.group(2);
    String content = matcher.group(3);
    try (FileWriter fileWriter = new FileWriter(new File(dataFolder, path + fileName))) {
      fileWriter.write(content);
      return "MERGED FILE \"" + path + fileName + "\" {\n" + content + "\n};";
    } catch (IOException e) {
      e.printStackTrace();
      return "FAILED TO MERGE FILE {\n" + e.getMessage() + "\n};";
    }
  }

  private String deleteFile(Matcher matcher) {
    String path = matcher.group(1);
    String fileName = matcher.group(2);
    if (!new File(dataFolder, path + fileName).delete())
      return "FAILED TO DELETE FILE \"" + path + fileName + "\";";
    return "DELETED FILE \"" + path + fileName + "\";";
  }

  private String addMemory(Matcher matcher) {
    String key = matcher.group(1);
    String value = matcher.group(2);
    memoryController.addMemory(key, value);
    return "ADDED MEMORY \"" + key + "\" {\n" + value + "\n};";
  }

  private String removeMemory(Matcher matcher) {
    String key = matcher.group(1);
    memoryController.removeMemory(key);
    return "REMOVED MEMORY \"" + key + "\";";
  }

  private String runBash(Matcher matcher) {
    String command = matcher.group(1);

    try {
      ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);
      pb.redirectErrorStream(true);
      Process process = pb.start();
      BufferedReader reader = new BufferedReader(
        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
      );
      StringBuilder output = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line).append("\n");
      }
      process.waitFor(60, TimeUnit.SECONDS);
      if (process.exitValue() != 0)
        return "ERROR EXECUTING COMMAND;";
      return "BASH COMMAND OUTPUT {\n" + output.toString().trim() + "\n};";
    } catch (Exception e) {
      e.printStackTrace();
      return "ERROR EXECUTING COMMAND {\n" + e.getMessage() + "\n};";
    }
  }

  private String runThinkMode(Matcher matcher) {
    String prompt = matcher.group(1);
    prompt += "\n\nYOU ARE NOW IN THINK MODE, CREATE A PLAN FOR WHAT TO DO, THEN CREATE A PLAN ON HOW TO DO IT, AND WHEN YOU ARE DONE EXECUTE 'STOP THINKING;'.";
    System.out.println();
    System.out.println("THINK MODE");
    System.out.println(prompt);
    StringBuilder responseHistory = new StringBuilder();
    String response;
    do {
      response = model.runModel(prompt + responseHistory);
      responseHistory.append(response).append("\n");
      System.out.println(response);
    } while (!EXIT_THINK_MODE.matcher(response).find());
    return "COMPLETED THINK MODE \"" + prompt + "\" {\n" + responseHistory + "\n};";
  }

}
