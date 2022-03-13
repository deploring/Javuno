package solar.rpg.javuno.client.views;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.client.controller.ClientAppController;
import solar.rpg.javuno.client.controller.ClientGameController;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.models.packets.in.JavunoPacketInOutChatMessage;
import solar.rpg.javuno.mvc.IView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import java.io.IOException;
import java.util.Objects;

public class ViewInformation implements IView {

    @NotNull
    private final JavunoClientMVC<ViewInformation, ClientAppController> mvc;

    /* UI Components */
    private JPanel rootPanel;
    private JTable playerTable;
    private JTextField chatTextField;
    private JButton sendButton;
    private JEditorPane logEditorPane;
    private HTMLDocument logEditorHtml;
    private final DefaultTableModel playerTableModel;

    public ViewInformation(@NotNull JavunoClientMVC<ViewInformation, ClientAppController> mvc) {
        this.mvc = mvc;

        playerTableModel = new DefaultTableModel(new Object[]{"Player Name", "Current Status"}, 0);
        playerTable.setModel(playerTableModel);

        logEditorHtml = (HTMLDocument) logEditorPane.getDocument();

        //TODO: Remove first paragraph. Unneeded.

        chatTextField.setDocument(new JTextFieldLimit(300));
        chatTextField.addActionListener((e) -> onSendChatClick());
        sendButton.addActionListener((e) -> onSendChatClick());
    }

    /* Server Events */

    public void onConnected() {
        refreshPlayerTable();
        mvc.logClientEvent(String.format(
            "> Connection successful! There are %d player(s) in the lobby.",
            getGameController().getGameLobbyModel().getLobbyPlayerNames().size()
        ));
        setChatEnabled(true);
    }

    public void onDisconnected() {
        clearPlayerTable();
        setChatEnabled(false);
    }

    /* Field Getters & Setters */

    @NotNull
    public JPanel getPanel() {
        return rootPanel;
    }

    @NotNull
    private ClientGameController getGameController() {
        return mvc.getController().getGameController();
    }

    @NotNull
    @Override
    public JavunoClientMVC<ViewInformation, ClientAppController> getMVC() {
        return mvc;
    }

    /* UI Manipulation */

    public void appendEventToLog(@NotNull String messageToAdd) {
        assert messageToAdd.strip().length() > 1;

        //TODO: Add HTML here.
        try {
            logEditorHtml.insertBeforeEnd(
                logEditorHtml.getElement(
                    logEditorHtml.getDefaultRootElement(),
                    StyleConstants.NameAttribute,
                    HTML.Tag.BODY
                ),
                "<p>" + messageToAdd + "</p>"
            );
        } catch (IOException | BadLocationException e) {
            e.printStackTrace();
        }

        DefaultCaret caret = (DefaultCaret) logEditorPane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }

    public void refreshPlayerTable() {
        clearPlayerTable();
        for (String playerName : getGameController().getGameLobbyModel().getLobbyPlayerNames())
            playerTableModel.addRow(new String[]{playerName, getGameController().getPlayerStatus(playerName)});
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
        if (chatToSend.isEmpty()) return;

        String playerName = Objects.requireNonNull(mvc.getAppController().getGameController().getPlayerName());
        JavunoPacketInOutChatMessage chatPacket = new JavunoPacketInOutChatMessage(chatToSend, playerName);
        mvc.getAppController().getConnectionController().getClientConnection().writePacket(chatPacket);
        chatTextField.setText("");
    }
}
