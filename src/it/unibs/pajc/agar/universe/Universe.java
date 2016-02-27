package it.unibs.pajc.agar.universe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Universe {

    private static final int FOODS_NUMBER = 500;
    private final Random generator = new Random();
    private final boolean isServer;
    private final Dimension universeDimension = new Dimension(5000, 3000);
    private ConcurrentHashMap<String, Player> players = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Food> foods = new ConcurrentHashMap<>();
    private int currentFoodId = 0;
    private Player player;
    private JSONObject jsonData = new JSONObject();
    private ConcurrentLinkedQueue<Food> eatenFoods = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Player> eatenPlayers = new ConcurrentLinkedQueue<>();
    private boolean playerCanDie = true;

    public Universe(String playerName, boolean isServer) {
        player = new Player(playerName, new Point2D.Float(20, 50), 30, new Random().nextInt(Player.possibleColors.length), this);
        players.put(playerName, player);
        this.isServer = isServer;
        if (isServer) {
            generateRandomFood(FOODS_NUMBER);
            //Food regen
            new Timer(1000, e -> {
                if (foods.size() < FOODS_NUMBER) generateRandomFood(1);
            }).start();
        }
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
        for (Player p : players.values()) {
            if (p == player || !p.isAlive()) continue;
            if (player.intersects(p).equals(IntersectionType.THIS_EATS)) {
                System.out.println("[Universe#update] Eaten Player! Mass gained = " + p.getMass());
                player.eat(p.getMass());
                p.die();
                if (isServer) {
                    System.out.println("[Universe#update] I am server and ate " + p.getName());
                } else {
                    System.out.println("[Universe#update] I am client and ate " + p.getName());
                    eatenPlayers.add(p);
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
        players.values().stream().forEach(p -> playersJson.put(p.toJSON()));
        for (Food f : foods.values()) foodJson.put(f.toJSON());
        JSONObject out = new JSONObject().put("p", playersJson);
        out.put("f", foodJson);
        return out;
    }

    //Client
    public void fromJSON(JSONObject jsonObject) throws IllegalArgumentException {
        try {
            //Parse players
            JSONArray playersJson = jsonObject.getJSONArray("p");
            for (Iterator<Player> iterator = players.values().iterator(); iterator.hasNext(); ) {
                Player p = iterator.next();
                boolean present = false;
                for (Object element : playersJson) {
                    if (((JSONObject) element).getString("n").equals(p.getName())) {
                        present = true;
                        break;
                    }
                }
                if (!present && p != player) {
                    System.out.println("[Universe.fromJson] Removed " + p.getName() + " because it's not present.");
                    iterator.remove();
                }
            }

            for (Object element : playersJson) {
                JSONObject playerJson = (JSONObject) element;
                Player selected = getPlayer(playerJson.getString("n"));
                if (selected == player) {
                    if (!playerCanDie && playerJson.getInt("m") >= 0) playerCanDie = true;
                    if (player.isAlive() && playerCanDie && playerJson.getInt("m") < 0) player.die();
                    continue;    //Skips update if is this player
                }
                if (selected == null) updatePlayer(playerJson, true);
                else selected.fromJSON(playerJson);
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
            players.get((String) p).die();
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
        if (name == null) return;
        players.remove(name);
    }

    public JSONObject getJson() {
        return jsonData;
    }

    public int getCurrentFoodId() {
        return currentFoodId;
    }

    public void restartGame() {
        System.out.println("Restarting game!");
        playerCanDie = false;
        String playerName = player.getName();
        player = new Player(playerName, new Point2D.Float(20, 50), 30, new Random().nextInt(Player.possibleColors.length), this);
        players.put(playerName, player);
    }

    public boolean existsPlayer(String playerName) {
        for (String name : players.keySet())
            if (name.equals(playerName))
                return true;
        return false;
    }
}
