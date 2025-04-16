package me.jeremiah.util;

public class Exceptions {

  public static String getPrintable(Throwable throwable) {
    StringBuilder stringBuilder = new StringBuilder(throwable.getMessage());
    stringBuilder.append("\n");
    for (StackTraceElement element : throwable.getStackTrace())
      stringBuilder.append(element).append("\n");
    return stringBuilder.toString();
  }

}
