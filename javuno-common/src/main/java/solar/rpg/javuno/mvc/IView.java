package solar.rpg.javuno.mvc;

import javax.swing.*;

public interface IView {

    void generateUI();

    void reset();

    JPanel getPanel();
}
