package me.jeremiah.util;

public class Formatting {

  public static String format(String title, String main, String content) {
    StringBuilder output = new StringBuilder(title.trim().toUpperCase());
    if (main != null)
      output.append(" \"").append(main).append("\"");
    if (content != null)
      output.append(" {\n").append(content).append("\n}");
    output.append(";");
    return output.toString();
  }

}
