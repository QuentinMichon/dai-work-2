import ch.heigvd.puissance4engine.EndOfGameStatus;
import ch.heigvd.puissance4engine.P4Engine;
import ch.heigvd.puissance4engine.PlayStatus;

import java.util.Scanner;

public class EngineTest {

    private static void testPlayerCreation(P4Engine.Player player) {
         if(player == null) {
             System.out.println("player is null");
         } else {
             System.out.println("player " + player + " is ready to play!!!");
         }
    }

    public static void main(String[] args) {
        System.out.println("---- programme de test du moteur de jeu puissance4 ----");


        P4Engine p4Engine = new P4Engine();
        Scanner scanner = new Scanner(System.in);
        int col;
        char[] symbols = {'X', 'O'};
        int playerTurn = 0;
        P4Engine.Player currentPlayer;


        P4Engine.Player player1 = p4Engine.newPlayer();
        P4Engine.Player player2 = p4Engine.newPlayer();
        P4Engine.Player player3 = p4Engine.newPlayer();

        testPlayerCreation(player1);
        testPlayerCreation(player2);
        testPlayerCreation(player3);

        player1.setName("Txtx");
        player2.setName("Toto");

        testPlayerCreation(player1);
        testPlayerCreation(player2);

        currentPlayer = player1;

        while(p4Engine.checkWin(symbols[playerTurn]) != EndOfGameStatus.LOOSE) {
            P4Engine.displayTable(p4Engine.toString());

            System.out.println("[" + symbols[playerTurn] + "] Select a column");
            System.out.print("> ");
            col = scanner.nextInt();

            PlayStatus status = currentPlayer.play(col-1);

            switch(status) {
                case ACCEPTED:
                    System.out.println("[" + currentPlayer + "] Accepted");
                    System.out.println("c'est le tour à " + currentPlayer.getOponentName());

                    if(playerTurn == 0) {
                        currentPlayer = player2;
                        playerTurn = 1;
                    } else {
                        currentPlayer = player1;
                        playerTurn = 0;
                    }

                    break;
                case NOT_YOUR_TURN:
                    System.out.println("[" + currentPlayer + "] Not your turn");
                    break;
                case OUT_OF_RANGE:
                    System.out.println("[" + currentPlayer + "] Out of range");
                    break;
                case COLUMN_FULL:
                    System.out.println("[" + currentPlayer + "] Column full");
                    break;
            }
        }

        P4Engine.displayTable(p4Engine.toString());
        System.out.println("------WIN------");

    }
}
