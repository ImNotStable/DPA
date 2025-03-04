package me.jeremiah;

import lombok.Getter;
import me.jeremiah.components.CommandComponent;
import me.jeremiah.components.MemoryComponent;
import me.jeremiah.components.PromptGeneratorComponent;
import me.jeremiah.components.WorkspaceComponent;
import me.jeremiah.util.AIModel;
import me.jeremiah.util.AIPrompt;

import java.util.List;

@Getter
public class AutonomousAI {

  private final AIModel model;
  private final UserInterface ui;

  private final MemoryComponent memoryComponent;
  private final WorkspaceComponent workspaceComponent;
  private final CommandComponent commandComponent;
  private final PromptGeneratorComponent promptGeneratorComponent;

  public AutonomousAI() {
    model = new AIModel("qwen2.5-coder:7b");
    ui = new UserInterface();
    memoryComponent = new MemoryComponent();
    workspaceComponent = new WorkspaceComponent();
    commandComponent = new CommandComponent(this);
    promptGeneratorComponent = new PromptGeneratorComponent(this);

    Thread conversationThread = new Thread(this::startConversation);
    conversationThread.start();
  }

  private void startConversation() {
    int roundNumber = 1;
    while (true) {
      ui.setTitleText("Autonomous AI - Round " + roundNumber);

      AIPrompt prompt = promptGeneratorComponent.generatePrompt();

      String response = model.runModel(prompt);
      ui.appendConversationText("AI: \n" + response);

      commandComponent.checkForCommands(response);
      List<String> commandOutput = commandComponent.getOutputCache();
      commandOutput.forEach(System.out::println);

      promptGeneratorComponent.addHistory(response, commandOutput);
      System.out.printf("Successfully completed round %d\n", roundNumber++);
    }
  }

}
