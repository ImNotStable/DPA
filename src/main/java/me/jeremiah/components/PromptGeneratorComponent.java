package me.jeremiah.components;

import me.jeremiah.AutonomousAI;
import me.jeremiah.util.AIPrompt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class PromptGeneratorComponent {

  private final File goalsFile = new File("./dpa/goals.txt");
  private final File formattingFile = new File("./dpa/formatting.txt");
  private final File warningsFile = new File("./dpa/warnings.txt");
  private final File contextFile = new File("./dpa/context.txt");

  private List<String> history = new ArrayList<>();

  private final MemoryComponent memoryComponent;
  private final WorkspaceComponent workspaceComponent;

  public PromptGeneratorComponent(AutonomousAI ai) {
    if (!goalsFile.exists() || !formattingFile.exists() || !warningsFile.exists() || !contextFile.exists()) {
      try {
        Files.createFile(goalsFile.toPath());
        Files.createFile(formattingFile.toPath());
        Files.createFile(warningsFile.toPath());
        Files.createFile(contextFile.toPath());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    memoryComponent = ai.getMemoryComponent();
    workspaceComponent = ai.getWorkspaceComponent();
  }

  public AIPrompt generatePrompt() {
    if (history.size() >= 20)
      history = history.subList(history.size() - 10, history.size());

    String goals = readFile(goalsFile);
    String formatting = readFile(formattingFile);
    String warnings = readFile(warningsFile);
    String baseContext = readFile(contextFile);
    String memoryContent = memoryComponent.getMemoryDisplay();
    String workspaceContent = workspaceComponent.getWorkspaceState();

    AIPrompt.Builder promptBuilder = new AIPrompt.Builder();
    promptBuilder.setGoals(goals);
    promptBuilder.setFormatting(formatting);
    promptBuilder.setWarnings(warnings);

    StringBuilder context = new StringBuilder(baseContext + "\n");
    if (!memoryContent.isEmpty())
      context.append("MEMORY {\n").append(memoryContent).append("\n};\n");
    if (!workspaceContent.isEmpty())
      context.append("PROJECT STATE {\n").append(workspaceContent).append("\n};\n");
    promptBuilder.setContext(context.toString());

    return promptBuilder.build();
  }

  public void addHistory(String response, List<String> commandOutput) {
    history.add(response + "\n\nCOMMAND OUTPUT {\n" + String.join("\n", commandOutput) + "\n};");
  }

  private String readFile(File file) {
    try {
      return Files.readString(file.toPath());
    } catch (IOException e) {
      return "";
    }
  }

}
