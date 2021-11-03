package solar.rpg.javuno.mvc;

/**
 * Marker interface that represents an MVC controller. As controllers, these classes act as the containers
 * and manipulators of their model objects. They have knowledge of their view in a {@link JMVC} relationship
 * so that model changes can be pushed to the UI.
 *
 * @author jskinner
 * @see JMVC
 * @since 1.0.0
 */
public interface IController {

    /**
     * @return The MVC relationship for this controller.
     * @see JMVC
     */
    JMVC<?, ?> getMVC();
}
