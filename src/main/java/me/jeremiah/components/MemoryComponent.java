package me.jeremiah.components;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import me.jeremiah.util.Formatting;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MemoryComponent {

  private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  private final File aiMemoryFile = new File("./dpa/AI_MEMORY.json");
  private final LinkedHashMap<String, String> memoryMap = new LinkedHashMap<>();

  private String outputCache = null;

  public MemoryComponent() {
    loadMemory();
  }

  @SneakyThrows
  private void loadMemory() {
    if (!aiMemoryFile.exists() && aiMemoryFile.createNewFile()) {
      saveMemory();
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
    if (memoryMap.remove(key) == null)
      return;
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
    outputCache = memoryMap.entrySet().stream()
      .map(entry -> Formatting.format("MEMORY", entry.getKey(), entry.getValue()))
      .collect(Collectors.joining("\n"));
    return outputCache;
  }

}
