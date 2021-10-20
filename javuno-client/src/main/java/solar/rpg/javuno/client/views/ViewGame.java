package solar.rpg.javuno.client.views;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.client.controller.ClientGameController;
import solar.rpg.javuno.mvc.IView;
import solar.rpg.javuno.mvc.JMVC;

import javax.swing.*;

public class ViewGame implements IView {

    @NotNull
    private final JMVC<ViewGame, ClientGameController> mvc;
    @NotNull
    private final JPanel rootPanel;

    public ViewGame(@NotNull JMVC<ViewGame, ClientGameController> mvc) {
        this.mvc = mvc;

        rootPanel = new JPanel();
        generateUI();
    }

    @Override
    public void generateUI() {

    }

    @Override
    public JPanel getPanel() {
        return rootPanel;
    }

    @NotNull
    @Override
    public JMVC<ViewGame, ClientGameController> getMVC() {
        return mvc;
    }
}
