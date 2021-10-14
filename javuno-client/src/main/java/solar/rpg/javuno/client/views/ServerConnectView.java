package solar.rpg.javuno.client.views;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.client.controller.JavunoClientConnectionController;
import solar.rpg.javuno.mvc.IController;
import solar.rpg.javuno.mvc.IView;
import solar.rpg.javuno.mvc.JMVC;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.text.NumberFormat;

public class ServerConnectView implements IView {

    @NotNull
    private final JMVC<ServerConnectView, JavunoClientConnectionController> mvc;
    @NotNull
    private final JPanel rootPanel;
    private JTextField usernameTextField;
    private JTextField serverPasswordTextField;
    private JTextField serverIpTextField;
    private JTextField serverPortTextField;
    private JButton connectButton;

    public ServerConnectView(@NotNull JMVC<ServerConnectView, JavunoClientConnectionController> mvc) {
        this.mvc = mvc;
        rootPanel = new JPanel();
        generateUI();
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
        serverPasswordTextField = new JTextField(10);
        serverPasswordLabel.setLabelFor(serverPasswordTextField);
        serverPasswordPanel.add(serverPasswordLabel);
        serverPasswordPanel.add(serverPasswordTextField);

        JPanel connectPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        connectButton = new JButton("Connect");
        connectButton.addActionListener((e) -> onConnectClick());
        connectPanel.add(connectButton);

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

    private void onConnectClick() {
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

        String username = usernameTextField.getText();
        if (username.length() == 0)
            errorMessage += "Please enter a username";

        String serverPassword = serverPasswordTextField.getText();

        if (!errorMessage.equals("")) {
            showErrorDialog("Validation Error", errorMessage);
            return;
        }

        final int finalServerPort = serverPort;

        SwingUtilities.invokeLater(() -> {
            connectButton.setEnabled(false);
            connectButton.setText("Connecting");
            getMVC().getController().tryConnect(serverIp, finalServerPort, username, serverPassword);
        });
    }

    @Override
    public void reset() {
        connectButton.setEnabled(true);
        connectButton.setText("Connect");
    }

    @Override
    public JPanel getPanel() {
        return rootPanel;
    }

    @NotNull
    @Override
    public JMVC<ServerConnectView, JavunoClientConnectionController> getMVC() {
        return mvc;
    }
}
