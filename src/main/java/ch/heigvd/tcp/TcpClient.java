package ch.heigvd.tcp;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TcpClient {

    // moyen de communication réseau
    private Socket socket = null;
    private BufferedReader in = null;
    private BufferedWriter out = null;

    // setting réseau
    private final String ip;
    private final int port;

    public TcpClient(String ip, int port) {
        this.port = port;
        this.ip = ip;
    }

    public boolean connect() {

        // ---|Connexion serveur|--------------------------
        try {
            socket = new Socket(this.ip, this.port);
            System.out.println("[Client] Connexion réussie");
        } catch (IOException e) {
            System.out.println("[client] Erreur de connexion");
            this.close();
            return false;
        }

        // ---|Ouverture du BufStream input|-----------------
        try {
            Reader reader = new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8);
            in = new BufferedReader(reader);
            System.out.println("[Client] Ouverture du stream input : ok");
        } catch (IOException e) {
            System.out.println("[Client] Erreur de connexion au flux d'entrée");
            this.close();
            return false;
        }

        // ---|Ouverture du BufStream output|-----------------
        try {
            Writer writer = new OutputStreamWriter(this.socket.getOutputStream(), StandardCharsets.UTF_8);
            out = new BufferedWriter(writer);
            System.out.println("[Client] Ouverture du stream output : ok");
        } catch (IOException e) {
            System.out.println("[Client] Erreur de connexion au flux de sortie");
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
                System.out.println("[Client] Erreur lors de l'envoie de la commande : " + cmd);
            }
        } else {
            System.out.println("[Client] Stream output non connecté : impossible d'envoyer une requête");
        }
    }

    public String receive() {
        if(this.in != null) {
            try {
                return this.in.readLine();  // todo ajouter un timeout ?
            } catch (IOException e) {
                System.out.println("[Client] Impossible de lire le serveur");
            }
        }
        return "";
    }

    public void close() {
        if (this.socket != null) {
            try {
                this.socket.close();
            } catch (IOException e) {
                System.out.println("[Client] erreur lors de la fermeture du socket");
            }
        }
        if (this.in != null) {
            try {
                this.in.close();
            } catch (IOException e) {
                System.out.println("[Client] erreur lors de la fermeture du flux d'entrée");
            }
        }
        if (this.out != null) {
            try {
                this.out.close();
            } catch (IOException e) {
                System.out.println("[Client] erreur lors de la fermeture du flux de sortie");
            }
        }
    }
}
