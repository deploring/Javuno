package solar.rpg.javuno.models.game;

/**
 * Signals that an operation on the JAVUNO game model has been unsuccessful due to invalid or unexpected state.
 *
 * @author jskinner
 * @since 1.0.0
 */
public class JavunoStateException extends IllegalStateException {

    /**
     * Constructs a new {@code JavunoStateException} instance.
     *
     * @param message A message stating why the exception was thrown.
     */
    public JavunoStateException(String message) {
        super(message);
    }
}
