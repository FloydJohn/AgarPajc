package it.unibs.pajc.agar.universe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class Universe {

    private final Random generator = new Random();
    private ConcurrentHashMap<String, Player> players = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Food> foods = new ConcurrentHashMap<>();
    private int currentFoodId = 0;
    private Dimension universeDimension;
    private Player player;

    public Universe(String playerName, Dimension universeDimension) {
        this.universeDimension = universeDimension;
        player = new Player(playerName, true, new Point2D.Float(20, 50), 30, new Random().nextInt(Player.possibleColors.length), this);
        players.put(playerName, player);
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
        player.update();
        for (Iterator<Food> iterator = foods.values().iterator(); iterator.hasNext(); ) {
            Food f = iterator.next();
            if (f.getCurrentState() == GameObject.State.REMOVED) {
                iterator.remove();
                continue;
            }
            if (player.intersects(f)) {
                player.eat(f);
                f.setCurrentState(GameObject.State.TO_REMOVE);
            }
        }
    }

    public Player getPlayer() {
        return player;
    }

    //Server
    public JSONObject toJSON(String playerName) {
        JSONArray playersJson = new JSONArray(),
                eatenFood = new JSONArray(),
                addedFood = new JSONArray(),
                removedPlayers = new JSONArray();

        for (Player p : players.values()) {
            if (playerName.equals(p.getName())) continue; //Don't send updates back
            switch (p.getCurrentState()) {
                case TO_ADD:
                    p.setCurrentState(GameObject.State.ADDED);
                case ADDED:
                    playersJson.put(p.toJSON());
                    break;
                case TO_REMOVE:
                    p.setCurrentState(GameObject.State.REMOVED);
                case REMOVED:
                    removedPlayers.put(p.toJSON());
                    break;
            }
        }
        for (Food f : foods.values()) {
            switch (f.getCurrentState()) {
                case TO_ADD:
                    addedFood.put(f.toJSON());
                    f.setCurrentState(GameObject.State.ADDED);
                    break;
                case TO_REMOVE:
                    eatenFood.put(f.toJSON());
                    f.setCurrentState(GameObject.State.REMOVED);
                    break;
            }
        }

        JSONObject out = new JSONObject().put("p", playersJson);
        if (eatenFood.length() > 0) out.put("e", eatenFood);
        if (addedFood.length() > 0) out.put("a", addedFood);
        if (removedPlayers.length() > 0) out.put("r", removedPlayers);
        return out;
    }

    //Client
    public void fromJSON(String inString) throws IllegalArgumentException {
        try {
            JSONObject jsonObject = new JSONObject(inString);
            //Parse players
            JSONArray playersJson = jsonObject.getJSONArray("p");
            for (Object element : playersJson) {
                JSONObject playerJson = (JSONObject) element;
                Player selected = getPlayer(playerJson.getString("n"));
                if (selected == player) continue; //Skips update if is this player
                if (selected == null) updatePlayer(playerJson, true);
                else selected.fromJSON(playerJson);
            }
            //Parse removed
            if (jsonObject.has("r")) {
                JSONArray removedJson = jsonObject.getJSONArray("r");
                for (Object element : removedJson) updatePlayer((JSONObject) element, false);
            }
            //Parse Eaten
            if (jsonObject.has("e"))
                eatFoods(jsonObject.getJSONArray("e"));
            //Parse Added
            if (jsonObject.has("a"))
                for (Object foodAdded : jsonObject.getJSONArray("a")) {
                    JSONObject currentFood = (JSONObject) foodAdded;
                    Food newFood = new Food(this, new Point2D.Float(), currentFood.getInt("id"));
                    newFood.fromJSON(currentFood);
                    foods.put(newFood.getId(), newFood);
                }
        } catch (JSONException e) {
            throw new IllegalArgumentException("Could not parse Universe", e);
        }
    }

    public void eatFoods(JSONArray e) {
        for (Object foodEaten : e)
            foods.remove(((JSONObject) foodEaten).getInt("id"));
    }

    public Player getPlayer(String name) {
        return players.get(name);
    }

    public void updatePlayer(JSONObject inJson, boolean toAdd) {
        if (toAdd) {
            Player newPlayer = new Player(this, inJson);
            players.put(newPlayer.getName(), newPlayer);
            System.out.println("Whoa added player! His name is " + newPlayer.getName());
        } else players.remove(inJson.getString("n"));
    }

    public void removePlayer(String name) {
        players.remove(name);
    }
}
