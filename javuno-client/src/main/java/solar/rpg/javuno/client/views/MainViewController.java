package solar.rpg.javuno.client.views;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.client.controller.JavunoClientAppController;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MainViewController extends JFrame {

    private DefaultTableModel playerTableModel;
    @NotNull
    private final ServerConnectView serverConnectView;
    @NotNull
    private final JavunoClientAppController appController;
    @NotNull
    private JPanel gamePanel;

    public MainViewController() {
        super("Javuno 1.0.0");

        serverConnectView = new ServerConnectView();
        generateUI();
        showView(ViewType.SERVER_CONNECT);
    }

    public void showView(ViewType viewType) {
        switch (viewType) {
            case SERVER_CONNECT -> swapPanel(serverConnectView.getPanel());
            case MAIN_GAME -> throw new IllegalCallerException();
        }
    }

    private void swapPanel(JPanel panel) {
        gamePanel.removeAll();
        gamePanel.add(panel, BorderLayout.CENTER);
        revalidate();
        repaint();
        gamePanel.revalidate();
        gamePanel.repaint();
    }

    public void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public void generateUI() {
        playerTableModel = new DefaultTableModel(new Object[]{"Player Name", "Current Status"}, 0);
        JTable playerTable = new JTable(playerTableModel);
        playerTable.setPreferredScrollableViewportSize(new Dimension(300, 140));
        JScrollPane playerScrollPane = new JScrollPane(playerTable);
        playerScrollPane.setMinimumSize(new Dimension(300, 140));

        JTextPane logTextPane = new JTextPane();
        logTextPane.setEnabled(false);
        JScrollPane logScrollPane = new JScrollPane(logTextPane);
        logScrollPane.setMinimumSize(new Dimension(300, 525));

        JPanel chatPanel = new JPanel(new BorderLayout());
        JTextField chatTextField = new JTextField(10);
        JButton sendButton = new JButton("Send");

        sendButton.addActionListener((e) -> logTextPane.setText(logTextPane.getText() + "\n" + Math.random()));

        chatPanel.add(chatTextField, BorderLayout.CENTER);
        chatPanel.add(sendButton, BorderLayout.EAST);

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                "Information",
                TitledBorder.LEFT,
                TitledBorder.TOP));
        infoPanel.setMinimumSize(new Dimension(300, 700));
        infoPanel.setPreferredSize(infoPanel.getMinimumSize());

        infoPanel.add(playerScrollPane, BorderLayout.NORTH);
        infoPanel.add(logScrollPane, BorderLayout.CENTER);
        infoPanel.add(chatPanel, BorderLayout.SOUTH);

        gamePanel = new JPanel(new BorderLayout());
        gamePanel.setMinimumSize(new Dimension(600, 700));
        gamePanel.setPreferredSize(gamePanel.getMinimumSize());

        getContentPane().setLayout(new BorderLayout());
        JSplitPane contentSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, infoPanel, gamePanel);
        contentSplitPane.setDividerLocation(300);
        getContentPane().add(contentSplitPane, BorderLayout.CENTER);
    }

    /**
     * Denotes all the different views that can be shown inside {@code MainView}.
     */
    public enum ViewType {
        SERVER_CONNECT,
        MAIN_GAME
    }
}
