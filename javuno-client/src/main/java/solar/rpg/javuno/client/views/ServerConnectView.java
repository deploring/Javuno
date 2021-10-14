package solar.rpg.javuno.client.views;

import solar.rpg.javuno.mvc.IView;

import javax.swing.*;
import java.awt.*;

public class ServerConnectView implements IView {

    private JPanel rootPanel;
    private JTextField serverIpTextField;

    public ServerConnectView() {
        generateUI();
    }

    @Override
    public void generateUI() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
        rootPanel.add(Box.createVerticalGlue());

        JPanel serverDetailsPanel = new JPanel(new FlowLayout());
        JLabel serverIpLabel = new JLabel("Server IP Address:");
        JTextField serverIpTextField = new JTextField(12);
        serverIpLabel.setLabelFor(serverIpTextField);
        JLabel serverPortLabel = new JLabel("Server Port:");
        JTextField serverPortTextField = new JTextField(4);
        serverPortLabel.setLabelFor(serverPortTextField);
        JButton connectButton = new JButton("Connect");

        serverDetailsPanel.add(serverIpLabel);
        serverDetailsPanel.add(serverIpTextField);
        serverDetailsPanel.add(serverPortLabel);
        serverDetailsPanel.add(serverPortTextField);
        serverDetailsPanel.add(connectButton);
        rootPanel.add(serverDetailsPanel);
        rootPanel.add(Box.createVerticalGlue());
    }

    @Override
    public void reset() {

    }

    @Override
    public JPanel getPanel() {
        return rootPanel;
    }
}
