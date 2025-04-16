package me.jeremiah.util;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

public class AIPrompt {

  private final String goal, history, formatting, warnings, context;

  private AIPrompt(String goal, String history, String formatting, String warnings, String context) {
    this.goal = goal;
    this.history = history;
    this.formatting = formatting;
    this.warnings = warnings;
    this.context = context;
  }

  @Override
  public String toString() {
    new JsonObject();
    StringBuilder builder = new StringBuilder();
    builder.append("GOALS {\n%s\n};\n".formatted(goal));
    if (history != null)
      builder.append("HISTORY {\n%s\n};\n".formatted(history));
    if (formatting != null)
      builder.append("FORMATTING {\n%s\n};\n".formatted(formatting));
    if (warnings != null)
      builder.append("WARNINGS {\n%s\n};\n".formatted(warnings));
    if (context != null)
      builder.append("CONTEXT DUMP {\n%s\n};\n".formatted(context));
    return builder.toString();
  }

  @Setter
  @RequiredArgsConstructor
  public static class Builder {

    private String goals, history, formatting, warnings, context;

    public AIPrompt build() {
      if (goals == null)
        throw new IllegalStateException("Goal cannot be null");
      return new AIPrompt(goals, history, formatting, warnings, context);
    }

  }

}
