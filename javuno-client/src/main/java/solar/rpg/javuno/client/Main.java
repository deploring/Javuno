package solar.rpg.javuno.client;

import solar.rpg.javuno.client.views.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
                UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        Logger logger = LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINER);
        logger.addHandler(handler);
        logger.setLevel(Level.FINER);

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(logger);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            frame.setMinimumSize(new Dimension(900, 800));
            frame.pack();

            frame.getMVC().logClientEvent(
                "Hello, welcome to <strong>Javuno</strong>. To get started, please enter the connection details of a " +
                    "server. You can also host this server yourself. Check the README for more information."
            );
            frame.onDisconnected(false);
        });
    }
}
