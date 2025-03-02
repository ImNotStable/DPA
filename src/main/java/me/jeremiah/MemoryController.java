package me.jeremiah;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class MemoryController {

  private final File aiMemoryFile;
  private final LinkedHashMap<String, String> memoryMap;

  private String outputCache = null;

  public MemoryController(File aiMemoryFile) {
    this.aiMemoryFile = aiMemoryFile;
    this.memoryMap = new LinkedHashMap<>();
    loadMemory();
  }

  private void loadMemory() {
    if (aiMemoryFile.exists()) {
      try (BufferedReader reader = new BufferedReader(new FileReader(aiMemoryFile))) {
        String line;
        while ((line = reader.readLine()) != null) {
          String[] parts = line.split(",");
          memoryMap.put(parts[0], parts[1]);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void addMemory(String key, String value) {
    memoryMap.put(key, value);
    saveMemory();
    outputCache = null;
  }

  public void removeMemory(String key) {
    memoryMap.remove(key);
    saveMemory();
    outputCache = null;
  }

  private void saveMemory() {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> entry : memoryMap.entrySet()) {
      sb.append(entry.getKey())
        .append(",")
        .append(entry.getValue())
        .append("\n");
    }
    try (FileWriter fileWriter = new FileWriter(aiMemoryFile)) {
      fileWriter.write(sb.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getMemoryDisplay() {
    if (outputCache != null)
      return outputCache;
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> entry : memoryMap.entrySet())
      sb.append(entry.getKey()).append(" = \"").append(entry.getValue()).append("\";\n");
    return outputCache = sb.toString();
  }

}
