package it.unibs.pajc.agar;

import javax.swing.*;

public class MainFrame {

    public static void main(String[] args) {
        JFrame frame = new JFrame("AgarPajc");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(GameController.getInstance());
        frame.setMinimumSize(GameController.getInstance().getMinimumSize());
        frame.setResizable(false);
        frame.pack();
        frame.getContentPane().addKeyListener(GameController.getInstance());
        frame.getContentPane().addMouseMotionListener(GameController.getInstance());
        frame.setVisible(true);
    }
}
