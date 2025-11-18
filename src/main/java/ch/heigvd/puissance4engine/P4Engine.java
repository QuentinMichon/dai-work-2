package ch.heigvd.puissance4engine;

import java.util.Arrays;

public class P4Engine {

    static private final int MAX_COL = 7;
    static private final int MAX_ROW = 6;

    private final Pile[] table = new Pile[MAX_COL];

    private final int[] offset = {0, 1, 2, 3};


    public P4Engine() {
        for (int i = 0; i < MAX_COL; i++) {
            table[i] = new Pile();
        }
    }

    public boolean play(int col, char symbol) {
        return table[col].put(symbol);
    }

    @Override
    public String toString() {
        String tableString = "";
        for (int i = 0; i < MAX_COL; i++) {
            for (int j = 0; j < table[i].pile.length; j++) {
                tableString += table[i].pile[j];
            }
        }
        return tableString;
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




    private static class Pile {
        private int height = 0;
        public char[] pile = new char[MAX_ROW];

        public Pile() {
            Arrays.fill(pile, '-');
        }

        public boolean put(char symbol) {
            if(this.height == MAX_ROW) {
                return false; // colonne pleine
            }

            pile[this.height] = symbol;
            this.height++;
            return true;
        }
    }

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
}