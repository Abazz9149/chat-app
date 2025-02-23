import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChatServer {
    private static Set<ClientHandler> clients = new HashSet<>();

    public static void main(String[] args) {
        int port = 12345; // Define port

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Chat Server started on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected!");

                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Broadcast messages to all clients
    public static void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    // Remove disconnected clients
    public static void removeClient(ClientHandler client) {
        clients.remove(client);
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String nickname;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Ask client for a nickname
            out.println("Enter your nickname:");
            nickname = in.readLine();
            System.out.println(nickname + " joined the chat.");
            ChatServer.broadcastMessage("[SERVER] " + nickname + " has joined the chat!", this);

            // Read and broadcast messages
            String message;
            while ((message = in.readLine()) != null) {
                String time = dateFormat.format(new Date());
                String formattedMessage = "[" + time + "] " + nickname + ": " + message;
                System.out.println(formattedMessage);
                ChatServer.broadcastMessage(formattedMessage, this);
            }
        } catch (IOException e) {
            System.out.println(nickname + " disconnected.");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ChatServer.removeClient(this);
            ChatServer.broadcastMessage("[SERVER] " + nickname + " has left the chat.", this);
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}
