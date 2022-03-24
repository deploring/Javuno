package solar.rpg.javuno.client.views;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

/**
 * {@code MainFrame} acts as the primary view of the application, which is responsible for creating, maintaining, and
 * linking all controllers & sub-views. Using a {@link JSplitPane}, the {@link ViewInformation} sub-view is displayed on
 * the left pane; various other sub-views are displayed on the right pane.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class MainFrame extends JFrame {

    /**
     * Constructs a new {@code MainFrame} instance.
     *
     * @param logger Logger object.
     */
    public MainFrame(@NotNull Logger logger) {
        super("Javuno Client 1.0.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setMinimumSize(new Dimension(900, 800));

        ViewMain viewMain = new ViewMain(logger);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(viewMain.getPanel(), BorderLayout.CENTER);
        setJMenuBar(viewMain.getMenuBar());

        pack();
    }
}
