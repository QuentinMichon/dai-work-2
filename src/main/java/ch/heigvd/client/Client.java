package ch.heigvd.client;

import ch.heigvd.puissance4engine.P4Engine;
import ch.heigvd.tcp.TcpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Client {
    private String name = "";
    private String opponentName = "";
    private char symbol = ' ';
    private TcpClient tcpClient;
    private boolean running = false;

    public Client(String hostname) {
        tcpClient = new TcpClient(hostname, 4444);
    }

    public boolean connect() {
        return tcpClient.connect();
    }

    public void run() {
        running = true;
        String cmd = "";

        // ouverture de la communication avec le terminal
        try(Reader systemInReader = new InputStreamReader(System.in, StandardCharsets.UTF_8);
            BufferedReader userIn = new BufferedReader(systemInReader)) {

            // ---- Programme principal ------

            // partie JOIN
            while (!cmd.equals("OK")) {
                System.out.println("--XO-- Welcome to Puissance 4 --OX--");
                System.out.print("Please enter your name: ");
                name = userIn.readLine();

                tcpClient.send("JOIN " + name);
                cmd = tcpClient.receive();

                if(cmd == null) {
                    tcpClient.close();
                    System.out.println("[Client] server closed the connection");
                    return;
                }

                if(cmd.equals("GAME_NOT_FREE")) {
                    System.out.println("The server is full, please try later...");
                    tcpClient.close();
                    return;
                }
            }
            // partie START
            System.out.println("Waiting an opponent...");

            cmd = tcpClient.receive();

            if(cmd == null) {
                tcpClient.close();
                System.out.println("Server closed the connection");
                return;
            }

            List<String> tokens = Arrays.asList(cmd.split(" "));
            if(tokens.size() != 3) {
                tcpClient.close();
                System.out.println("Server closed the connection");
                return;
            }

            if(!tokens.get(0).equals("START")) {
                tcpClient.close();
                System.out.println("Server send an invalid command");
                return;
            }

            opponentName = tokens.get(1);
            symbol = tokens.get(2).charAt(0);

            System.out.println("Your opponent is " + opponentName);
            System.out.println("You have the symbol " + symbol);

            // parte JEU
            while(running) {
                System.out.println("Waiting your turn...");
                cmd = tcpClient.receive();

                if(cmd == null) {
                    tcpClient.close();
                    System.out.println("Server send an invalid command");
                }

                List<String> params = Arrays.asList(cmd.split(" ", 2));

                if(params.isEmpty()) {
                    tcpClient.close();
                    System.out.println("Server send an invalid command");
                }

                switch (params.get(0)) {
                    case "TURN":
                        if(params.size() != 2) {
                            tcpClient.close();
                            System.out.println("Server send an invalid command");
                        }

                        tokens = Arrays.asList(params.get(1).split(" "));

                        if(tokens.size() != 1) {
                            tcpClient.close();
                            System.out.println("Server send an invalid command");
                        }

                        P4Engine.displayTable(tokens.getFirst());
                        break;
                    default:
                        // si le serveur renvoie autre chose, c'est une fin de partie
                        running = false;
                        break;
                }

                boolean valid_col = false;

                if(running)
                    System.out.print("It's your turn, select a column ("+ symbol +"): ");

                while(!valid_col && running) {
                    String userInput = userIn.readLine();

                    if(userInput.matches("[1-7]")) {
                        int col = Integer.parseInt(userInput) - 1;
                        tcpClient.send("PLAY " + col);
                    } else {
                        System.out.print("You have entered an invalid column\nSelect a column ("+ symbol +"): ");
                        continue;
                    }

                    cmd = tcpClient.receive();

                    if(cmd == null) {
                        tcpClient.close();
                        System.out.println("Server send an invalid command");
                        return;
                    }

                    params = Arrays.asList(cmd.split(" ", 2));

                    if(params.isEmpty()) {
                        tcpClient.close();
                        System.out.println("Server send an invalid command");
                        return;
                    }

                    switch (params.get(0)) {
                        case "FULL":
                            System.out.println("You selected a column already full.\nSelect a column ("+ symbol +"): ");
                            continue;
                        case "MOVE_OK":
                            if(params.size() != 2) {
                                tcpClient.close();
                                System.out.println("Server send an invalid command");
                                return;
                            }
                            tokens = Arrays.asList(params.get(1).split(" "));

                            if(tokens.size() != 1) {
                                tcpClient.close();
                                System.out.println("Server send an invalid command");
                                return;
                            }

                            P4Engine.displayTable(tokens.getFirst());
                            valid_col = true;
                            break;
                        default:
                            tcpClient.close();
                            System.out.println("Server send an invalid command");
                            return;
                    }
                }

            }

            // partie END OF GAME
            switch (cmd) {
                case "WIN":
                    System.out.println("You won !!!");
                    break;
                case "LOOSE":
                    System.out.println("You lost !!!");
                    break;
                case "DRAW":
                    System.out.println("It's a draw !!!");
                    break;
                case "FORFEIT":
                    System.out.println("You won by forfeit !!!");
                    break;
                default:
                    tcpClient.close();
                    System.out.println("Server send an invalid command");
                    return;
            }

            tcpClient.close();

        } catch (IOException e) {
            System.out.println("[Client] Error: " + e.getMessage());
            return;
        }
    }
}
