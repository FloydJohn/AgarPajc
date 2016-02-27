package it.unibs.pajc.agar.network;

import it.unibs.pajc.agar.GameController;
import it.unibs.pajc.agar.universe.Player;
import it.unibs.pajc.agar.universe.Universe;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public abstract class NetworkConnection extends Thread{

    protected Universe myUniverse;
    protected NetworkController controller;
    protected BufferedWriter out;
    private Socket socket;
    private boolean toClose = false;
    private int attempts = 0;

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
            attempts = 0;
        } catch (Exception e) {
            if (attempts++ < 3) return;
            if (this instanceof Server)
                System.out.printf("Player %s left the game.\n", ((Server) this).getPlayerName());
            else System.out.println("Server disconnected.");
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
            if (myUniverse.getEatenFoods().peek() != null) {
                JSONArray eaten = new JSONArray();
                while (myUniverse.getEatenFoods().peek() != null) eaten.put(myUniverse.getEatenFoods().poll().getId());
                json.put("e", eaten);
            }
            if (myUniverse.getEatenPlayers().peek() != null) {
                System.out.println("[NetworkConnection.Client] Found players in eatenPlayers queue! Sending...");
                JSONArray eatenPlayers = new JSONArray();
                while (myUniverse.getEatenPlayers().peek() != null)
                    eatenPlayers.put(myUniverse.getEatenPlayers().poll().getName());
                json.put("ep", eatenPlayers);
            }
            super.send(json);
        }

        @Override
        public void receive(String in) {
            try {
                JSONObject inJson = new JSONObject(in);
                if (inJson.has("error")) {
                    GameController.getInstance().abort(inJson.getString("error"));
                } else myUniverse.fromJSON(inJson);
            } catch (IllegalArgumentException e) {
                System.out.println("[NetworkConnection.Client] Bad formatted json in: " + e);
            }
        }
    }

    public static class Server extends NetworkConnection {

        private String playerName = "NoName";
        private ArrayList<Integer> notifiedFood = new ArrayList<>();

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

            int universeIndex, notifiedIndex;
            for (int id = 0; id < myUniverse.getCurrentFoodId(); id++) {
                universeIndex = -1;
                notifiedIndex = -1;
                for (int i = 0; i < foodJson.length(); i++)
                    if (foodJson.getJSONObject(i).getInt("id") == id)
                        universeIndex = i;

                for (int i = 0; i < notifiedFood.size(); i++)
                    if (notifiedFood.get(i).equals(id))
                        notifiedIndex = i;

                if ((notifiedIndex >= 0 && universeIndex >= 0) || (notifiedIndex < 0 && universeIndex < 0)) continue;

                if (universeIndex >= 0) {
                    //Added
                    addedFood.put(foodJson.getJSONObject(universeIndex));
                    notifiedFood.add(id);
                } else {
                    //Removed
                    removedFood.put(id);
                    notifiedFood.remove(notifiedIndex);
                }
            }

            JSONObject out = new JSONObject();
            if (addedFood.length() > 0) out.put("a", addedFood);
            if (removedFood.length() > 0) out.put("r", removedFood);
            out.put("p", myUniverse.getJson().get("p"));
            super.send(out);
        }

        private void sendError(String error) {
            super.send(new JSONObject().put("error", error));
        }

        @Override
        public void receive(String in) {
            try {
                JSONObject inJson = new JSONObject(in);
                playerName = inJson.getString("n");
                Player thisPlayer = myUniverse.getPlayer(playerName);
                if (thisPlayer == null) {
                    if (!myUniverse.existsPlayer(playerName)) myUniverse.updatePlayer(inJson, true);
                    else sendError("Name already used");
                }
                else thisPlayer.fromJSON(new JSONObject(in));

                if (inJson.has("ep")) {
                    myUniverse.eatPlayers(inJson.getJSONArray("ep"));
                    if (thisPlayer != null)
                        System.out.println("[NetworkConnection.Server] " + thisPlayer.getName() + " ate " + inJson.getJSONArray("ep").get(0) + "...");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("[NetworkConnection.Server] Bad formatted json in: " + e);
            }
        }
    }

}
