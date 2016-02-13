package it.unibs.pajc.agar.network;

import it.unibs.pajc.agar.universe.GameObject;
import it.unibs.pajc.agar.universe.Player;
import it.unibs.pajc.agar.universe.Universe;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public abstract class NetworkConnection extends Thread{

    protected Universe myUniverse;
    protected NetworkController controller;
    protected BufferedWriter out;
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
            out = null;
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
            this.out = out;
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

    public abstract void receive(String received);

    public abstract void send();

    protected void send(JSONObject json) {
        try {
            json.write(out);
            out.write("\n");
            out.flush();
            //System.out.println("SENT: " + json);
        } catch (Exception e) {
            System.out.println("Couldn't flush: " + e.getMessage());
            controller.updateConnections(this, false);
            this.interrupt();
        }
    }

    public static class Client extends NetworkConnection {

        public Client(Universe universe, Socket socket, NetworkController networkController) {
            super(universe, socket, networkController);
        }

        @Override
        public void send() {
            JSONObject json = myUniverse.getPlayer().toJSON();
            JSONArray eaten = new JSONArray();
            myUniverse.getFoods().values().stream().filter(f -> f.getCurrentState() == GameObject.State.TO_REMOVE).forEach(f -> {
                eaten.put(f.toJSON());
                f.setCurrentState(GameObject.State.REMOVED);
            });
            if (eaten.length() > 0) json.put("e", eaten);
            super.send(json);
        }

        @Override
        public void receive(String in) {
            try {
                myUniverse.fromJSON(in);
            } catch (IllegalArgumentException e) {
                System.out.println("Bad formatted json in: " + e);
            }
        }
    }

    public static class Server extends NetworkConnection {

        private String playerName = "NoName";
        private HashMap<Integer, Boolean> notifiedFood = new HashMap<>();

        public Server(Universe universe, Socket socket, NetworkController networkController) {
            super(universe, socket, networkController);
        }

        public String getPlayerName() {
            return playerName;
        }

        @Override
        public void send() {
            JSONArray foodJson = myUniverse.getJson().getJSONArray("f");
            JSONArray addedFood = new JSONArray();
            JSONArray removedFood = new JSONArray();
            for (int i = 0; i < myUniverse.getCurrentFoodId(); i++) {
                if (foodJson.opt(i) != null && notifiedFood.containsKey(i) ||
                        foodJson.opt(i) == null && !notifiedFood.containsKey(i)) continue;
                if (foodJson.opt(i) != null && !notifiedFood.containsKey(i)) addedFood.put(foodJson.get(i));
                else if (foodJson.opt(i) == null && notifiedFood.containsKey(i)) removedFood.put(foodJson.get(i));
            }
            JSONObject out = new JSONObject();
            out.put("a", addedFood);
            out.put("r", removedFood);
            out.put("p", myUniverse.getJson().get("p"));
            super.send(out);
        }

        @Override
        public void receive(String in) {
            try {
                JSONObject inJson = new JSONObject(in);
                Player thisPlayer = myUniverse.getPlayer(inJson.getString("n"));
                if (thisPlayer == null) myUniverse.updatePlayer(inJson, true);
                else thisPlayer.fromJSON(new JSONObject(in));
                playerName = inJson.getString("n");
            } catch (IllegalArgumentException e) {
                System.out.println("Bad formatted json in: " + e);
            }
        }
    }

}
