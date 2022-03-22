package solar.rpg.javuno.client.views;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.client.controller.ConnectionController;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.mvc.IView;

import javax.swing.*;

/**
 * This view acts as a form that allows the user to enter connection details of the JAVUNO server they wish to join.
 *
 * @author jskinner
 * @since 1.0
 */
public class ViewServerConnect implements IView {

    @NotNull
    private final JavunoClientMVC<ViewServerConnect, ConnectionController> mvc;

    private JPanel rootPanel;
    private JTextField usernameTextField;
    private JTextField serverIpTextField;
    private JTextField serverPortTextField;
    private JPasswordField serverPasswordTextField;
    private JButton connectButton;
    private JButton cancelButton;

    /**
     * Constructs a new {@code ViewServerConnect} instance.
     *
     * @param mvc MVC relationship between this view and the {@link ConnectionController}.
     */
    public ViewServerConnect(@NotNull JavunoClientMVC<ViewServerConnect, ConnectionController> mvc) {
        this.mvc = mvc;

        //TODO: Config file

        usernameTextField.setDocument(new JTextFieldLimit(10));
        serverIpTextField.setDocument(new JTextFieldLimit(100));
        serverPortTextField.setDocument(new JTextFieldLimit(5));

        connectButton.addActionListener((e) -> onConnectExecute());
        usernameTextField.addActionListener((e) -> onConnectExecute());
        serverIpTextField.addActionListener((e) -> onConnectExecute());
        serverPortTextField.addActionListener((e) -> onConnectExecute());
        serverPasswordTextField.addActionListener((e) -> onConnectExecute());
        cancelButton.addActionListener((e) -> onCancelExecute());
    }

    /* Server Connect Events */

    public void onConnectionFailed(@NotNull String message) {
        mvc.logClientEvent(String.format("&gt; Connection failed: %s", message));
        setFormEnabled(true);
        showErrorDialog(
            "Could not establish connection",
            String.format("Could not establish connection to server: %s", message)
        );

    }

    public void onDisconnected(boolean notify) {
        if (notify) mvc.logClientEvent("&gt; You have been disconnected from the server.");
        setFormEnabled(true);
    }

    /* UI Manipulation */

    private void setFormEnabled(boolean enabled) {
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

        mvc.getController().tryConnect(serverIp, finalServerPort, username, serverPassword);

        SwingUtilities.invokeLater(() -> {
            setFormEnabled(false);
            mvc.logClientEvent(String.format(
                "&gt; Attempting to connect to server at %s:%s",
                serverIp,
                finalServerPort
            ));
        });
    }

    private void onCancelExecute() {
        if (connectButton.isEnabled() || !cancelButton.isEnabled())
            throw new IllegalStateException("Buttons are not enabled correctly");

        mvc.getController().cancelPendingConnect();

        SwingUtilities.invokeLater(() -> {
            setFormEnabled(true);
            mvc.logClientEvent("&gt; Connection cancelled!");
        });
    }

    /* Field Getters & Setters */

    @NotNull
    public JPanel getPanel() {
        return rootPanel;
    }

    @NotNull
    @Override
    public JavunoClientMVC<ViewServerConnect, ConnectionController> getMVC() {
        return mvc;
    }
}
