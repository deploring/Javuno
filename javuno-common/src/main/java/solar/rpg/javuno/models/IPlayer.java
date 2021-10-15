package solar.rpg.javuno.models;

public interface IPlayer {

    String getName();

    int getHandAmount();

    PlayerState getState();

    enum PlayerState {
        IN_LOBBY
    }
}
