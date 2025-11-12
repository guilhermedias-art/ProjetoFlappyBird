package game;

import javax.swing.*;

public class App {
    public static void main(String[] args) throws Exception {
        int boardWidth = 2400;
        int boardHeight = 1200;

        JFrame frame = new JFrame("Flappy Bird");
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GameMenu gameMenu = new GameMenu(boardWidth, boardHeight);
        frame.add(gameMenu);
        frame.pack();
        frame.setVisible(true);
    }
}