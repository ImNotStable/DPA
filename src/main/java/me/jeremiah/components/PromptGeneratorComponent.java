package me.jeremiah.components;

import me.jeremiah.AutonomousAI;
import me.jeremiah.util.AIPrompt;
import me.jeremiah.util.Files;

import java.io.File;
import java.io.IOException;
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

  @SuppressWarnings("ResultOfMethodCallIgnored")
  public PromptGeneratorComponent(AutonomousAI ai) {
    if (!goalsFile.exists() || !formattingFile.exists() || !warningsFile.exists() || !contextFile.exists()) {
      try {
        goalsFile.createNewFile();
        formattingFile.createNewFile();
        warningsFile.createNewFile();
        contextFile.createNewFile();
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

    String goals = Files.readFileContents(goalsFile);
    String formatting = Files.readFileContents(formattingFile);
    String warnings = Files.readFileContents(warningsFile);
    String baseContext = Files.readFileContents(contextFile);
    String memoryContent = memoryComponent.getMemoryDisplay();
    String workspaceContent = workspaceComponent.getWorkspaceState();

    AIPrompt.Builder promptBuilder = new AIPrompt.Builder();
    if (!history.isEmpty())
      goals += "\n\n" + history.getLast();
    promptBuilder.setGoals(goals);
    if (!history.isEmpty())
      promptBuilder.setHistory(String.join("\n", history.subList(0, history.size() - 1)));
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

}
