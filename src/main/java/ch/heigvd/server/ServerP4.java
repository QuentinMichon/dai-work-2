package ch.heigvd.server;

import ch.heigvd.puissance4engine.EndOfGameStatus;
import ch.heigvd.puissance4engine.P4Engine;
import ch.heigvd.puissance4engine.PlayStatus;
import ch.heigvd.tcp.TcpServeur;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Main TCP server for hosting a single Puissance 4 (Connect Four) match.
 * <p>
 * The server accepts up to two players. When the first player connects,
 * a new game engine is created. When both players have joined with valid
 * names, the match begins. Communication follows a simple text protocol.
 * <p>
 * Synchronization between the two players is handled through semaphores,
 * allowing strict turn-based interactions.
 */
public class ServerP4 {

    Thread threadPlayer1 = null;
    Thread threadPlayer2 = null;
    /**
     * Global synchronization barrier used between players
     * (turn changes, connection events…).
     */
    private static Semaphore synchro = new Semaphore(0);
    /**
     * Barrier used to synchronize end-of-game messages for player 1.
     */
    private Semaphore player1end = new Semaphore(0);
    /**
     * Barrier used to synchronize end-of-game messages for player 2.
     */
    private Semaphore player2end = new Semaphore(0);
    /**
     * Reference to the unique P4 game engine instance.
     * Reset each time a new match starts.
     */
    private static AtomicReference<P4Engine> engine = new AtomicReference<>(new P4Engine()); // moteur de jeu unique (1 partie possible)
    /**
     * Indicates whether the match is over.
     */
    private final AtomicBoolean endOfGame = new AtomicBoolean(false);

    /**
     * Starts the server on TCP port 4444.
     * <p>
     * Accepts incoming clients, assigns them to a player slot,
     * and launches a corresponding ClientHandler thread.
     */
    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[Server p4] started");
            System.out.println("[Server p4] listening on port " + port);

            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();  // bloquant

