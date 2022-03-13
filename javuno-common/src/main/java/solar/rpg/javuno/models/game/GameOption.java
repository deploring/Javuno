package solar.rpg.javuno.models.game;

import org.jetbrains.annotations.NotNull;

/**
 * UNO is a complex game with different play styles among different sets of people. This enumerated type dictates the
 * various game options which can affect play style. Check the long description for more details on each game option.
 *
 * @author jskinner
 * @since 1.0.0
 */
public enum GameOption {
    CONSECUTIVE_DRAW_TWO(
        "Draw Two Multiplier",
        "A Draw Two card may be placed on top of another Draw Two card. This increases the amount of cards " +
            "that must be picked up by the current player once no further Draw Two cards can be played. This " +
            "multiplier resets once the penalty has been applied, even if another Draw Two card is placed on top of " +
            "the existing stack."
    ),
    DRAW_FOUR_CHALLENGES(
        "Draw Four Challenges",
        "A Draw Four card can only be legally played once it is the only playable card in a player's hand. It " +
            "can still be played at any time, however, the player receiving the Draw Four penalty may challenge the " +
            "usage of the card. If the card was not played legally, the challenge is successful and the original " +
            "player must instead pick up four cards from the discard pile. If the card was played legally, the " +
            "challenge fails and the penalty increases to six cards."
    ),
    JUMP_INS(
        "Jump Ins",
        "Any player may \"jump in\" and play their own card at any point if the color and symbol matches. " +
            "The game then continues from that player's position. This does not apply to wild cards. "
    ),
    PICK_UP_PUT_DOWN(
        "Pick Up Put Down",
        "If a player picks up a card from the draw pile because they have no playable cards, and the card " +
            "that was picked up is playable on the discard pile, then the player may immediately play that card."
    ),
    MUST_FINISH_WITH_NUMBERED_CARD(
        "Last Card Must Be Numbered",
        "In order to win the game, a player's final card must be numbered. Otherwise, the player cannot use " +
            "their final card and must pick up from the draw pile."
    );

    /**
     * Short description for UI purposes.
     */
    private final String shortDesc;
    /**
     * Long description where a more detailed explanation is needed for unfamiliar users.
     */
    private final String longDesc;

    GameOption(@NotNull String shortDesc, @NotNull String longDesc) {
        this.shortDesc = shortDesc;
        this.longDesc = longDesc;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public String getLongDesc() {
        return longDesc;
    }
}
