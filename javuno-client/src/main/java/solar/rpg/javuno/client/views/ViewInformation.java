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
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import java.io.IOException;
import java.util.Objects;

/**
 * This view is responsible for displaying various information to the user, including:
 * <ul>
 *     <li>All players in the lobby and their current status (if applicable).</li>
 *     <li>Events from the system and game play, as well as chat messages from other players.</li>
 *     <ul>Chat box and "Send" button to send chat messages to other players.</ul>
 * </ul>
 *
 * @author jskinner
 * @since 1.0.0
 */
public class ViewInformation implements IView {

    @NotNull
    private final JavunoClientMVC<ViewInformation, ClientAppController> mvc;

    /* UI Components */
    private JPanel rootPanel;
    private JTable playerTable;
    private JTextField chatTextField;
    private JButton sendButton;
    private JEditorPane logEditorPane;
    private JScrollPane logScrollPane;
    private final HTMLDocument logEditorHtml;
    private final Element logBodyElement;
    private boolean firstLineRemoved;
    private final DefaultTableModel playerTableModel;

    /**
     * Constructs a new {@code ViewInformation} instance.
     *
     * @param mvc MVC relationship between this view and the {@link ClientAppController}.
     */
    public ViewInformation(@NotNull JavunoClientMVC<ViewInformation, ClientAppController> mvc) {
        this.mvc = mvc;

        playerTableModel = new DefaultTableModel(new Object[]{"Player Name", "Current Status"}, 0);
        playerTable.setModel(playerTableModel);

        logEditorHtml = (HTMLDocument) logEditorPane.getDocument();
        logBodyElement = logEditorHtml.getElement(
            logEditorHtml.getDefaultRootElement(),
            StyleConstants.NameAttribute,
            HTML.Tag.BODY
        );
        firstLineRemoved = false;

        // Keeps the scroll pane scrolling with new content if it is at or near the bottom.
        logScrollPane.getVerticalScrollBar().addAdjustmentListener(
            e -> {
                if ((e.getAdjustable().getValue() - e.getAdjustable().getMaximum()) > -logScrollPane.getHeight() - 20) {
                    e.getAdjustable().setValue(e.getAdjustable().getMaximum());
                }
            }
        );

        chatTextField.setDocument(new JTextFieldLimit(300));
        chatTextField.addActionListener((e) -> onSendChatExecute());
        sendButton.addActionListener((e) -> onSendChatExecute());
    }

    /* Server Events */

    /**
     * This method is called when a client successfully connects to a server and joins the lobby.
     */
    public void onConnected() {
        refreshPlayerTable();
        mvc.logClientEvent(String.format(
            "&gt; Connection successful! There are %d player(s) in the lobby.",
            getGameController().getGameLobbyModel().getLobbyPlayerNames().size()
        ));
        setChatEnabled(true);
    }

    /**
     * This method is called when a client disconnects from a server and leaves the lobby.
     */
    public void onDisconnected() {
        clearPlayerTable();
        setChatEnabled(false);
    }

    /* UI Manipulation */

    /**
     * Appends a new message or event to the log under a new paragraph. This can include any HTML tags which will be
     * formatted as such. Any user input such as chat messages should be HTML escaped to prevent unwanted formatting.
     *
     * @param messageToAdd Message to append to the log under a new paragraph.
     * @throws IllegalArgumentException Message cannot be empty.
     */
    public void appendEventToLog(@NotNull String messageToAdd) {
        if (messageToAdd.strip().length() == 0) throw new IllegalArgumentException("Message cannot be empty");

        try {
            logEditorHtml.insertBeforeEnd(logBodyElement, "<p>" + messageToAdd + "</p>");
        } catch (IOException | BadLocationException e) {
            e.printStackTrace();
        }

        if (!firstLineRemoved) {
            // Removes the first blank paragraph which exists by default when the JEditorPane is created.
            logEditorHtml.removeElement(logBodyElement.getElement(0));
            firstLineRemoved = true;
        }
    }

    /**
     * Refreshes the data displayed in the table. This method is called whenever there is an update to a player's
     * current status.
     */
    public void refreshPlayerTable() {
        clearPlayerTable();
        for (String playerName : getGameController().getGameLobbyModel().getLobbyPlayerNames())
            playerTableModel.addRow(new String[]{playerName, getGameController().getPlayerStatus(playerName)});
    }

    /**
     * Removes all data displayed in the table. This is done before refreshing the data, or upon disconnecting.
     */
    private void clearPlayerTable() {
        playerTableModel.setRowCount(0);
    }

    /**
     * Toggles the chat controls depending on the given state.
     *
     * @param enabled True, if chat controls should be enabled.
     */
    private void setChatEnabled(boolean enabled) {
        chatTextField.setEnabled(enabled);
        if (!enabled) chatTextField.setText("");
        sendButton.setEnabled(enabled);
    }

    /**
     * This method is called when the user attempts to send an outgoing message using the chat controls.
     */
    private void onSendChatExecute() {
        String chatToSend = chatTextField.getText();
        if (chatToSend.isEmpty()) return;

        //TODO: This needs to be controller code, not view code.
        String playerName = Objects.requireNonNull(mvc.getAppController().getGameController().getPlayerName());
        JavunoPacketInOutChatMessage chatPacket = new JavunoPacketInOutChatMessage(chatToSend, playerName);
        mvc.getAppController().getConnectionController().getClientConnection().writePacket(chatPacket);
        chatTextField.setText("");
    }

    /* Field Getters & Setters */

    /**
     * @return The panel representing this view, so that it can be attached to the main GUI.
     */
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
}
