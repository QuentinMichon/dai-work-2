package ch.heigvd.tcp;

import java.io.*;
import java.net.InetAddress;
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

    public String clientInfo() {
        if (socket == null || socket.isClosed()) {
            return "Aucun client connecté.";
        }

        StringBuilder info = new StringBuilder();

        InetAddress addr = socket.getInetAddress();

        info.append("Client IP : ").append(addr.getHostAddress()).append("\n");
        info.append("Client Hostname : ").append(addr.getHostName()).append("\n");
        info.append("Client Port : ").append(socket.getPort()).append("\n");

        return info.toString();
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
                return this.in.readLine();
            } catch (IOException e) {
                System.out.println("[Client] Impossible de lire le serveur");
            }
        }
        return "";
    }

    public boolean isConnected() {
        try {
            out.write("ping");
            out.flush();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public void close() {
        if (this.socket != null) {
            try {
                this.socket.close();
                System.out.println("[Client] Socket closed");
            } catch (IOException e) {
                System.out.println("[Client] erreur lors de la fermeture du socket");
            }
        }
        if (this.in != null) {
            try {
                this.in.close();
                System.out.println("[Client] InputStream closed");
            } catch (IOException e) {
                System.out.println("[Client] erreur lors de la fermeture du flux d'entrée");
            }
        }
        if (this.out != null) {
            try {
                this.out.close();
                System.out.println("[Client] OutputStream closed");
            } catch (IOException e) {
                System.out.println("[Client] erreur lors de la fermeture du flux de sortie");
            }
        }
    }
}
