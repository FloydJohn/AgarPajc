package it.unibs.pajc.agar.network;

import it.unibs.pajc.agar.universe.Universe;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public abstract class NetworkConnection extends Thread{

    protected Universe myUniverse;
    protected NetworkController controller;
    private Socket socket;
    private boolean toClose = false;

    public NetworkConnection(Universe universe, Socket socket, NetworkController networkController) {
        this.myUniverse = universe;
        this.socket = socket;
        this.controller = networkController;
    }

    @Override
    public void interrupt() {
        super.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Failed to close socket: "+e);
        } finally {
            controller.updateConnections(this, false);
        }
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter out= new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
                ) {
            new Timer(20, e -> send(out)).start();
            while(!toClose) {
                String received = in.readLine(); //Blocking
                if (received == null) {
                    toClose = true;
                    continue;
                }
                receive(received);
            }
        } catch (IOException e) {
            System.out.println("Error opening sockets: "+e);
            interrupt();
        }
    }

    public abstract void send(BufferedWriter out);
    public abstract void receive(String received);
}
