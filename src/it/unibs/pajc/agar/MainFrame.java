package it.unibs.pajc.agar;

import javax.swing.*;

public class MainFrame {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Agar Bs");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(GameController.getInstance());
        frame.setMinimumSize(GameController.getInstance().getMinimumSize());
        frame.setResizable(false);
        frame.pack();
        frame.addKeyListener(GameController.getInstance());
        frame.addMouseMotionListener(GameController.getInstance());
        frame.setVisible(true);
    }
}
