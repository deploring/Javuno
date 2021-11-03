package solar.rpg.javuno.mvc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@code JMVC} is a representation of MVC. References to the specific {@link IView} and {@link IController} types are
 * stored in this MVC object, and can be accessed by either member. Since there is no reference to a Model, the
 * controller is expected to provide the view with adequate data access for the user interface.
 *
 * @param <V> The specific type of View.
 * @param <C> The specific type of Controller.
 * @author jskinner
 * @since 1.0.0
 */
public class JMVC<V extends IView, C extends IController> {

    /**
     * Reference to the view that is associated with the controller.
     */
    @Nullable
    private V view;
    /**
     * Reference to the controller that is associated with the view.
     */
    @Nullable
    private C controller;

    /**
     * @return True, if this {@code JMVC} object has been initialised, and both the view and controller are set.
     */
    private boolean isMVCSet() {
        return controller != null && view != null;
    }

    /**
     * @return The view that is associated with the controller.
     * @throws IllegalStateException MVC must be set.
     */
    @NotNull
    public V getView() {
        if (!isMVCSet()) throw new IllegalStateException("MVC has not been set");
        return view;
    }

    /**
     * @return The controller that is associated with the view.
     * @throws IllegalStateException MVC must be set.
     */
    @NotNull
    public C getController() {
        if (!isMVCSet()) throw new IllegalStateException("MVC has not been set");
        return controller;
    }

    /**
     * Sets the MVC references for this {@code JMVC} object.
     * The view and controller must be associated with one another.
     *
     * @param view       The view.
     * @param controller The controller.
     * @throws IllegalStateException MVC is already set.
     */
    public void set(@NotNull V view, @NotNull C controller) {
        if (isMVCSet()) throw new IllegalStateException("MVC can only be set once");
        this.view = view;
        this.controller = controller;
    }
}
