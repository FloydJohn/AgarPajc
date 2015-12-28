package it.unibs.pajc.agar.universe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class Universe {

    private final Random generator = new Random();
    private HashMap<String, Player> players = new HashMap<>();
    private HashMap<Integer, Food> foods = new HashMap<>();
    private int currentFoodId = 0;
    private Dimension universeDimension;
    private Player player;

    public Universe(String playerName, Dimension universeDimension) {
        this.universeDimension = universeDimension;
        player = new Player(playerName, true, new Point2D.Float(50,50), 30, Color.RED, this);
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

    public HashMap<Integer, Food> getFoods() {
        return foods;
    }

    public HashMap<String, Player> getPlayers() {
        return players;
    }

    public void update() {
        players.values().forEach(Player::update);
        for (Iterator<Food> iterator = foods.values().iterator(); iterator.hasNext(); ) {
            Food f = iterator.next();
            if (f.getCurrentState() == GameObject.State.REMOVED) {
                iterator.remove();
                continue;
            }
            if (player.intersects(f)) {
                player.eat(f);
                iterator.remove();
            }
        }
    }

    public Player getPlayer() {
        return player;
    }

    //Server
    public JSONObject toJSON() {
        JSONArray playersJson = new JSONArray(),
                eatenFood = new JSONArray(),
                addedFood = new JSONArray(),
                removedPlayers = new JSONArray();

        for (Player p : players.values()) {
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
            playersJson.put(p.toJSON());
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
                    Food newFood = new Food(this, new Point2D.Float(), -1);
                    newFood.fromJSON((JSONObject) foodAdded);
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
}
