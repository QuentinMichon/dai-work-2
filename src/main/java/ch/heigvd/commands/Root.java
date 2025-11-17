/**
 * @file : Root.java
 * @author : Quentin Michon, Gianni BEE
 * @date : 2025-09-29
 * @since : 1.0
 */

package ch.heigvd.commands;

import ch.heigvd.tcp.TcpServeur;
import ch.heigvd.tcp.TcpClient;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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

    //------------------------- SERVEUR -------------------------------------------------------------------------
    private void lancement_serveur() {
        System.out.println("Lancement du serveur...");
        TcpServeur serveur = new TcpServeur(4444);
        serveur.up();

        String request = "";
        while(!request.equals("STOP")) {
            request = serveur.receive();
            System.out.println("[Serveur] : request from client : " + request);
        }

        serveur.close();
    }

    //------------------------- CLIENT -------------------------------------------------------------------------

    private void lancement_client() {
        System.out.println("Lancement du client...");
        TcpClient client = new TcpClient("localhost", 4444);
        client.connect();

        try (Reader systemInReader = new InputStreamReader(System.in, StandardCharsets.UTF_8);
             BufferedReader userIn = new BufferedReader(systemInReader))
        {
            String cmd = "";

            while (!cmd.equals("STOP")) {
                System.out.print("> ");
                cmd = userIn.readLine();
                client.send(cmd);
            }

        } catch(IOException e) {
            System.out.println("An error occurred.");
        }
    }

    private enum TYPE {
        CLIENT, SERVER
    }
}