package solar.rpg.javuno.client.views;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.mvc.IController;
import solar.rpg.javuno.mvc.IView;
import solar.rpg.javuno.mvc.JMVC;

import java.util.function.Consumer;

public class JavunoClientMVC<V extends IView, C extends IController> extends JMVC<V, C> {

    @Nullable
    private Consumer<String> logMessageConsumer;

    public void writeClientEvent(String log) {
        assert logMessageConsumer != null : "Expected logger function to be set";
        logMessageConsumer.accept(log);
    }

    public void set(@NotNull V view, @NotNull C controller, @Nullable Consumer<String> logMessageConsumer) {
        this.logMessageConsumer = logMessageConsumer;
        set(view, controller);
    }
}
