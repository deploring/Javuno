package solar.rpg.javuno.client.views;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.client.controller.ClientAppController;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.mvc.IView;

import javax.swing.*;
import java.awt.*;

public class ViewMain implements IView {

    private JPanel rootPanel;
    private JPanel mainPanel;
    private JPanel informationPanel;

    public ViewMain() {

    }

    /* Server Events */

    public void onConnected() {
        viewInformation.onConnected();
        if (mvc.getController().getGameController().getGameLobbyModel().isInGame()) {
            viewGame.onJoinGame();
            showView(viewGame);
        } else {
            viewLobby.onShowLobby();
            showView(viewLobby);
        }

        menuItemDisconnect.setEnabled(true);
    }

    public void onDisconnected(boolean notify) {
        viewInformation.onDisconnected();
        viewServerConnect.onDisconnected(notify);
        viewGame.getMVC().getController().onDisconnected();
        showView(viewServerConnect);

        menuItemDisconnect.setEnabled(false);
    }

    /* UI Manipulation */

    private void showView(IView view) {
        mainPanel.removeAll();
        mainPanel.add(view.getPanel(), BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void onDisconnectExecute() {
        viewServerConnect.getMVC().getController().close();
    }

    private void generateUI() {
        JMenuBar menuBar = new JMenuBar();
        JMenu actionMenu = new JMenu("Action");
        menuItemDisconnect = new JMenuItem("Disconnect");
        menuItemDisconnect.addActionListener((e) -> onDisconnectExecute());
        actionMenu.add(menuItemDisconnect);

        JMenu helpMenu = new JMenu("Help");
        menuItemAbout = new JMenuItem("About");
        helpMenu.add(menuItemAbout);

        menuBar.add(actionMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        mainPanel.setLayout(new BorderLayout());
        mainPanel.setMinimumSize(new Dimension(600, 700));
        mainPanel.setPreferredSize(mainPanel.getMinimumSize());

        getContentPane().setLayout(new BorderLayout());
        JSplitPane contentSplitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            viewInformation.getPanel(),
            mainPanel);
        contentSplitPane.setDividerLocation((int) viewInformation.getPanel().getMinimumSize().getWidth());
        getContentPane().add(contentSplitPane, BorderLayout.CENTER);
    }

    /* MVC */

    @NotNull
    public ViewInformation getViewInformation() {
        return viewInformation;
    }

    @NotNull
    @Override
    public JPanel getPanel() {
        return mainPanel;
    }

    @NotNull
    @Override
    public JavunoClientMVC<MainFrame, ClientAppController> getMVC() {
        return mvc;
    }
}
