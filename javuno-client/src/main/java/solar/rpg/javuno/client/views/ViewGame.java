package solar.rpg.javuno.client.views;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.client.controller.ClientGameController;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.mvc.IView;
import solar.rpg.javuno.mvc.JMVC;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ViewGame implements IView {

    @NotNull
    private final JavunoClientMVC<ViewGame, ClientGameController> mvc;
    @NotNull
    private final JPanel rootPanel;
    private JPanel topRow;
    private JPanel middleRow;
    private JPanel bottomRow;
    private JPanel opponentHintsPanel;
    private JPanel lobbyButtonsPanel;
    private JButton readyButton;
    private JButton cancelButton;

    public ViewGame(@NotNull JavunoClientMVC<ViewGame, ClientGameController> mvc) {
        this.mvc = mvc;

        rootPanel = new JPanel(new GridLayout(3, 1));
        generateUI();
        showLobby();
    }

    private void onMarkSelfReady() {
        if (!readyButton.isEnabled() || cancelButton.isEnabled())
            throw new IllegalStateException("Buttons are not enabled correctly");
        mvc.getController().markSelfReady();
        SwingUtilities.invokeLater(() -> {
            mvc.logClientEvent("> You have marked yourself as ready to play.");
            setReadyButtons(true);
            mvc.getViewInformation().refreshPlayerTable();
        });
    }

    private void onUnmarkSelfReady() {
        if (readyButton.isEnabled() || !cancelButton.isEnabled())
            throw new IllegalStateException("Buttons are not enabled correctly");
        mvc.getController().unmarkSelfReady();
        SwingUtilities.invokeLater(() -> {
            mvc.logClientEvent("> You are no longer marked as ready to play.");
            setReadyButtons(false);
            mvc.getViewInformation().refreshPlayerTable();
        });
    }

    @Override
    public void generateUI() {
        topRow = new JPanel(new GridLayout(1, 3));
        topRow.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                "Opponents",
                TitledBorder.LEFT,
                TitledBorder.TOP));
        opponentHintsPanel = new JPanel();
        opponentHintsPanel.setLayout(new BoxLayout(opponentHintsPanel, BoxLayout.Y_AXIS));
        JLabel opponentHintsHeadingLabel = new JLabel("<html><h2>Opponents</h2></html>");
        JLabel opponentHintsLabel = new JLabel(
                "<html><p align='justify'><em>" +
                "Information about your opponents, such as number of cards, will appear here.</em>" +
                "</p></em></html>");
        opponentHintsPanel.add(opponentHintsHeadingLabel);
        opponentHintsPanel.add(opponentHintsLabel);

        middleRow = new JPanel(new GridLayout(1, 3));
        middleRow.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                "Play Area",
                TitledBorder.LEFT,
                TitledBorder.TOP));
        lobbyButtonsPanel = new JPanel();
        lobbyButtonsPanel.setLayout(new BoxLayout(lobbyButtonsPanel, BoxLayout.Y_AXIS));
        JPanel lobbyButtonsHintsPanel = new JPanel(new BorderLayout());
        JLabel lobbyButtonsHintsLabel = new JLabel(
                "<html><p align='justify'><em>" +
                "The game starts once the first 4 players in the lobby are marked as ready. If there " +
                "are more than 4 players in the lobby, those players will spectate the game." +
                "</p></em></html>");
        lobbyButtonsHintsPanel.add(lobbyButtonsHintsLabel, BorderLayout.NORTH);
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        readyButton = new JButton("Ready");
        readyButton.addActionListener((e) -> onMarkSelfReady());
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener((e) -> onUnmarkSelfReady());
        buttonsPanel.add(readyButton);
        buttonsPanel.add(cancelButton);
        lobbyButtonsPanel.add(lobbyButtonsHintsPanel);
        lobbyButtonsPanel.add(buttonsPanel);

        bottomRow = new JPanel();
        bottomRow.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                "Your Cards",
                TitledBorder.LEFT,
                TitledBorder.TOP));

        rootPanel.add(topRow);
        rootPanel.add(middleRow);
        rootPanel.add(bottomRow);
    }

    public void showLobby() {
        topRow.removeAll();
        topRow.add(opponentHintsPanel);
        topRow.add(new JPanel());
        topRow.add(new JPanel());

        middleRow.removeAll();
        middleRow.add(new JPanel());
        middleRow.add(lobbyButtonsPanel);
        setReadyButtons(false);
        middleRow.add(new JPanel());
    }

    private void setReadyButtons(boolean ready) {
        readyButton.setEnabled(!ready);
        cancelButton.setEnabled(ready);
    }

    @Override
    public JPanel getPanel() {
        return rootPanel;
    }

    @NotNull
    @Override
    public JMVC<ViewGame, ClientGameController> getMVC() {
        return mvc;
    }
}
