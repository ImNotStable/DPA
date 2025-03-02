package me.jeremiah;

import lombok.Getter;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
public class AutonomousAI {

  private final AIModel model;
  private final UserInterface ui;
  private final CommandDispatcher commandDispatcher;
  private final MemoryController memoryController;

  private final File initialPromptFile = new File("./src/main/resources/ai_prompt.txt");
  private final File goalsFile = new File("./src/main/resources/goals.txt");
  private final File aiMemoryFile = new File("./src/main/resources/AI_MEMORY.csv");

  private final File dataFolder = new File("P:/Conversational AI/data/");

  private String initialPrompt;
  private String goals;

  public AutonomousAI() {
    dataFolder.mkdirs();

    model = new AIModel("codellama:7b");//new AIModel("qwen2.5-coder:7b");
    ui = new UserInterface();
    memoryController = new MemoryController(aiMemoryFile);
    commandDispatcher = new CommandDispatcher(this);

    Thread conversationThread = new Thread(this::converse);
    conversationThread.start();
  }

  private void appendConvText(String text) {
    SwingUtilities.invokeLater(() -> ui.appendConversationText(text + "\n"));
  }

  private String readFile(File file) {
    try {
      return Files.readString(file.toPath());
    } catch (IOException e) {
      return "";
    }
  }

  private String getProjectState() {
    List<File> files = getFiles(dataFolder);

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

  private void converse() {
    String progressLog = "";
    int roundNumber = 1;
    while (true) {
      ui.setTitleText("Autonomous AI - Round " + roundNumber);
      initialPrompt = readFile(initialPromptFile);
      goals = readFile(goalsFile);

      SwingUtilities.invokeLater(() -> ui.setGoalsText(goals));

      String memoryContent = memoryController.getMemoryDisplay();
      String aiFilesStr = getProjectState();

      StringBuilder combinedPrompt = new StringBuilder(initialPrompt);
      combinedPrompt
        .append("\n\n")
        .append(progressLog)
        .append("\n\n");

      if (!goals.isEmpty()) {
        combinedPrompt
          .append("These goals are here to guide your actions, please follow them {\n")
          .append(goals)
          .append("\n};\n");
      }
      if (!memoryContent.isEmpty()) {
        combinedPrompt
          .append("Your Memory {\n")
          .append(memoryContent)
          .append("\n};\n");
      }
      if (!aiFilesStr.isEmpty()) {
        combinedPrompt
          .append("Here is the current project state {\n")
          .append(aiFilesStr)
          .append("\n};\n");
      }

      if (progressLog.length() > 4000)
        progressLog = "[Summary of previous rounds]\n" + progressLog.substring(progressLog.length() - 2000);

      String response = model.runModel(combinedPrompt.toString());
      appendConvText("AI: \n" + response);

      List<String> commandOutput = commandDispatcher.checkForCommands(response);

      commandOutput.forEach(System.out::println);

      //System.out.println("--------------------------------");
      //System.out.println(combinedPrompt);
      //System.out.println("--------------------------------");

      progressLog += "\n" + response + "\n" + String.join("\n", commandOutput);
      roundNumber++;
    }
  }

}
