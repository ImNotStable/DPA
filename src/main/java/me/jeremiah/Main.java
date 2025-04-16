package me.jeremiah;

public class Main {

  private static final Thread ai = new Thread(new AutonomousAI());

  public static void main(String[] args) {
    ai.start();
  }

}