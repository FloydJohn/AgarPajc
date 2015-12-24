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
            if (f.getCurrentState() == Food.State.REMOVED) {
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
                addedFood = new JSONArray();

        for (Player p : players.values()) playersJson.put(p.toJSON());
        for (Food f : foods.values()) {
            switch (f.getCurrentState()) {
                case TO_ADD:
                    addedFood.put(f.toJSON());
                    f.setCurrentState(Food.State.ADDED);
                    break;
                case TO_REMOVE:
                    eatenFood.put(f.toJSON());
                    f.setCurrentState(Food.State.REMOVED);
                    break;
            }
        }

        JSONObject out = new JSONObject().put("p", playersJson);
        if (eatenFood.length() > 0) out.put("e", eatenFood);
        if (addedFood.length() > 0) out.put("a", addedFood);
        return out;
    }

    //Client
    public void fromJSON(JSONObject jsonObject) throws IllegalArgumentException {
        try {

            JSONArray playersJson = jsonObject.getJSONArray("p");
            for (Object element : playersJson) {
                JSONObject playerJson = (JSONObject) element;
                if (players.containsKey(playerJson.getString("n"))) {
                    players.get(playerJson.getString("n")).fromJSON(playerJson);
                } else {
                    Player newPlayer = new Player(this, playerJson);
                    players.put(newPlayer.getName(), newPlayer);
                }
            }
            for (Iterator<Player> iterator = players.values().iterator(); iterator.hasNext(); ) {
                Player p = iterator.next();
                boolean present = false;
                for (Object element : playersJson)
                    if (((JSONObject) element).getString("n").equals(p.getName())) present = true;
                if (!present) iterator.remove();
            }

            if (jsonObject.has("e"))
                eatFoods(jsonObject.getJSONArray("e"));
            if (jsonObject.has("a"))
                for (Object foodEaten : jsonObject.getJSONArray("a")) {
                    Food newFood = new Food(this, new Point2D.Float(), -1);
                    newFood.fromJSON((JSONObject) foodEaten);
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
}
