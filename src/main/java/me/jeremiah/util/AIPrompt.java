package me.jeremiah.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
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
    StringBuilder builder = new StringBuilder();
    builder.append("GOALS {\n").append(goal).append("\n};\n");
    if (history != null)
      builder.append("HISTORY {\n").append(history).append("\n};\n");
    if (formatting != null)
      builder.append("FORMATTING {\n").append(formatting).append("\n};\n");
    if (warnings != null)
      builder.append("WARNINGS {\n").append(warnings).append("\n};\n");
    if (context != null)
      builder.append("CONTEXT DUMP {\n").append(context).append("\n};\n");
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
