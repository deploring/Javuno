package solar.rpg.javuno.models.game;

import org.jetbrains.annotations.NotNull;

public enum GameRule {
    CONSECUTIVE_DRAW_TWO("Consecutive Draw Twos"),
    DRAW_FOUR_CHALLENGES("Draw Four Challenges"),
    JUMP_INS("Jump Ins"),
    PICK_UP_PUT_DOWN("Pick Up Put Down"),
    MUST_FINISH_WITH_NUMBERED_CARD("");

    private String shortDesc;

    GameRule(@NotNull String shortDesc) {
        this.shortDesc = shortDesc;
    }

    public String getShortDesc() {
        return shortDesc;
    }
}
