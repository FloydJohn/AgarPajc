package it.unibs.pajc.agar.universe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Universe {

    private final Random generator = new Random();
    private final boolean isServer;
    private ConcurrentHashMap<String, Player> players = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Food> foods = new ConcurrentHashMap<>();
    private int currentFoodId = 0;
    private Dimension universeDimension;
    private Player player;
    private JSONObject jsonData = new JSONObject();
    private ConcurrentLinkedQueue<Food> eatenFoods = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Player> eatenPlayers = new ConcurrentLinkedQueue<>();
    //Client
    private boolean updatedByServer = false;

    public Universe(String playerName, Dimension universeDimension, boolean isServer) {
        this.universeDimension = universeDimension;
        player = new Player(playerName, true, new Point2D.Float(20, 50), 30, new Random().nextInt(Player.possibleColors.length), this);
        players.put(playerName, player);
        this.isServer = isServer;
    }

    public ConcurrentLinkedQueue<Food> getEatenFoods() {
        return eatenFoods;
    }

    public ConcurrentLinkedQueue<Player> getEatenPlayers() {
        return eatenPlayers;
    }

    public void generateRandomFood(int foodNumber) {
        for (int i = 0; i < foodNumber; i++) {
            Point2D.Float pos = new Point2D.Float(
                    generator.nextInt(universeDimension.width),
                    generator.nextInt(universeDimension.height));

            foods.put(currentFoodId, new Food(this, pos, currentFoodId++));
        }
    }

    public Dimension getBounds() {
        return universeDimension;
    }

    public ConcurrentHashMap<Integer, Food> getFoods() {
        return foods;
    }

    public ConcurrentHashMap<String, Player> getPlayers() {
        return players;
    }

    public void update() {
        players.values().forEach(Player::updateMass);
        player.update();
        for (Iterator<Food> iterator = foods.values().iterator(); iterator.hasNext(); ) {
            Food f = iterator.next();
            switch (f.getCurrentState()) {
                case ADDED:
                    if (player.intersects(f).equals(IntersectionType.THIS_EATS)) {
                        player.eat(f);
                        f.setCurrentState(Food.State.REMOVING);
                    }
                    break;
                case REMOVING:
                    if (!isServer) eatenFoods.add(f);
                    iterator.remove();
                    break;
            }
        }
        for (Iterator<Player> iterator = players.values().iterator(); iterator.hasNext(); ) {
            Player p = iterator.next();
            if (p == player) continue;
            if (player.intersects(p).equals(IntersectionType.THIS_EATS)) {
                player.eat(p);
                if (p.getPieces().size() == 0) {
                    eatenPlayers.add(p);
                    iterator.remove();
                }
            }
        }
        jsonData = toJSON();
    }

    public Player getPlayer() {
        return player;
    }

    //Server
    public JSONObject toJSON() {
        JSONArray playersJson = new JSONArray(), foodJson = new JSONArray();
        for (Player p : players.values()) playersJson.put(p.toJSON());
        for (Food f : foods.values()) foodJson.put(f.toJSON());
        JSONObject out = new JSONObject().put("p", playersJson);
        out.put("f", foodJson);
        return out;
    }

    public void fromJSON(String inString) throws IllegalArgumentException {
        try {
            JSONObject jsonObject = new JSONObject(inString);
            //Parse players
            JSONArray playersJson = jsonObject.getJSONArray("p");
            boolean alive = false;
            for (Object element : playersJson) {
                JSONObject playerJson = (JSONObject) element;
                Player selected = getPlayer(playerJson.getString("n"));
                if (selected == player) {
                    alive = true;
                    updatedByServer = true;
                    continue;    //Skips update if is this player
                }
                if (selected == null) updatePlayer(playerJson, true);
                else selected.fromJSON(playerJson);
            }

            if (!alive && updatedByServer) {
                System.out.println("I'm dead, leaving!");
                updatedByServer = true;
                System.exit(0);
            }

            //Parse Eaten
            if (jsonObject.has("r"))
                eatFoods(jsonObject.getJSONArray("r"));
            //Parse Added
            if (jsonObject.has("a")) {
                for (Object foodAdded : jsonObject.getJSONArray("a")) {
                    JSONObject currentFood = (JSONObject) foodAdded;
                    Food newFood = new Food(this, currentFood);
                    foods.put(newFood.getId(), newFood);
                }
            }
        } catch (JSONException e) {
            throw new IllegalArgumentException("Could not parse Universe", e);
        }
    }

    public void eatFoods(JSONArray e) {
        for (Object foodEaten : e)
            //noinspection RedundantCast
            foods.remove((Integer) foodEaten);
    }

    public void eatPlayers(JSONArray eatenPlayers) {
        for (Object p : eatenPlayers) {
            //noinspection RedundantCast
            players.remove((String) p);
        }
    }

    public Player getPlayer(String name) {
        return players.get(name);
    }

    public void updatePlayer(JSONObject inJson, boolean toAdd) {
        if (toAdd) {
            Player newPlayer = new Player(this, inJson);
            players.put(newPlayer.getName(), newPlayer);
        } else players.remove(inJson.getString("n"));
    }

    public void removePlayer(String name) {
        players.remove(name);
    }

    public JSONObject getJson() {
        return jsonData;
    }

    public int getCurrentFoodId() {
        return currentFoodId;
    }
}
