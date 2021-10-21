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

        SwingUtilities.invokeLater(() -> {
            Logger logger = LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME);
            ConsoleHandler handler = new ConsoleHandler();
            handler.setLevel(Level.FINER);
            logger.addHandler(handler);
            logger.setLevel(Level.FINER);

            JFrame frame = new MainFrame(logger);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            frame.setMinimumSize(new Dimension(900, 700));
            frame.setPreferredSize(new Dimension(900, 700));
            frame.pack();
        });
    }
}
