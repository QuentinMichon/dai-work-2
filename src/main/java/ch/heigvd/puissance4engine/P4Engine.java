package ch.heigvd.puissance4engine;

import java.util.Arrays;

public class P4Engine {

    static private final int MAX_COL = 7;
    static private final int MAX_ROW = 6;

    private final Pile[] table = new Pile[MAX_COL];
    private final int[] offset = {0, 1, 2, 3};

    private int nbPlayer = 0;
    private final char[] symbolUsers = {'X', 'O'};
    private int playerTurn = 0;

    private Player[] players = new Player[2];

    public P4Engine() {
        for (int i = 0; i < MAX_COL; i++) {
            table[i] = new Pile();
        }
    }

    public Player newPlayer() {
        if(nbPlayer < 2) {
            int id = nbPlayer++;
            players[id] = new Player(symbolUsers[id], id);
            return players[id];
        } else {
            return null;
        }
    }

    public int getNbPlayer() {
        return nbPlayer;
    }

    private String getOponentName(Player player) {
        if(player.id == 0) {
            if(nbPlayer == 1) {
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

    public boolean checkWin(char symbol) {
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

                if(suite == 4) { return true; }
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

                if(suite == 4) { return true; }
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

                if(suite == 4) { return true; }
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

                if(suite == 4) { return true; }
            }
        }

        return false;
    }

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

    private PlayStatus play(int col, Player player) {
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


    // Classe interne : Pile
    private static class Pile {
        private int height = 0;
        public char[] pile = new char[MAX_ROW];

        public Pile() {
            Arrays.fill(pile, '-');
        }

        public PlayStatus put(char symbol) {
            if(this.height == MAX_ROW) {
                return PlayStatus.COLUMN_FULL; // colonne pleine
            }

            pile[this.height] = symbol;
            this.height++;
            return PlayStatus.ACCEPTED;
        }
    }

    // Méthode static pour l'affichage coté client
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

    // INNER CLASS PLAYER joue le role de remote
    public class Player {
        String name = "";
        char symbol;
        int id;

        private Player(char symbol, int id) {
            this.symbol = symbol;
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public PlayStatus play(int col) {
            return P4Engine.this.play(col, this);
        }

        public void disconnect() {
            if(nbPlayer > 1) {
                nbPlayer--;
            }
        }

        public String getOponentName() {
            return P4Engine.this.getOponentName(this);
        }

        public String getName() {
            return name;
        }

        public char getSymbol() {
            return symbol;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}