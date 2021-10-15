package solar.rpg.javuno.client.models;

import solar.rpg.javuno.models.IPlayer;

public class Opponent implements IPlayer {

    private String name;
    private int handAmount;
    private PlayerState state;

    public Opponent(IPlayer source) {
        this.name = source.getName();
        this.handAmount = source.getHandAmount();
        this.state = source.getState();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getHandAmount() {
        return handAmount;
    }

    @Override
    public PlayerState getState() {
        return state;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHandAmount(int handAmount) {
        this.handAmount = handAmount;
    }

    public void setState(PlayerState state) {
        this.state = state;
    }
}
