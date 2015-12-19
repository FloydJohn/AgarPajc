package it.unibs.pajc.agar.network;

import it.unibs.pajc.agar.universe.Universe;

import java.io.BufferedWriter;
import java.net.Socket;

public class ClientConnection extends NetworkConnection {


    public ClientConnection(Universe universe, Socket socket, NetworkController networkController) {
        super(universe, socket, networkController);
    }

    @Override
    public void send(BufferedWriter out) {
        //TODO Send player...
    }

    @Override
    public void receive(String in) {
        //TODO Update universe...
    }
}
