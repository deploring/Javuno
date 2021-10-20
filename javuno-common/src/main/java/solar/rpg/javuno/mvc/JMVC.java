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
 */
public class JMVC<V extends IView, C extends IController> {

    @Nullable
    private V view;
    @Nullable
    private C controller;

    private boolean isMVCSet() {
        return controller != null && view != null;
    }

    @NotNull
    public V getView() {
        assert isMVCSet() : "MVC has not been set";
        return view;
    }


    @NotNull
    public C getController() {
        assert isMVCSet() : "MVC has not been set";
        return controller;
    }

    public void set(@NotNull V view, @NotNull C controller) {
        assert !isMVCSet() : "MVC can only be set once";
        this.view = view;
        this.controller = controller;
    }
}
