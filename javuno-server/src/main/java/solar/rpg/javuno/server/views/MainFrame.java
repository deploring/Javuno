package solar.rpg.javuno.server.views;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.mvc.IView;
import solar.rpg.javuno.mvc.JMVC;
import solar.rpg.javuno.server.controllers.HostController;
import solar.rpg.javuno.server.controllers.ServerAppController;
import solar.rpg.javuno.server.controllers.ServerGameController;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

/**
 * This {@code JFrame} acts as the main user interface where GUI is available. If GUI is not available, the frame will
 * throw a {@link HeadlessException} and the server main will default to command line interface (CLI) for user input.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class MainFrame extends JFrame implements IView {

    @NotNull
    private final Logger logger;
    @NotNull
    private final JPanel mainPanel;
    @NotNull
    private JTextField serverIpTextField;
    private JTextField serverPortTextField;
    private JTextField serverPasswordTextField;
    private JButton startButton;
    private JButton stopButton;
    @NotNull
    private final JMVC<MainFrame, ServerAppController> mvc;

    public MainFrame(@NotNull Logger logger) {
        super("Javuno Server 1.0.0");
        this.logger = logger;

        ServerAppController appController = new ServerAppController(logger);
        mvc = appController.getMVC();
        mvc.set(this, appController);

        HostController serverHostController = appController.getHostController();
        serverHostController.getMVC().set(this, serverHostController);

        ServerGameController serverGameController = appController.getGameController();
        serverGameController.getMVC().set(this, serverGameController);

        mainPanel = new JPanel();

        generateUI();
    }

    private void onStartServerExecute() {
        if (!startButton.isEnabled() || stopButton.isEnabled())
            throw new IllegalStateException("Buttons are not enabled correctly");

        String serverIp = serverIpTextField.getText();
        String serverPort = serverPortTextField.getText();
        String serverPassword = serverPasswordTextField.getText();

        mvc.getController().getHostController().setServerPassword(serverPassword);
        try {
            mvc.getController().getHostController().startHost(InetAddress.getByName(serverIp),
                                                              Integer.parseInt(serverPort));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void onStopServerExecute() {
        if (startButton.isEnabled() || !stopButton.isEnabled())
            throw new IllegalStateException("Buttons are not enabled correctly");

        mvc.getController().getHostController().stopHost();
    }

    private void generateUI() {
        mainPanel.setLayout(new GridLayout(2, 2));

        JPanel serverHostPanel = new JPanel();
        serverHostPanel.setLayout(new BoxLayout(serverHostPanel, BoxLayout.Y_AXIS));

        JPanel serverIpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel serverIpLabel = new JLabel("Server IP:");
        serverIpTextField = new JTextField(8);
        serverIpTextField.setDocument(new JTextFieldLimit(100));
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
        serverPasswordTextField = new JTextField(4);
        serverPasswordTextField.setDocument(new JTextFieldLimit(10));
        serverPasswordLabel.setLabelFor(serverPasswordTextField);
        serverPasswordPanel.add(serverPasswordLabel);
        serverPasswordPanel.add(serverPasswordTextField);

        JPanel serverButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        startButton = new JButton("Start");
        startButton.addActionListener((e) -> onStartServerExecute());
        serverIpTextField.addActionListener((e) -> onStartServerExecute());
        serverPortTextField.addActionListener((e) -> onStartServerExecute());
        serverPasswordTextField.addActionListener((e) -> onStartServerExecute());
        stopButton = new JButton("Cancel");
        stopButton.setEnabled(false);
        stopButton.addActionListener((e) -> onStopServerExecute());
        serverButtonsPanel.add(startButton);
        serverButtonsPanel.add(stopButton);

        serverHostPanel.add(serverIpPanel);
        serverHostPanel.add(serverPortPanel);
        serverHostPanel.add(serverPasswordPanel);
        serverHostPanel.add(serverButtonsPanel);

        mainPanel.add(serverHostPanel);

        add(mainPanel);
    }

    @Override
    public JPanel getPanel() {
        return null;
    }

    @NotNull
    @Override
    public JMVC<MainFrame, ServerAppController> getMVC() {
        return mvc;
    }
}
