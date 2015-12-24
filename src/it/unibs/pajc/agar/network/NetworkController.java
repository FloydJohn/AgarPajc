package it.unibs.pajc.agar.network;

import it.unibs.pajc.agar.universe.Universe;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class NetworkController extends Thread {

    private static NetworkController instance;
    private Universe universe;
    private ConnectionState currentState = ConnectionState.LOGIN;
    private ArrayList<NetworkConnection> connections = new ArrayList<>();
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
        //noinspection InfiniteLoopStatement
        while (true) {
            try (ServerSocket serverSocket = new ServerSocket(port)){
                currentState = ConnectionState.CONNECTED;
                NetworkConnection.Server newConnection = new NetworkConnection.Server(universe, serverSocket.accept(), this);
                System.out.println("Server received client request! Adding connection...");
                updateConnections(newConnection, true);
            } catch (IOException e) {
                System.out.println("Could not open connection: "+e);
            }
        }
    }

    public ConnectionState getCurrentState() {
        return currentState;
    }

    public synchronized void updateConnections(NetworkConnection connection, boolean toAdd) {
        if (toAdd) {
            System.out.println("Successfully added Connection!");
            connections.add(connection);
            connection.start();
        } else {
            connection.interrupt();
            connections.remove(connection);
            if (connections.size() == 0 && !isServer) {
                currentState = ConnectionState.EXIT;
                this.interrupt();
            }
        }

    }

    public enum ConnectionState {
        CONNECTED, LOGIN, DEAD, EXIT
    }
}