                int nbPlayer = engine.get().getNbPlayer();
                switch (nbPlayer) {
                    case 0:
                        engine = new AtomicReference<>(new P4Engine()); // nouveau engine (reset pour nouvelle partie)
                        endOfGame.set(false); // reset le endOfGame
                        synchro = new Semaphore(0);

                        threadPlayer1 = new Thread(new ClientHandler(clientSocket, engine.get().newPlayer(), this.endOfGame, player1end, player2end));
                        threadPlayer1.start();
                        break;
                    case 1:
                        threadPlayer2 = new Thread(new ClientHandler(clientSocket, engine.get().newPlayer(), this.endOfGame, player2end, player1end));
                        threadPlayer2.start();
                        break;
                    default:
                        TcpServeur tcpServeur = new TcpServeur(clientSocket);
                        tcpServeur.connect();
                        tcpServeur.receive();
                        tcpServeur.send("GAME_NOT_FREE");
                        tcpServeur.close();
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("[Server p4] exception: " + e);
        }
    }

    /**
     * Handles the full session for a single connected player.
     * <p>
     * This includes:
     * - processing JOIN command
     * - waiting for game start
     * - handling turn-based PLAY commands
     * - sending MOVE_OK, TURN, WIN/LOSE/DRAW/FORFEIT
     * - managing disconnections
     */
    static class ClientHandler implements Runnable {
        /**
         * TCP communication wrapper for this player's socket.
         */
        private final TcpServeur tcp;
        /**
         * P4Engine Player instance representing this client.
         */
        private final P4Engine.Player player;
        /**
         * Shared flag indicating whether the game has ended.
         */
        private final AtomicBoolean endOfGame;
        /**
         * Semaphore the player waits on at the end of the game.
         */
        private final Semaphore thisPlayerEnd;
        /**
         * Semaphore released by the opponent when they finish.
         */
        private final Semaphore otherPlayerEnd;

        /**
         * Creates a new client handler thread.
         *
         * @param clientSocket the socket connected to the client
         * @param player the game engine's Player object
         * @param endOfGame shared end-of-game flag
         * @param thisPlayerEnd synchronization barrier for this player
         * @param otherPlayerEnd synchronization barrier for the opponent
         */
        public ClientHandler(Socket clientSocket, P4Engine.Player player, AtomicBoolean endOfGame, Semaphore thisPlayerEnd, Semaphore otherPlayerEnd) {
            this.tcp = new TcpServeur(clientSocket);
            this.player = player;
            this.endOfGame = endOfGame;
            this.thisPlayerEnd = thisPlayerEnd;
            this.otherPlayerEnd = otherPlayerEnd;
        }

        @Override
        public void run() {
            String cmd = "";
            boolean playerJoined = false;

            tcp.connect();  // connecte les flux entrée/sortie avec le client

            // -------- Partie JOIN -----------------------------------------
            while(player.getName().isEmpty()) {

                cmd = tcp.receive();

                if (cmd == null) {
                    // connexion fermée par le client
                    synchro.release();
                    break;
                } else {
                    List<String> messageParts = Arrays.asList(cmd.split(" ", 2));
                    if(messageParts.size() < 2) {
                        tcp.send("UNKNOWN");
                        continue;
                    }

                    String header = messageParts.get(0);
                    List<String> params = Arrays.asList(messageParts.get(1).split(" "));

                    if (header.equals("JOIN")) {
                        if (params.size() != 1) {
                            tcp.send("UNKNOWN");
                        } else {
                            String name = params.getFirst();

                            if (!player.getOponentName().isEmpty()) {
                                if (player.getOponentName().equals(name)) {
                                    tcp.send("NOK");
                                    continue;
                                } else {
                                    player.setName(name);
                                    tcp.send("OK");
                                    // débloquer le joueur en attente
                                    synchro.release();
                                    tcp.send("START " + player.getOponentName() + " " + player.getSymbol());
                                }
                            } else {
                                player.setName(name);
                                tcp.send("OK");

                                try {
                                    // bloquer le joueur en attente d'un autre
                                    synchro.acquire();
                                } catch (InterruptedException e) {
                                    System.out.println("[Server p4] interrupted" + e.getMessage());
                                }
                                tcp.send("START " + player.getOponentName() + " " + player.getSymbol());
                            }
                        }
                    } else {
                        tcp.send("UNKNOWN");
                    }
                }
            }

            // -------- Partie AFTER START -----------------------------------------

            boolean playValid = false;
            EndOfGameStatus endOfGameStatus = EndOfGameStatus.LOOSE;

            while (!endOfGame.get()) {
                if(player.isMyTurn()) {
                    playValid = false;
                    String table = engine.get().toString();
                    tcp.send("TURN " + table);

                    while (!playValid) {
                        cmd = tcp.receive();
                        if (cmd == null || !tcp.isClientConnected()) {
                            System.out.println("[Server p4] player " + player + " is disconnected");
                            endOfGame.set(true); // met fin à la partie
                            player.disconnect(); // se déconnecte et passe la main à l'autre joueur
                            synchro.release(); // relache la barrière de synchro
                            thisPlayerEnd.release(); // relache la barrière de synchro de fin
                            tcp.close();
                            System.out.println("[Server p4] player " + player + " disconnected during the game");
                            return; // fin de la session du joueur

                        } else {
                            // décodage de la commande
                            System.out.println("[Server p4] player " + player + " cmd : " + cmd);

                            List<String> messageParts = Arrays.asList(cmd.split(" ", 2));

                            if(messageParts.size() < 2) {
                                tcp.send("UNKNOWN");
                                continue;
                            }

                            String header = messageParts.get(0);
                            List<String> params = Arrays.asList(messageParts.get(1).split(" "));

                            if (!header.equals("PLAY")) {
                                tcp.send("UNKNOWN");
                                continue;
                            }

                            if(params.size() != 1) {
                                tcp.send("UNKNOWN");
                                continue;
                            }

                            if (!isInteger(params.getFirst())) {
                                tcp.send("UNKNOWN");
                                continue;
                            }

                            int col = Integer.parseInt(params.getFirst());
                            PlayStatus status = player.play(col);

                            switch (status) {
                                case COLUMN_FULL:
                                    tcp.send("FULL");
                                    break;
                                case NOT_YOUR_TURN:
                                    tcp.send("NOT_YOUR_TURN");
                                    break;
                                case OUT_OF_RANGE:
                                    // pas encore de commande de retour
                                    tcp.send("OUT_OF_RANGE");
                                    break;
                                case ACCEPTED:
                                    table = engine.get().toString();
                                    tcp.send("MOVE_OK " + table);
                                    playValid = true;
                                    break;
                            }

                            endOfGameStatus = engine.get().checkWin(player.getSymbol());
                            if(endOfGameStatus != EndOfGameStatus.LOOSE) { // LOOSE est le status par défaut
                                endOfGame.set(true);
                            }
                            synchro.release();
                        }
                    }

                } else {
                    // SLOT d'attente si ce n'est plus le tour du joueur
                    while(!player.isMyTurn()) {
                        if(tcp.haveClientRequest()) {
                            tcp.receive();
                            tcp.send("NOT_YOUR_TURN");
                        }
                    }
                    try {
                        synchro.acquire();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }


            endOfGameStatus = engine.get().checkWin(player.getSymbol());

            // point de rencontre
            thisPlayerEnd.release();
            try {
                otherPlayerEnd.acquire();
            } catch (InterruptedException e) {
                System.out.println("[Server p4] synchro impossible");
            }

            switch (endOfGameStatus) {
                case LOOSE:
                    tcp.send("LOOSE");
                    break;
                case WIN:
                    tcp.send("WIN");
                    break;
                case FORFEIT:
                    tcp.send("FORFEIT");
                    break;
                case DRAW:
                    tcp.send("DRAW");
                    break;
            }



            player.disconnect();
            tcp.close();
            System.out.println("[engine] " + engine.get().getNbPlayer());
        }
    }

    /**
     * Utility method to check if a string contains a valid integer.
     *
     * @param s the input string
     * @return true if the string is a valid integer
     */
    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
