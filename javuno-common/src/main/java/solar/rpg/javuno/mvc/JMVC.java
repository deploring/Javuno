package solar.rpg.javuno.mvc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
