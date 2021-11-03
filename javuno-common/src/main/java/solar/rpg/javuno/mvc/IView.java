package solar.rpg.javuno.mvc;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * Marker interface that represents an MVC view. As views, these classes act as the interface to the user,
 * and translate their inputs into controller calls via events. They are responsible for displaying updated
 * data from the models back to the user. They also have knowledge of their controller (and its models) in
 * a {@link JMVC} relationship to make the above possible.
 *
 * @author jskinner
 * @see JMVC
 * @since 1.0.0
 */
public interface IView {

    /**
     * @return The MVC relationship for this view.
     * @see JMVC
     */
    @NotNull
    JMVC<?, ?> getMVC();

    /**
     * Shows an error dialog in a popup Swing pane.
     *
     * @param title The popup title.
     * @param message The popup message.
     */
    default void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Simple Swing {@link Document} that limits the amount of characters allowed in a text input.
     */
    class JTextFieldLimit extends PlainDocument {

        private final int limit;

        public JTextFieldLimit(int limit) {
            super();
            this.limit = limit;
        }

        @Override
        public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
            if (str == null)
                return;

            if ((getLength() + str.length()) <= limit) {
                super.insertString(offset, str, attr);
            }
        }
    }
}
