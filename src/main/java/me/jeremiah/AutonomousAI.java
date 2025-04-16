package me.jeremiah;

import lombok.Getter;
import me.jeremiah.components.CommandComponent;
import me.jeremiah.components.MemoryComponent;
import me.jeremiah.components.PromptGeneratorComponent;
import me.jeremiah.components.WorkspaceComponent;
import me.jeremiah.util.AIPrompt;

import java.util.List;

@Getter
public class AutonomousAI implements Runnable {

  private final AIModel model;
  private final UserInterface ui;

  private final MemoryComponent memoryComponent;
  private final WorkspaceComponent workspaceComponent;
  private final CommandComponent commandComponent;
  private final PromptGeneratorComponent promptGeneratorComponent;

  private boolean running = true;
  private int round = 1;

  public AutonomousAI() {
    model = new AIModel("qwen2.5-coder:7b");
    ui = new UserInterface();
    memoryComponent = new MemoryComponent();
    workspaceComponent = new WorkspaceComponent();
    commandComponent = new CommandComponent(this);
    promptGeneratorComponent = new PromptGeneratorComponent(this);
  }

  @Override
  public void run() {
    while (running) {
      ui.setTitleText("Autonomous AI (Round " + round + ")");

      AIPrompt prompt = promptGeneratorComponent.generatePrompt();

      String response = model.runModel(prompt);
      ui.appendArtificialThoughts(response);

      commandComponent.checkForCommands(response);
      List<String> commandOutput = commandComponent.getOutputCache();
      ui.appendCommandLog(String.join("\n", commandOutput));

      promptGeneratorComponent.addHistory(response, commandOutput);
      round++;
    }
  }

  public void stop() {
    running = false;
  }

}
