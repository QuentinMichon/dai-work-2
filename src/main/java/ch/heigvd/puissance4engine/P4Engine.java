package ch.heigvd.puissance4engine;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Core engine for the Puissance 4 (Connect Four) game.
 * <p>
 * This class manages players, the game board, turn order, move validation,
 * and win/draw/forfeit detection. It is designed to be used by a server
 * handling remote players.
 */
public class P4Engine {

    static private final int MAX_COL = 7;
    static private final int MAX_ROW = 6;

    private final Pile[] table = new Pile[MAX_COL];
    private final int[] offset = {0, 1, 2, 3};

    private AtomicInteger nbPlayer;
    private final char[] symbolUsers = {'X', 'O'};
    private int playerTurn = 0;

    private Player[] players = new Player[2];

    /**
     * Creates a new game engine instance and initializes the game board.
     */
    public P4Engine() {
        for (int i = 0; i < MAX_COL; i++) {
            table[i] = new Pile();
        }
        nbPlayer = new AtomicInteger(0);
    }

    /**
     * Registers a new player in the engine, assigning an ID and symbol.
     *
     * @return a new Player object if a slot is available, otherwise null
     */
    public Player newPlayer() {
        if(nbPlayer.get() < 2) {
            int id = nbPlayer.getAndIncrement();
            players[id] = new Player(symbolUsers[id], id);
            return players[id];
        } else {
            return null;
        }
    }

    /**
     * Returns the number of currently registered players.
     *
     * @return number of connected players (0 to 2)
     */
    public synchronized int getNbPlayer() {
        return nbPlayer.get();
    }

    /**
     * Retrieves the opponent's name for a given player.
     *
     * @param player the player whose opponent name is requested
     * @return the opponent's name, or an empty string if none is available
     */
    private synchronized String getOponentName(Player player) {
        if(player.id == 0) {
            if(nbPlayer.get() == 1) {
                return "";
            } else {
                return players[1].getName();
            }
        } else if(player.id == 1) {
            return players[0].getName();
        } else {
            return "";
        }
    }

    /**
     * Checks if the given symbol has won the game, if the game is drawn,
     * or if a player has forfeited.
     *
     * @param symbol the symbol of the player performing the check
     * @return the status of the game: WIN, LOOSE, DRAW, or FORFEIT
     */
    public synchronized EndOfGameStatus checkWin(char symbol) {
        if(nbPlayer.get() == 1) {
            return EndOfGameStatus.FORFEIT;
        }

        boolean tableFull = true;
        for(int i=0; i<MAX_COL; i++) {
            if(!table[i].isFull()) {
                tableFull = false;
                break;
            }
        }
        if(tableFull) {
            return EndOfGameStatus.DRAW;
        }

        int suite = 0;

        // check |
        for (int c = 0; c < MAX_COL; c++) {
            for (int r = 0; r < 3; r++) {

                suite = 0;

                for(int i = 0; i < 4; i++) {
                    if(table[c].pile[r + offset[i]] == symbol) {
                        suite++;
                    } else {
                        break;
                    }
                }

                if(suite == 4) { return EndOfGameStatus.WIN; }
            }
        }

        // check _
        for (int c = 0; c < 4; c++) {
            for (int r = 0; r < MAX_ROW; r++) {

                suite = 0;

                for(int i = 0; i < 4; i++) {
                    if(table[c + offset[i]].pile[r] == symbol) {
                        suite++;
                    } else {
                        break;
                    }
                }

                if(suite == 4) { return EndOfGameStatus.WIN; }
            }
        }

        // check \
        for (int c = 0; c < 4; c++) {
            for (int r = 3; r < MAX_ROW; r++) {

                suite = 0;

                for(int i = 0; i < 4; i++) {
                    if(table[c + offset[i]].pile[r - offset[i]] == symbol) {
                        suite++;
                    } else {
                        break;
                    }
                }

                if(suite == 4) { return EndOfGameStatus.WIN; }
            }
        }

        // check /
        for (int c = 3; c < MAX_COL; c++) {
            for (int r = 3; r < MAX_ROW; r++) {
                suite = 0;

                for(int i = 0; i < 4; i++) {
                    if(table[c - offset[i]].pile[r - offset[i]] == symbol) {
                        suite++;
                    } else {
                        break;
                    }
                }

                if(suite == 4) { return EndOfGameStatus.WIN; }
            }
        }

        return EndOfGameStatus.LOOSE;
    }

    /**
     * Converts the entire board into a single string (column-major order).
     *
     * @return string representation of the board
     */
    @Override
    public String toString() {
        StringBuilder tableString = new StringBuilder();
        for (int i = 0; i < MAX_COL; i++) {
            for (int j = 0; j < table[i].pile.length; j++) {
                tableString.append(table[i].pile[j]);
            }
        }
        return tableString.toString();
    }

