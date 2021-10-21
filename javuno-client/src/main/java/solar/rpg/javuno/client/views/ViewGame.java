package solar.rpg.javuno.client.views;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.client.controller.ClientGameController;
import solar.rpg.javuno.mvc.IView;
import solar.rpg.javuno.mvc.JMVC;

import javax.swing.*;
import java.awt.*;

public class ViewGame implements IView {

    @NotNull
    private final JMVC<ViewGame, ClientGameController> mvc;
    @NotNull
    private final JPanel rootPanel;
    private JPanel topRow;
    private JPanel middleRow;
    private JPanel bottomRow;
    private JPanel opponentHintsPanel;
    private JPanel lobbyButtonsPanel;

    public ViewGame(@NotNull JMVC<ViewGame, ClientGameController> mvc) {
        this.mvc = mvc;

        rootPanel = new JPanel(new GridLayout(3, 1));
        generateUI();
        showLobby();
    }

    @Override
    public void generateUI() {
        topRow = new JPanel(new GridLayout(1, 3));
        opponentHintsPanel = new JPanel();
        opponentHintsPanel.setLayout(new BoxLayout(opponentHintsPanel, BoxLayout.Y_AXIS));
        JLabel opponentHintsHeadingLabel = new JLabel("<html><h2>Opponents</h2></html>");
        JLabel opponentHintsLabel = new JLabel(
                "<html>" +
                "<em>Information about your opponents, such as number of cards, will appear here.</em>" +
                "</html>");
        opponentHintsPanel.add(opponentHintsHeadingLabel);
        opponentHintsPanel.add(opponentHintsLabel);

        middleRow = new JPanel(new GridLayout(1, 3));
        lobbyButtonsPanel = new JPanel();
        lobbyButtonsPanel.setLayout(new BoxLayout(lobbyButtonsPanel, BoxLayout.Y_AXIS));
        JLabel lobbyButtonsHintsLabel = new JLabel(
                "<html>The game starts once the first 4 players in the lobby are marked as ready. If there are " +
                "more than 4 players in the lobby, those players will spectate the game.<hr/></html>");
        JButton readyButton = new JButton("Ready");
        JButton cancelButton = new JButton("Cancel");
        lobbyButtonsPanel.add(lobbyButtonsHintsLabel);
        lobbyButtonsPanel.add(readyButton);
        lobbyButtonsPanel.add(cancelButton);

        bottomRow = new JPanel();

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
        middleRow.add(new JPanel());
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
