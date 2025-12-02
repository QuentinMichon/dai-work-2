package ch.heigvd.puissance4engine;

/**
 * Represents the possible results of a player's move attempt
 * in the Puissance 4 engine.
 */
public enum PlayStatus {

    /**
     * The move has been accepted and applied to the game board.
     */
    ACCEPTED,

    /**
     * The move is rejected because it is not the player's turn.
     */
    NOT_YOUR_TURN,

    /**
     * The move is rejected because the selected column is already full.
     */
    COLUMN_FULL,

    /**
     * The move is rejected because the selected column index is outside
     * the valid range.
     */
    OUT_OF_RANGE
}
