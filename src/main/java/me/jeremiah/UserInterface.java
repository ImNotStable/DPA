package me.jeremiah;

import javax.swing.*;
import java.awt.*;

public class UserInterface extends JFrame {

  private JTextArea conversationArea;

  public UserInterface() {
    SwingUtilities.invokeLater(() -> {
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

      setVisible(true);
    });
  }

  public void setTitleText(String text) {
    SwingUtilities.invokeLater(() -> setTitle(text));
  }

  public void appendConversationText(String text) {
    SwingUtilities.invokeLater(() -> {
      conversationArea.append(text + "\n");
      conversationArea.setCaretPosition(conversationArea.getDocument().getLength());
    });
  }

}