    /**
     * Attempts to play a move for the given player in the specified column.
     *
     * @param col the column index
     * @param player the player attempting the move
     * @return the result of the play attempt
     */
    private synchronized PlayStatus play(int col, Player player) {
        if(player.id == playerTurn) {
            if(col < 0 || col >= MAX_COL) {
                return PlayStatus.OUT_OF_RANGE;
            }
            PlayStatus status = table[col].put(player.symbol);
            if(status == PlayStatus.ACCEPTED) {
                playerTurn = (playerTurn + 1) % 2;
                return status;
            } else if(status == PlayStatus.COLUMN_FULL) {
                return status;
            }
        }
        return PlayStatus.NOT_YOUR_TURN;
    }

    /**
     * Returns the ID of the player whose turn it currently is.
     *
     * @return the current player's ID (0 or 1)
     */
    private synchronized int getTurn() {
        return playerTurn;
    }

    /**
     * Internal class representing a single column of the board.
     * Stores the number of tokens and handles insertion.
     */
    private static class Pile {
        private int height = 0;
        private char[] pile = new char[MAX_ROW];

        /**
         * Creates an empty column initialized with '-'.
         */
        public Pile() {
            Arrays.fill(pile, '-');
        }

        /**
         * Attempts to place a token in this column.
         *
         * @param symbol the player's symbol
         * @return ACCEPTED if placed, COLUMN_FULL otherwise
         */
        public PlayStatus put(char symbol) {
            if(this.height == MAX_ROW) {
                return PlayStatus.COLUMN_FULL; // colonne pleine
            }

            pile[this.height] = symbol;
            this.height++;
            return PlayStatus.ACCEPTED;
        }

        /**
         * Checks whether the column is full.
         *
         * @return true if full, false otherwise
         */
        public boolean isFull() {
            return height == MAX_ROW;
        }
    }

    /**
     * Static helper method used by the client to display a board
     * string in a human-readable grid format.
     *
     * @param tableString the linearized board representation
     */
    static public void displayTable(String tableString) {

        StringBuilder string = new StringBuilder();

        for (int r = MAX_ROW; r >=0 ; r--) {

            if(r == MAX_ROW) {
                string.append("|"); // System.out.print("|");
                for (int c = 0; c < MAX_COL; c++) {
                    string.append(c + 1).append("|"); //System.out.print(c+1 + "|");
                }
                string.append("\n");
                //System.out.println();
                continue;
            }


            for (int c = 0; c < MAX_COL; c++) {
                if(c == 0) {
                    string.append("|"); //System.out.print("|");
                }
                string.append(tableString.charAt((c * MAX_ROW + r))).append("|");
                // System.out.print(tableString.charAt((c * MAX_ROW + r)) + "|");
            }
            string.append("\n");
            //System.out.println();
        }

        System.out.println(string);
    }

    /**
     * Inner class representing a remote player interacting with the engine.
     * Stores identifying information and delegates move requests.
     */
    public class Player {
        String name = "";
        char symbol;
        int id;

        /**
         * Creates a new player entry with its symbol and ID.
         *
         * @param symbol the symbol assigned to this player
         * @param id the player's unique internal ID (0 or 1)
         */
        private Player(char symbol, int id) {
            this.symbol = symbol;
            this.id = id;
        }

        /**
         * Sets the player's display name.
         *
         * @param name the player's name
         */
        public synchronized void setName(String name) {
            this.name = name;
        }

        /**
         * Attempts to play a move in the specified column.
         *
         * @param col the column index
         * @return the play status returned by the engine
         */
        public PlayStatus play(int col) {
            return P4Engine.this.play(col, this);
        }

        /**
         * Disconnects the player from the engine and updates internal state.
         */
        public synchronized void disconnect() {
            if(nbPlayer.get() > 0) {
                nbPlayer.decrementAndGet();
            }
            playerTurn = (playerTurn + 1) % 2;
        }

        /**
         * Returns the name of the opponent player.
         *
         * @return the opponent's name or empty if none
         */
        public synchronized String getOponentName() {
            return P4Engine.this.getOponentName(this);
        }

        /**
         * Returns this player's name.
         *
         * @return the name of the player
         */
        public synchronized String getName() {
            return name;
        }

        /**
         * Returns this player's assigned symbol.
         *
         * @return the symbol ('X' or 'O')
         */
        public char getSymbol() {
            return symbol;
        }

        /**
         * Returns the internal numeric ID of the player.
         *
         * @return 0 or 1
         */
        public int getId() {
            return id;
        }

        /**
         * Checks whether it is currently this player's turn.
         *
         * @return true if it is their turn, false otherwise
         */
        public boolean isMyTurn() {
            return getTurn() == this.id;
        }

        /**
         * Returns the player's name as its string representation.
         *
         * @return the name of the player
         */
        @Override
        public String toString() {
            return this.name;
        }
    }
}