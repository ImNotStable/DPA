package me.jeremiah;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class UserInterface extends JFrame {

  private JTextArea artificialThoughtsArea;
  private JTextArea commandLogArea;
  private JLabel titleLabel;
  private static final Color BACKGROUND_COLOR = new Color(28, 28, 30);
  private static final Color TEXT_AREA_COLOR = new Color(44, 44, 46);
  private static final Color TEXT_COLOR = new Color(235, 235, 240);
  private static final Color ACCENT_COLOR = new Color(0, 122, 255);
  private static final Color CLOSE_HOVER_COLOR = new Color(232, 17, 35);
  private static final Font MAIN_FONT = new Font("SansSerif", Font.PLAIN, 14);
  private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 18);
  private static final Font LABEL_FONT = new Font("SansSerif", Font.BOLD, 14);

  private Point initialClick;

  public UserInterface() {
    SwingUtilities.invokeLater(() -> {
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setSize(900, 700);
      setUndecorated(true); // Remove default window decorations
      setLayout(new BorderLayout(0, 0));
      getContentPane().setBackground(BACKGROUND_COLOR);

      // Custom header panel with gradient
      JPanel headerPanel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
          super.paintComponent(g);
          Graphics2D g2d = (Graphics2D) g;
          g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
          int w = getWidth();
          int h = getHeight();
          Color color1 = new Color(35, 35, 40);
          Color color2 = new Color(45, 45, 50);
          GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
          g2d.setPaint(gp);
          g2d.fillRect(0, 0, w, h);
        }
      };
      headerPanel.setLayout(new BorderLayout());
      headerPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 60, 60)),
        new EmptyBorder(10, 20, 10, 20)
      ));

      // Add window dragging functionality
      headerPanel.addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
          initialClick = e.getPoint();
        }
      });

      headerPanel.addMouseMotionListener(new MouseMotionAdapter() {
        public void mouseDragged(MouseEvent e) {
          // Get location of Window
          int thisX = getLocation().x;
          int thisY = getLocation().y;

          // Determine how much the mouse moved since the initial click
          int xMoved = e.getX() - initialClick.x;
          int yMoved = e.getY() - initialClick.y;

          // Move window to this position
          int X = thisX + xMoved;
          int Y = thisY + yMoved;
          setLocation(X, Y);
        }
      });

      titleLabel = new JLabel("Autonomous AI");
      titleLabel.setFont(TITLE_FONT);
      titleLabel.setForeground(TEXT_COLOR);

      // Create title and icon panel
      JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
      titlePanel.setOpaque(false);
      titlePanel.add(titleLabel);
      headerPanel.add(titlePanel, BorderLayout.WEST);

      // Create window controls panel
      JPanel windowControlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
      windowControlsPanel.setOpaque(false);

      // Minimize button
      JButton minimizeButton = createWindowButton("minimize", new Color(180, 180, 180), e -> setState(JFrame.ICONIFIED));
      windowControlsPanel.add(minimizeButton);

      // Maximize button
      JButton maximizeButton = createWindowButton("maximize", new Color(180, 180, 180), e -> {
        if ((getExtendedState() & JFrame.MAXIMIZED_BOTH) == 0) {
          setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
          setExtendedState(JFrame.NORMAL);
        }
      });
      windowControlsPanel.add(maximizeButton);

      // Close button
      JButton closeButton = createWindowButton("close", CLOSE_HOVER_COLOR, e -> System.exit(0));
      windowControlsPanel.add(closeButton);

      headerPanel.add(windowControlsPanel, BorderLayout.EAST);
      add(headerPanel, BorderLayout.NORTH);

      // Main content panel with padding
      JPanel contentPanel = new JPanel(new BorderLayout());
      contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
      contentPanel.setBackground(BACKGROUND_COLOR);

      // Create split pane with custom UI for the divider
      JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

      // Custom UI for the split pane divider
      splitPane.setUI(new BasicSplitPaneUI() {
        @Override
        public BasicSplitPaneDivider createDefaultDivider() {
          return new BasicSplitPaneDivider(this) {
            @Override
            public void setBorder(javax.swing.border.Border b) {
              // No border for the divider
            }

            @Override
            public void paint(Graphics g) {
              g.setColor(BACKGROUND_COLOR); // Match background color
              g.fillRect(0, 0, getSize().width, getSize().height);
              super.paint(g);
            }
          };
        }
      });

      splitPane.setBorder(null);
      splitPane.setDividerSize(8); // Thin divider
      splitPane.setDividerLocation(600); // Initial divider location (will be adjusted)
      splitPane.setResizeWeight(0.67); // 2/3 for left, 1/3 for right
      splitPane.setContinuousLayout(true);
      splitPane.setBackground(BACKGROUND_COLOR);

      // Create left panel for Artificial Thoughts (larger area)
      JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
      leftPanel.setBackground(BACKGROUND_COLOR);
      leftPanel.setBorder(new EmptyBorder(0, 0, 0, 4)); // Add some spacing on the right

      // Label for Artificial Thoughts
      JLabel thoughtsLabel = new JLabel("Artificial Thoughts");
      thoughtsLabel.setFont(LABEL_FONT);
      thoughtsLabel.setForeground(TEXT_COLOR);

      // Create Artificial Thoughts text area
      artificialThoughtsArea = createStyledTextArea();
      JScrollPane thoughtsScrollPane = createStyledScrollPane(artificialThoughtsArea);

      leftPanel.add(thoughtsLabel, BorderLayout.NORTH);
      leftPanel.add(thoughtsScrollPane, BorderLayout.CENTER);

      // Create right panel for Command Log (smaller area)
      JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
      rightPanel.setBackground(BACKGROUND_COLOR);
      rightPanel.setBorder(new EmptyBorder(0, 4, 0, 0)); // Add some spacing on the left

      // Label for Command Log
      JLabel commandLabel = new JLabel("Command Log");
      commandLabel.setFont(LABEL_FONT);
      commandLabel.setForeground(TEXT_COLOR);

      // Create Command Log text area
      commandLogArea = createStyledTextArea();
      JScrollPane commandScrollPane = createStyledScrollPane(commandLogArea);

      rightPanel.add(commandLabel, BorderLayout.NORTH);
      rightPanel.add(commandScrollPane, BorderLayout.CENTER);

      // Add panels to split pane
      splitPane.setLeftComponent(leftPanel);
      splitPane.setRightComponent(rightPanel);

      contentPanel.add(splitPane, BorderLayout.CENTER);
      add(contentPanel, BorderLayout.CENTER);

      // Add a subtle border to the window
      getRootPane().setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));

      // Center window on screen
      setLocationRelativeTo(null);
      setVisible(true);
    });
  }

  // Helper method for creating styled text areas
  private JTextArea createStyledTextArea() {
    JTextArea textArea = new JTextArea();
    textArea.setEditable(false);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.setBackground(TEXT_AREA_COLOR);
    textArea.setForeground(TEXT_COLOR);
    textArea.setFont(MAIN_FONT);
    textArea.setBorder(new EmptyBorder(10, 10, 10, 10));
    textArea.setMargin(new Insets(10, 10, 10, 10));
    return textArea;
  }

  // Helper method for creating styled scroll panes with rounded corners
  private JScrollPane createStyledScrollPane(JTextArea textArea) {
    JScrollPane scrollPane = new JScrollPane(textArea) {
      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Paint rounded rectangle background
        g2.setColor(TEXT_AREA_COLOR);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
        g2.dispose();
      }
    };

    scrollPane.setOpaque(false);
    scrollPane.getViewport().setOpaque(false);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

    // Modern scrollbar UI
    scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
      @Override
      protected void configureScrollBarColors() {
        this.thumbColor = new Color(100, 100, 100);
        this.trackColor = TEXT_AREA_COLOR;
      }

      @Override
      protected JButton createDecreaseButton(int orientation) {
        return createZeroButton();
      }

      @Override
      protected JButton createIncreaseButton(int orientation) {
        return createZeroButton();
      }

      private JButton createZeroButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(0, 0));
        button.setMinimumSize(new Dimension(0, 0));
        button.setMaximumSize(new Dimension(0, 0));
        return button;
      }

      @Override
      protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
          return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setPaint(thumbColor);
        g2.fillRoundRect(thumbBounds.x + 1, thumbBounds.y + 1,
          thumbBounds.width - 2, thumbBounds.height - 2,
          8, 8);
        g2.dispose();
      }

      @Override
      protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        g.setColor(trackColor);
        g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
      }
    });

    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));

    return scrollPane;
  }

  private JButton createWindowButton(String buttonType, Color hoverColor, ActionListener action) {
    JButton button = new JButton() {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean isHover = getModel().isRollover() || getModel().isPressed();
        g2.setColor(isHover ? hoverColor : new Color(180, 180, 180));

        int w = getWidth();
        int h = getHeight();

        // Draw different symbols based on button type
        switch(buttonType) {
          case "minimize":
            // Horizontal line
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(w/4, h/2, w*3/4, h/2);
            break;
          case "maximize":
            // Square
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRect(w/4, h/4, w/2, h/2);
            break;
          case "close":
            // X shape
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(w/4, h/4, w*3/4, h*3/4);
            g2.drawLine(w*3/4, h/4, w/4, h*3/4);
            break;
        }

        g2.dispose();
      }

      @Override
      public Dimension getPreferredSize() {
        return new Dimension(30, 30);
      }
    };

    button.setFocusPainted(false);
    button.setBorderPainted(false);
    button.setContentAreaFilled(false);
    button.setCursor(new Cursor(Cursor.HAND_CURSOR));

    // Add hover effect
    button.getModel().addChangeListener(e -> button.repaint());
    button.addActionListener(action);

    return button;
  }

  public void setTitleText(String text) {
    SwingUtilities.invokeLater(() -> {
      setTitle(text);
      titleLabel.setText(text);
    });
  }
  public void appendArtificialThoughts(String text) {
    SwingUtilities.invokeLater(() -> {
      artificialThoughtsArea.append("AI:\n" + text + "\n");
      artificialThoughtsArea.setCaretPosition(artificialThoughtsArea.getDocument().getLength());
    });
  }

  public void appendCommandLog(String text) {
    SwingUtilities.invokeLater(() -> {
      commandLogArea.append(text + "\n");
      commandLogArea.setCaretPosition(commandLogArea.getDocument().getLength());
    });
  }

}