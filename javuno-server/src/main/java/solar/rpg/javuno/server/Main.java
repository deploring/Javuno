package solar.rpg.javuno.server;

import solar.rpg.javuno.server.views.MainView;

import javax.swing.*;
import java.awt.*;
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

            JFrame frame = new MainView(logger);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            frame.setMinimumSize(new Dimension(128, 128));
            frame.setPreferredSize(new Dimension(256, 256));
            frame.pack();
        });
    }
}
