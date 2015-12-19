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

    public static NetworkController getInstance() {
        if (instance == null) instance = new NetworkController();
        return instance;
    }

    private NetworkController() {
    }

    public void connect(boolean isServer, String serverIP, int port, Universe universe) {
        this.port = port;
        this.universe = universe;
        this.isServer = isServer;
        if (isServer) this.start();
        else {
            try {
                ClientConnection clientConnection = new ClientConnection(universe, new Socket(serverIP, port), this);
                updateConnections(clientConnection, true);
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
                ServerConnection newConnection = new ServerConnection(universe, serverSocket.accept(), this);
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
            connections.add(connection);
            connection.start();
        } else {
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
