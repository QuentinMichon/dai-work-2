/**
 * @file : Root.java
 * @author : Quentin Michon, Gianni BEE
 * @date : 2025-09-29
 * @since : 1.0
 */

package ch.heigvd.commands;

import ch.heigvd.util.TCPServeur;
import ch.heigvd.util.TcpClient;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

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
    }

    //------------------------- CLIENT -------------------------------------------------------------------------

    private void lancement_client() {
        System.out.println("Lancement du client...");
    }

    private enum TYPE {
        CLIENT, SERVER
    }
}