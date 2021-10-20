package solar.rpg.javuno.client.views;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.client.controller.ClientAppController;
import solar.rpg.javuno.client.models.ClientGameLobbyModel;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.models.packets.JavunoPacketInOutChatMessage;
import solar.rpg.javuno.mvc.IView;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Objects;

public class ViewInformation implements IView {

    @NotNull
    private final JavunoClientMVC<ViewInformation, ClientAppController> mvc;
    @NotNull
    private final JPanel rootPanel;

    private DefaultTableModel playerTableModel;
    private JTextArea logTextPane;
    private JTextField chatTextField;
    private JButton sendButton;

    public ViewInformation(@NotNull JavunoClientMVC<ViewInformation, ClientAppController> mvc) {
        this.mvc = mvc;
        rootPanel = new JPanel(new BorderLayout());
        generateUI();
    }

    private ClientGameLobbyModel getLobbyModel() {
        return mvc.getController().getGameController().getLobbyModel();
    }

    public void onConnected() {
        refreshPlayerTable();
        mvc.logClientEvent(String.format("> Connection successful! There are %d player(s) in the lobby.",
                                         getLobbyModel().getLobbyPlayerNames().size()));
        setChatEnabled(true);
    }

    public void onDisconnected() {
        mvc.logClientEvent("> You have been disconnected from the server.");
        clearPlayerTable();
        setChatEnabled(false);
    }

    public void refreshPlayerTable() {
        clearPlayerTable();
        List<String> lobbyPlayerNames = getLobbyModel().getLobbyPlayerNames();
        for (String playerName : lobbyPlayerNames) playerTableModel.addRow(new String[]{playerName, "Waiting"});
    }

    private void clearPlayerTable() {
        playerTableModel.setRowCount(0);
    }


    private void setChatEnabled(boolean enabled) {
        chatTextField.setEnabled(enabled);
        if (!enabled) chatTextField.setText("");
        sendButton.setEnabled(enabled);
    }

    private void onSendChatClick() {
        String chatToSend = chatTextField.getText();
        assert chatToSend.length() <= 300 : "Chat length longer than allowed (300chars)";

        if (chatToSend.isEmpty()) return;

        String playerName = Objects.requireNonNull(mvc.getAppController().getGameController().getPlayerName());
        JavunoPacketInOutChatMessage chatPacket = new JavunoPacketInOutChatMessage(chatToSend, playerName);
        mvc.getAppController().getConnectionController().getClientConnection().writePacket(chatPacket);

        SwingUtilities.invokeLater(() -> {
            mvc.logClientEvent(chatPacket.getMessageFormat());
            chatTextField.setText("");
        });
    }

    public void appendEventToLog(@NotNull String messageToAdd) {
        assert messageToAdd.strip().length() > 1;
        String existingTextLog = logTextPane.getText();

        logTextPane.setText(existingTextLog + (existingTextLog.length() > 0 ? "\n" : "") + messageToAdd);
    }

    @Override
    public void generateUI() {
        playerTableModel = new DefaultTableModel(new Object[]{"Player Name", "Current Status"}, 0);
        JTable playerTable = new JTable(playerTableModel);
        playerTable.setMaximumSize(new Dimension(300, 140));
        playerTable.setPreferredScrollableViewportSize(new Dimension(300, 140));
        playerTable.setEnabled(false);
        JScrollPane playerScrollPane = new JScrollPane(playerTable);
        playerScrollPane.setMinimumSize(new Dimension(300, 140));

        logTextPane = new JTextArea();
        logTextPane.setEnabled(false);
        logTextPane.setFont(new Font("Courier New", Font.PLAIN, 12));
        logTextPane.setDisabledTextColor(Color.BLACK);
        logTextPane.setWrapStyleWord(true);
        logTextPane.setLineWrap(true);

        JScrollPane logScrollPane = new JScrollPane(logTextPane);
        logScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                "Chat + Event Log",
                TitledBorder.LEFT,
                TitledBorder.TOP));
        logScrollPane.setMinimumSize(new Dimension(300, 525));
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatTextField = new JTextField(10);
        chatTextField.setDocument(new JTextFieldLimit(300));
        sendButton = new JButton("Send");
        setChatEnabled(false);

        chatTextField.addActionListener((e) -> onSendChatClick());
        sendButton.addActionListener((e) -> onSendChatClick());

        chatPanel.add(chatTextField, BorderLayout.CENTER);
        chatPanel.add(sendButton, BorderLayout.EAST);

        rootPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                "Information",
                TitledBorder.LEFT,
                TitledBorder.TOP));

        rootPanel.add(playerScrollPane, BorderLayout.NORTH);
        rootPanel.add(logScrollPane, BorderLayout.CENTER);
        rootPanel.add(chatPanel, BorderLayout.SOUTH);
    }

    @Override
    public JPanel getPanel() {
        return rootPanel;
    }

    @NotNull
    @Override
    public JavunoClientMVC<ViewInformation, ClientAppController> getMVC() {
        return mvc;
    }
}
