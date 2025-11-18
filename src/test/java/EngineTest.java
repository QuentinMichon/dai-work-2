import ch.heigvd.puissance4engine.P4Engine;

import java.util.Scanner;

public class EngineTest {

    static private void testWin(P4Engine engine) {
        if(engine.checkWin('X')) {
            System.out.println("win : X");
        } else {
            System.out.println("no win : X");
        }

        if(engine.checkWin('O')) {
            System.out.println("win : O");
        } else {
            System.out.println("no win : O");
        }
    }

    public static void main(String[] args) {
        System.out.println("---- programme de test du moteur de jeu puissance4 ----");


        P4Engine p4Engine = new P4Engine();
        Scanner scanner = new Scanner(System.in);
        int col;
        char[] symbols = {'X', 'O'};
        int player = 1;

        while(!p4Engine.checkWin(symbols[player])) {
            player = (player + 1) % 2;
            P4Engine.displayTable(p4Engine.toString());

            System.out.println("[" + symbols[player] + "] Select a column");
            System.out.print("> ");
            col = scanner.nextInt();

            p4Engine.play(col-1, symbols[player]);
        }

        P4Engine.displayTable(p4Engine.toString());
        System.out.println("------WIN------");

    }
}
