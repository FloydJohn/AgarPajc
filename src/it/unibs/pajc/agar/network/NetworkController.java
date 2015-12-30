package it.unibs.pajc.agar.network;

import it.unibs.pajc.agar.universe.Universe;

import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class NetworkController extends Thread {

    public static final int SEND_DELAY = 20;
    private static NetworkController instance;
    private final ArrayList<NetworkConnection> connections = new ArrayList<>();
    private Universe universe;
    private ConnectionState currentState = ConnectionState.LOGIN;
    private int port;
    private boolean isServer;

    private NetworkController() {
    }

    public static NetworkController getInstance() {
        if (instance == null) instance = new NetworkController();
        return instance;
    }

    public void connect(boolean isServer, String serverIP, int port, Universe universe) {
        this.port = port;
        this.universe = universe;
        this.isServer = isServer;
        new Timer(SEND_DELAY, e -> sendUpdate()).start();
        if (isServer) this.start();
        else {
            try {
                System.out.println("Trying to connect to server!");
                NetworkConnection.Client client = new NetworkConnection.Client(universe, new Socket(serverIP, port), this);
                updateConnections(client, true);
                currentState = ConnectionState.CONNECTED;
            } catch (IOException e) {
                System.out.println("Could not open connection: "+e);
            }
        }
    }

    @Override
    public void run() {

        while (currentState != ConnectionState.EXIT) {
            try (ServerSocket serverSocket = new ServerSocket(port)){
                currentState = ConnectionState.CONNECTED;
                NetworkConnection.Server newConnection = new NetworkConnection.Server(universe, serverSocket.accept(), this);
                System.out.println("Server received client request! Adding connection...");
                updateConnections(newConnection, true);
            } catch (IOException e) {
                System.out.println("Could not open connection: "+e);
                currentState = ConnectionState.EXIT;
            }
        }
    }

    public ConnectionState getCurrentState() {
        return currentState;
    }

    public synchronized void updateConnections(NetworkConnection connection, boolean toAdd) {
        if (toAdd) {
            System.out.println("Successfully added Connection!");
            synchronized (connections) {
                connections.add(connection);
            }
            connection.start();
        } else {
            connection.interrupt();
            synchronized (connections) {
                connections.remove(connection);
                if (connections.size() == 0 && !isServer) {
                    currentState = ConnectionState.EXIT;
                    this.interrupt();
                }
            }

            }
    }

    public synchronized void sendUpdate() {
        try {
            for (NetworkConnection c : connections) {
                if (isServer) c.send(universe.toJSON());
                else c.send(null);
            }
        } catch (ConcurrentModificationException ignored) {
        }
    }

    public enum ConnectionState {
        CONNECTED, LOGIN, DEAD, EXIT
    }
}
