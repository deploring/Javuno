package solar.rpg.javuno.client.views;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.client.controller.ConnectionController;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.mvc.IView;

import javax.swing.*;
import java.awt.*;

public class ViewServerConnect implements IView {

    @NotNull
    private final JavunoClientMVC<ViewServerConnect, ConnectionController> mvc;
    @NotNull
    private final JPanel rootPanel;
    private JTextField usernameTextField;
    private JPasswordField serverPasswordTextField;
    private JTextField serverIpTextField;
    private JTextField serverPortTextField;
    private JButton connectButton;
    private JButton cancelButton;

    public ViewServerConnect(@NotNull JavunoClientMVC<ViewServerConnect, ConnectionController> mvc) {
        this.mvc = mvc;
        rootPanel = new JPanel();
        generateUI();
    }

    public void onConnectionFailed(@NotNull String message) {
        mvc.logClientEvent(String.format("> Connection failed: %s", message));
        setFormEntryEnabled(true);
        showErrorDialog(
                "Could not establish connection",
                String.format("Could not establish connection to server: %s", message));

    }

    public void onDisconnected(boolean notify) {
        if (notify) mvc.logClientEvent("> You have been disconnected from the server.");
        setFormEntryEnabled(true);
    }

    private void setFormEntryEnabled(boolean enabled) {
        usernameTextField.setEnabled(enabled);
        serverIpTextField.setEnabled(enabled);
        serverPortTextField.setEnabled(enabled);
        serverPasswordTextField.setEnabled(enabled);
        connectButton.setEnabled(enabled);
        connectButton.setText(enabled ? "Connect" : "Connecting");
        cancelButton.setEnabled(!enabled);
    }

    private void onConnectExecute() {
        if (!connectButton.isEnabled() || cancelButton.isEnabled())
            throw new IllegalStateException("Buttons are not enabled correctly");

        String errorMessage = "";

        String serverIp = serverIpTextField.getText();
        if (serverIpTextField.getText().length() == 0)
            errorMessage += "Please enter a server IP\n";

        String serverPortText = serverPortTextField.getText();
        int serverPort = -1;
        if (serverPortText.length() == 0)
            errorMessage += "Please enter a server port\n";
        else {
            try {
                serverPort = Integer.parseInt(serverPortText);
                if (serverPort < 1 || serverPort > 65535) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                errorMessage += "Please enter a valid server port (1-65535)\n";
            }
        }
        final int finalServerPort = serverPort;

        String username = usernameTextField.getText();
        if (username.length() == 0)
            errorMessage += "Please enter a username";

        String serverPassword = String.valueOf(serverPasswordTextField.getPassword());

        if (!errorMessage.equals("")) {
            showErrorDialog("Validation Error", errorMessage);
            return;
        }

        mvc.getAppController().getGameController().setPlayerName(username);
        mvc.getController().tryConnect(serverIp, finalServerPort, username, serverPassword);

        SwingUtilities.invokeLater(() -> {
            setFormEntryEnabled(false);
            mvc.logClientEvent(String.format("> Attempting to connect to server at %s:%s", serverIp, finalServerPort));
        });
    }

    private void onCancelExecute() {
        if (connectButton.isEnabled() || !cancelButton.isEnabled())
            throw new IllegalStateException("Buttons are not enabled correctly");

        mvc.getController().cancelPendingConnect();

        SwingUtilities.invokeLater(() -> {
            setFormEntryEnabled(true);
            mvc.logClientEvent("> Connection cancelled!");
        });
    }

    @Override
    public void generateUI() {
        JPanel loginDetailsPanel = new JPanel();
        loginDetailsPanel.setLayout(new BoxLayout(loginDetailsPanel, BoxLayout.Y_AXIS));
        loginDetailsPanel.setMaximumSize(new Dimension(300, 150));
        loginDetailsPanel.setPreferredSize(new Dimension(300, 150));

        JPanel usernamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel usernameLabel = new JLabel("Username:");
        usernameTextField = new JTextField(8);
        usernameTextField.setDocument(new JTextFieldLimit(10));
        usernameLabel.setLabelFor(usernameTextField);
        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameTextField);

        JPanel serverIpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel serverIpLabel = new JLabel("Server IP Address:");
        serverIpTextField = new JTextField(12);
        serverIpTextField.setDocument(new JTextFieldLimit(15));
        serverIpLabel.setLabelFor(serverIpTextField);
        serverIpPanel.add(serverIpLabel);
        serverIpPanel.add(serverIpTextField);

        JPanel serverPortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel serverPortLabel = new JLabel("Server Port:");
        serverPortTextField = new JTextField(4);
        serverPortTextField.setDocument(new JTextFieldLimit(5));
        serverPortLabel.setLabelFor(serverPortTextField);
        serverPortPanel.add(serverPortLabel);
        serverPortPanel.add(serverPortTextField);

        JPanel serverPasswordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel serverPasswordLabel = new JLabel("Server Password:");
        serverPasswordTextField = new JPasswordField(10);
        serverPasswordLabel.setLabelFor(serverPasswordTextField);
        serverPasswordPanel.add(serverPasswordLabel);
        serverPasswordPanel.add(serverPasswordTextField);

        JPanel connectPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        connectButton = new JButton("Connect");
        connectButton.addActionListener((e) -> onConnectExecute());
        usernameTextField.addActionListener((e) -> onConnectExecute());
        serverIpTextField.addActionListener((e) -> onConnectExecute());
        serverPortTextField.addActionListener((e) -> onConnectExecute());
        serverPasswordTextField.addActionListener((e) -> onConnectExecute());
        cancelButton = new JButton("Cancel");
        cancelButton.setEnabled(false);
        cancelButton.addActionListener((e) -> onCancelExecute());
        connectPanel.add(connectButton);
        connectPanel.add(cancelButton);

        loginDetailsPanel.add(usernamePanel);
        loginDetailsPanel.add(serverIpPanel);
        loginDetailsPanel.add(serverPortPanel);
        loginDetailsPanel.add(serverPasswordPanel);
        loginDetailsPanel.add(connectPanel);

        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
        rootPanel.add(Box.createVerticalGlue());
        rootPanel.add(loginDetailsPanel);
        rootPanel.add(Box.createVerticalGlue());
        rootPanel.setBackground(Color.getColor("#ffdead"));
    }

    @Override
    public JPanel getPanel() {
        return rootPanel;
    }

    @NotNull
    @Override
    public JavunoClientMVC<ViewServerConnect, ConnectionController> getMVC() {
        return mvc;
    }
}
