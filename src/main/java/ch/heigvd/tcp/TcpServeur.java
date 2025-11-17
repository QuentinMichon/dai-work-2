package ch.heigvd.tcp;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TcpServeur {
    private Socket socket = null;
    private ServerSocket serverSocket = null;
    private BufferedReader in = null;
    private BufferedWriter out = null;

    private final int port;

    public TcpServeur(int port) {
        this.port = port;
    }

    public boolean up() {

        // ---|Connexion serveur|--------------------------
        try {
            this.serverSocket = new ServerSocket(this.port);
            System.out.println("[Serveur] écoute le port " + this.port);
            System.out.println("[Serveur] En attente de connexion...");
            this.socket = serverSocket.accept(); // attend une connexion
        } catch (IOException e) {
            System.out.println("[Serveur] Erreur de connexion");
            this.close();
            return false;
        }

        // ---|Ouverture du BufStream input|-----------------
        try {
            Reader reader = new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8);
            in = new BufferedReader(reader);
            System.out.println("[Serveur] Ouverture du stream input : ok");
        } catch (IOException e) {
            System.out.println("[Serveur] Erreur de connexion au flux d'entrée");
            this.close();
            return false;
        }

        // ---|Ouverture du BufStream output|-----------------
        try {
            Writer writer = new OutputStreamWriter(this.socket.getOutputStream(), StandardCharsets.UTF_8);
            out = new BufferedWriter(writer);
            System.out.println("[Serveur] Ouverture du stream output : ok");
        } catch (IOException e) {
            System.out.println("[Serveur] Erreur de connexion au flux de sortie");
            this.close();
            return false;
        }

        return true;
    }

    public void send(String cmd) {
        if (this.out != null) {
            try {
                this.out.write(cmd + "\n");
                this.out.flush();
            } catch (IOException e) {
                System.out.println("[Serveur] Erreur lors de l'envoie de la commande : " + cmd);
            }
        } else {
            System.out.println("[Serveur] Stream output non connecté : impossible d'envoyer une requête");
        }
    }

    public String receive() {
        if(this.in != null) {
            try {
                return this.in.readLine();
            } catch (IOException e) {
                System.out.println("[Serveur] Impossible de lire le client");
            }
        }
        return "";
    }

    public void close() {
        if (this.socket != null) {
            try {
                this.socket.close();
            } catch (IOException e) {
                System.out.println("[Serveur] erreur lors de la fermeture du socket");
            }
        }
        if (this.in != null) {
            try {
                this.in.close();
            } catch (IOException e) {
                System.out.println("[Serveur] erreur lors de la fermeture du flux d'entrée");
            }
        }
        if (this.out != null) {
            try {
                this.out.close();
            } catch (IOException e) {
                System.out.println("[Serveur] erreur lors de la fermeture du flux de sortie");
            }
        }
    }
}
