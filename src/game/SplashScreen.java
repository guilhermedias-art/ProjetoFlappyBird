package game;

import javax.swing.*;

public class SplashScreen extends JWindow {

    public SplashScreen() {
        java.net.URL imageUrl = getClass().getResource("/ui/loading.gif");

        if (imageUrl == null) {
            System.err.println("AVISO: Arquivo loading.gif não encontrado em /ui/loading.gif");
            JLabel label = new JLabel("Carregando...");
            add(label);
        } else {
            ImageIcon gif = new ImageIcon(imageUrl);
            JLabel label = new JLabel(gif);
            add(label);
        }

        pack();
        setLocationRelativeTo(null);
    }

    public void showSplash(int durationMs) {
        setVisible(true);
        try {
            Thread.sleep(durationMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Splash screen interrompida: " + e.getMessage());
        }
        setVisible(false);
        dispose();
    }
}