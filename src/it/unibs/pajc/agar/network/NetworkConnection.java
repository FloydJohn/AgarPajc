package it.unibs.pajc.agar.network;

import it.unibs.pajc.agar.universe.Food;
import it.unibs.pajc.agar.universe.Universe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
        try {
            socket.close();
            super.interrupt();
        } catch (IOException e) {
            System.out.println("Failed to close socket: "+e);
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

    public static class Client extends NetworkConnection {

        public Client(Universe universe, Socket socket, NetworkController networkController) {
            super(universe, socket, networkController);
        }

        @Override
        public void send(BufferedWriter out) {
            try {
                JSONObject jsonOut = myUniverse.getPlayer().toJSON();
                JSONArray eaten = new JSONArray();
                myUniverse.getFoods().values().stream().filter(f -> f.getCurrentState() == Food.State.TO_ADD).forEach(f -> eaten.put(f.toJSON()));
                if (eaten.length() > 0) jsonOut.put("e", eaten);
                jsonOut.write(out);
                out.write("\n");
                out.flush();
            } catch (IOException e) {
                System.out.println("Couldn't flush: " + e);
                this.interrupt();
            }
        }

        @Override
        public void receive(String in) {
            try {
                System.out.println("Client received data: " + in);
                myUniverse.fromJSON(new JSONObject(in));
            } catch (IllegalArgumentException e) {
                System.out.println("Bad formatted json in: " + e);
            }
        }
    }

    public static class Server extends NetworkConnection {

        public Server(Universe universe, Socket socket, NetworkController networkController) {
            super(universe, socket, networkController);
        }

        @Override
        public void send(BufferedWriter out) {
            try {
                JSONObject jsonOut = myUniverse.toJSON();
                jsonOut.write(out);
                out.write("\n");
                out.flush();
            } catch (IOException | JSONException e) {
                System.out.println("Couldn't flush: " + e.getMessage());
                controller.updateConnections(this, false);
                this.interrupt();
            }
        }

        @Override
        public void receive(String in) {
            try {
                System.out.println("Server received data: " + in);
                myUniverse.getPlayer().fromJSON(new JSONObject(in));
            } catch (IllegalArgumentException e) {
                System.out.println("Bad formatted json in: " + e);
            }
        }
    }
}
