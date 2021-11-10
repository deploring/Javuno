package solar.rpg.javuno.models.game;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractGamePlayer {

    @NotNull
    private final String name;
    private boolean uno;

    public AbstractGamePlayer(@NotNull String name, boolean uno) {
        this.name = name;
        this.uno = uno;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public abstract int getCardCount();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractGamePlayer that = (AbstractGamePlayer) o;
        return name.equals(that.name);
    }

    public boolean isUno() {
        return uno;
    }

    public void setUno(boolean uno) {
        this.uno = uno;
    }
}
