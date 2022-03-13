package solar.rpg.javuno.models.game;

/**
 * Denotes the possible directions of play in an UNO game.
 * The real game proceeds clockwise, then counterclockwise when the direction is reversed.
 *
 * @author jskinner
 * @since 1.0.0
 */
public enum Direction {
    FORWARD,
    BACKWARD;

    public Direction getReverse() {
        return this == FORWARD ? BACKWARD : FORWARD;
    }
}
