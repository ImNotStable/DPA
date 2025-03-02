package me.jeremiah;

import javax.swing.*;
import java.awt.*;

public class UserInterface extends JFrame {

  private final JTextArea conversationArea;
  private final JTextArea goalsDisplay;

  public UserInterface() {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(800, 600);
    setLayout(new BorderLayout());
    getContentPane().setBackground(new Color(46, 46, 46));

    // Conversation area with scroll pane
    conversationArea = new JTextArea("");
    conversationArea.setEditable(false);
    conversationArea.setLineWrap(true);
    conversationArea.setBackground(new Color(30, 30, 30));
    conversationArea.setForeground(new Color(212, 212, 212));
    conversationArea.setFont(new Font("Helvetica", Font.PLAIN, 12));
    JScrollPane convScrollPane = new JScrollPane(conversationArea);
    convScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    add(convScrollPane, BorderLayout.CENTER);

    // Goals display panel at the bottom
    JPanel goalsPanel = new JPanel(new BorderLayout());
    goalsPanel.setBackground(new Color(46, 46, 46));
    JLabel goalsLabel = new JLabel("Current Goals:");
    goalsLabel.setForeground(Color.WHITE);
    goalsLabel.setFont(new Font("Helvetica", Font.BOLD, 12));
    goalsPanel.add(goalsLabel, BorderLayout.NORTH);
    goalsDisplay = new JTextArea(3, 20);
    goalsDisplay.setEditable(false);
    goalsDisplay.setLineWrap(true);
    goalsDisplay.setBackground(new Color(30, 30, 30));
    goalsDisplay.setForeground(new Color(212, 212, 212));
    goalsDisplay.setFont(new Font("Helvetica", Font.PLAIN, 12));
    JScrollPane goalsScrollPane = new JScrollPane(goalsDisplay);
    goalsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    goalsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    goalsPanel.add(goalsScrollPane, BorderLayout.CENTER);
    add(goalsPanel, BorderLayout.SOUTH);

    setVisible(true);
  }

  public void setTitleText(String text) {
    setTitle(text);
  }

  public void appendConversationText(String text) {
    conversationArea.append(text);
    conversationArea.setCaretPosition(conversationArea.getDocument().getLength());
  }

  public void setGoalsText(String text) {
    goalsDisplay.setText(text);
  }

}
