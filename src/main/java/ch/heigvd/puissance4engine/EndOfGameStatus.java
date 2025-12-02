package ch.heigvd.puissance4engine;

/**
 * Represents the different possible outcomes of a Puissance 4 game.
 * <p>
 * This enumeration is used by both the server and the clients to indicate
 * how a game has ended.
 */
public enum EndOfGameStatus {

    /**
     * The player has won the game.
     */
    WIN,

    /**
     * The player has lost the game.
     */
    LOOSE,

    /**
     * The game ended with no winner.
     */
    DRAW,

    /**
     * The opponent disconnected.
     */
    FORFEIT,
}
