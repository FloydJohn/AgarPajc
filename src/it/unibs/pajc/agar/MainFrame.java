package it.unibs.pajc.agar;

import javax.swing.*;

class MainFrame {

    public static void main(String[] args) {
        JFrame frame = new JFrame("AgarUnibs");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(GameController.getInstance());
        frame.setMinimumSize(GameController.getInstance().getMinimumSize());
        frame.setResizable(false);
        frame.pack();
        frame.addKeyListener(GameController.getInstance());
        frame.getContentPane().addMouseMotionListener(GameController.getInstance());
        frame.setVisible(true);
    }
}
