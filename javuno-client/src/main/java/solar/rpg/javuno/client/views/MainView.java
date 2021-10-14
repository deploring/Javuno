package solar.rpg.javuno.client.views;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.client.controller.JavunoClientAppController;
import solar.rpg.javuno.client.controller.JavunoClientConnectionController;
import solar.rpg.javuno.mvc.IView;
import solar.rpg.javuno.mvc.JMVC;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.logging.Logger;

public class MainView extends JFrame implements IView {

    @NotNull
    private final Logger logger;
    @NotNull
    private final JMVC<MainView, JavunoClientAppController> mvc;
    private DefaultTableModel playerTableModel;
    @NotNull
    private final ServerConnectView serverConnectView;
    @NotNull
    private final JPanel mainPanel;
    private JTextPane logTextPane;
    private JTextField chatTextField;

    public MainView(@NotNull Logger logger) {
        super("Javuno 1.0.0");
        this.logger = logger;

        JavunoClientAppController appController = new JavunoClientAppController(logger);
        mvc = appController.getMVC();
        mvc.set(this, appController);

        JMVC<ServerConnectView, JavunoClientConnectionController> serverConnectMVC =
                appController.getConnectionController().getMVC();
        serverConnectView = new ServerConnectView(serverConnectMVC);
        serverConnectMVC.set(serverConnectView, appController.getConnectionController());

        mainPanel = new JPanel();
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
        mainPanel.removeAll();
        mainPanel.add(panel, BorderLayout.CENTER);
        revalidate();
        repaint();
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void generateUI() {
        playerTableModel = new DefaultTableModel(new Object[]{"Player Name", "Current Status"}, 0);
        JTable playerTable = new JTable(playerTableModel);
        playerTable.setMaximumSize(new Dimension(300, 140));
        playerTable.setPreferredScrollableViewportSize(new Dimension(300, 140));
        JScrollPane playerScrollPane = new JScrollPane(playerTable);
        playerScrollPane.setMinimumSize(new Dimension(300, 140));

        logTextPane = new JTextPane();
        logTextPane.setEnabled(false);
        JScrollPane logScrollPane = new JScrollPane(logTextPane);
        logScrollPane.setMinimumSize(new Dimension(300, 525));

        JPanel chatPanel = new JPanel(new BorderLayout());
        chatTextField = new JTextField(10);
        chatTextField.setDocument(new JTextFieldLimit(300));
        JButton sendButton = new JButton("Send");

        sendButton.addActionListener((e) -> SwingUtilities.invokeLater(this::onSendChatClick));

        chatPanel.add(chatTextField, BorderLayout.CENTER);
        chatPanel.add(sendButton, BorderLayout.EAST);

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                "Information",
                TitledBorder.LEFT,
                TitledBorder.TOP));

        infoPanel.add(playerScrollPane, BorderLayout.NORTH);
        infoPanel.add(logScrollPane, BorderLayout.CENTER);
        infoPanel.add(chatPanel, BorderLayout.SOUTH);

        mainPanel.setLayout(new BorderLayout());
        mainPanel.setMinimumSize(new Dimension(600, 700));
        mainPanel.setPreferredSize(mainPanel.getMinimumSize());

        getContentPane().setLayout(new BorderLayout());
        JSplitPane contentSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, infoPanel, mainPanel);
        contentSplitPane.setDividerLocation(300);
        getContentPane().add(contentSplitPane, BorderLayout.CENTER);
    }

    private void onSendChatClick() {
        String chatToSend = chatTextField.getText();
        assert chatToSend.length() <= 300 : "Chat length longer than allowed (300chars)";

        //TODO: Disable chat box if not connected?
        if (chatToSend.length() == 0) return;

        appendChatLog(String.format("<Test Chat>: %s", chatToSend));
        chatTextField.setText("");
        //TODO: Send chat to server, other clients receive as packet?
    }

    private void appendChatLog(@NotNull String messageToAdd) {
        assert messageToAdd.strip().length() > 1;
        String existingTextLog = logTextPane.getText();

        logTextPane.setText(existingTextLog + (existingTextLog.length() > 0 ? "\n" : "") + messageToAdd);
    }

    @Override
    public void reset() {
        //TODO Case where view reset is needed?
        throw new UnsupportedOperationException();
    }

    @Override
    public JPanel getPanel() {
        return mainPanel;
    }

    @NotNull
    @Override
    public JMVC<MainView, JavunoClientAppController> getMVC() {
        return mvc;
    }

    /**
     * Denotes all the different views that can be shown inside {@code MainView}.
     */
    public enum ViewType {
        SERVER_CONNECT,
        MAIN_GAME
    }
}
