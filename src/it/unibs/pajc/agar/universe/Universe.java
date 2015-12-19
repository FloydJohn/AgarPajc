package it.unibs.pajc.agar.universe;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class Universe {

    private HashMap<String, Player> players = new HashMap<>();
    private HashMap<Integer, Food> foods = new HashMap<>();

    private int currentFoodId = 0;
    private Dimension universeDimension;
    private final Random generator = new Random();
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

            foods.put(currentFoodId++, new Food(this, pos));
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
            if (player.intersects(f)) {
                player.eat(f);
                iterator.remove();
            }
        }
    }

    public Player getPlayer() {
        return player;
    }
}
