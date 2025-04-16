package me.jeremiah;

import me.jeremiah.util.AIPrompt;
import me.jeremiah.util.Exceptions;
import me.jeremiah.util.Formatting;

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
        return Formatting.format("ERROR", model, "Model exited with non-zero status code (%s)".formatted(process.exitValue()));
    } catch (Exception e) {
      return Formatting.format("ERROR", model, Exceptions.getPrintable(e));
    }
    return clean(output.toString().trim());
  }

  private static String clean(String str) {
    return str
      .replaceAll("\\u001B\\[[0-?]*[ -/]*[@-~]", "") // Standard ANSI escape sequences
      .replaceAll("\\[\\?[0-9;]*[a-zA-Z]", "") // Control sequence introducers like [?2026h
      .replaceAll("[\\u2800-\\u28FF]+", "") // Braille pattern Unicode characters
      .replaceAll("\\r", "") // Carriage return characters
      .replaceAll("\\u0008", "") // Backspace characters
      .replaceAll("```[A-Za-z]*([\\s\\S]*?)```", "$1") // Markdown code blocks, keeping only content
      .replaceAll("\\*\\*(.*)\\*\\*", "$1") // Markdown bold syntax, keeping only the text
      .trim();
  }

}
