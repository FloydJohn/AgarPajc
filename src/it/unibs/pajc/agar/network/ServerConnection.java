package it.unibs.pajc.agar.network;

import it.unibs.pajc.agar.universe.Universe;

import java.io.BufferedWriter;
import java.net.Socket;

public class ServerConnection extends NetworkConnection {

    public ServerConnection(Universe universe, Socket socket, NetworkController networkController) {
        super(universe, socket, networkController);
    }

    @Override
    public void send(BufferedWriter out) {

    }

    @Override
    public void receive(String received) {

    }
}
