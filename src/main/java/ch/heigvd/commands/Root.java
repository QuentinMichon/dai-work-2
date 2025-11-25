/**
 * @file : Root.java
 * @author : Quentin Michon, Gianni BEE
 * @date : 2025-09-29
 * @since : 1.0
 */

package ch.heigvd.commands;

import ch.heigvd.client.Client;
import ch.heigvd.server.ServerP4;
import ch.heigvd.tcp.TcpServeur;
import ch.heigvd.tcp.TcpClient;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@CommandLine.Command(
        description = "Lancement d'un serveur ou client pour jouer au Puissance 4",
        version = "1.0.0",
        subcommands = {},
        scope = CommandLine.ScopeType.INHERIT,
        mixinStandardHelpOptions = true)
public class Root implements Runnable {

    @Override
    public void run() {
        switch (type) {
            case CLIENT:
                lancement_client();
                break;
            case SERVER:
                lancement_serveur();
                break;
            default:
                System.out.println("Error : unknown command");
                return;
        }
    }

    @CommandLine.Parameters(index = "0", description = "choisir de lancer un serveur ou un client")
    protected TYPE type;

    @CommandLine.Option(
            names = {"-", "--hostname"},
            description = "Hostname used by the client to contact the server",
            defaultValue = "localhost"
    )
    protected String hostname;

    //------------------------- SERVEUR -------------------------------------------------------------------------
    private void lancement_serveur() {
        System.out.println("Lancement du serveur...");

        ServerP4 serverP4 = new ServerP4();
        serverP4.start();
    }

    //------------------------- CLIENT -------------------------------------------------------------------------

    private void lancement_client() {
        System.out.println("Lancement du client...");

        Client client = new Client(hostname);
        if(client.connect()) {
            client.run();
        }
    }

    private enum TYPE {
        CLIENT, SERVER
    }

    static class ClientHandler implements Runnable {
        private final TcpServeur serveur;

        public ClientHandler(TcpServeur serveur, boolean stopper) {
            this.serveur = serveur;
        }

        @Override
        public void run() {
            serveur.connect();
            System.out.println(serveur.receive());
            serveur.close();
        }
    }
}
