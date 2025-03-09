package me.jeremiah;

import me.jeremiah.util.AIPrompt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.TimeUnit;

public class AIModel {

  private final String model;

  public AIModel(String model) {
    this.model = model;
  }

  public String runModel(AIPrompt prompt) {
    ProcessBuilder pb = new ProcessBuilder("ollama", "run", model);
    pb.redirectErrorStream(true);
    StringBuilder output = new StringBuilder();
    try {
      Process process = pb.start();

      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
      writer.write(prompt.toString());
      writer.flush();
      writer.close();

      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      while ((line = reader.readLine()) != null)
        output.append(line).append("\n");
      reader.close();

      process.waitFor(600, TimeUnit.SECONDS);
      if (process.exitValue() != 0)
        return "Error running " + model;
    } catch (Exception e) {
      return "Error running " + model + ": " + e.getMessage();
    }
    return clean(output.toString().trim());
  }

  private static String clean(String str) {
    return str
      .replaceAll("\\u001B\\[[0-?]*[ -/]*[@-~]", "")
      .replaceAll("[\\u2800-\\u28FF]+", "")
      .replaceAll("\\r", "")
      .replaceAll("\\u0008", "")
      .replaceAll("```[A-Za-z]*([\\s\\S]*?)```", "$1")
      .replaceAll("\\*\\*(.*)\\*\\*", "$1");
  }

}
