package me.jeremiah;

public class Main {

  private static final Thread autonomousAI = new Thread(new AutonomousAI());

  public static void main(String[] args) {
    autonomousAI.start();
  }

}