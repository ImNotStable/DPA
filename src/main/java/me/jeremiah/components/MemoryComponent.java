package me.jeremiah.components;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class MemoryComponent {

  private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  private final File aiMemoryFile = new File("./dpa/AI_MEMORY.json");
  private final LinkedHashMap<String, String> memoryMap;

  private String outputCache = null;

  public MemoryComponent() {
    this.memoryMap = new LinkedHashMap<>();
    loadMemory();
  }

  @SneakyThrows
  private void loadMemory() {
    if (!aiMemoryFile.exists()) {
      aiMemoryFile.createNewFile();
      return;
    }
    JsonObject root = gson.fromJson(new FileReader(aiMemoryFile), JsonObject.class);
    for (Map.Entry<String, JsonElement> entry : root.entrySet())
      memoryMap.put(entry.getKey(), entry.getValue().getAsString());
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
    try (FileWriter writer = new FileWriter(aiMemoryFile)) {
      gson.toJson(memoryMap, writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getMemoryDisplay() {
    if (outputCache != null)
      return outputCache;
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> entry : memoryMap.entrySet())
      sb.append("MEMORY \"%s\" {\n%s\n};\n".formatted(entry.getKey(), entry.getValue()));
    return outputCache = sb.toString();
  }

}
