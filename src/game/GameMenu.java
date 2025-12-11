package game;

import model.BirdSkin;
import util.SaveManager;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class GameMenu extends JPanel {
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 50;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private ArrayList<BirdSkin> skins;
    private FlappyBird gamePanel;
    private int money = 0;
    private int highScore = 0;
    private BirdSkin currentSkin;
    private ImageIcon coinIcon;
    private JLabel menuMoneyLabel;


    public GameMenu(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        playMusicLoop();



        // Initialize skins
        skins = new ArrayList<>();
        skins.add(new BirdSkin("Normal","flappybird.png",0));
        skins.add(new BirdSkin("Pacman", "PACMAN.png", 20));
        currentSkin = skins.get(0);
        currentSkin.unlock();

        // Load saved data
        try {
            SaveManager.SaveData data = SaveManager.load();
            money = data.money;
            data.highScore = highScore;
            if (data.unlockedCSV != null && data.unlockedCSV.length() > 0) {
                String[] unlocked = data.unlockedCSV.split(",");
                for (String name : unlocked) {
                    String trimmed = name.trim();
                    for (BirdSkin s : skins) {
                        if (s.getName().equalsIgnoreCase(trimmed)) s.unlock();
                    }
                }
            }
            if (data.currentSkinName != null) {
                for (BirdSkin s : skins) {
                    if (s.getName().equalsIgnoreCase(data.currentSkinName)) {
                        currentSkin = s;
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            // ignore
        }

        createMenuPanel();
        createShopPanel();
        createGamePanel(width, height);

        cardLayout.show(mainPanel, "menu");
    }

    public class BackgroundPanel extends JPanel {

    private Image backgroundImage;

    public BackgroundPanel(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
        setLayout(new BorderLayout());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }
}

    private void createMenuPanel() {
        Image bg = new ImageIcon(getClass().getResource("/resources/backgrounds/flappy_bird_night_bg.png")).getImage();
        BackgroundPanel menuPanel = new BackgroundPanel(bg);

        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        menuPanel.add(Box.createVerticalGlue());

        // Add title text
        JLabel title = new JLabel("Flappy Bird");
        title.setFont(loadCustomFont("/resources/fonts/FlappybirdyRegular-KaBW.ttf", 150f));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuPanel.add(title);

        menuPanel.add(Box.createVerticalStrut(24));

        JButton playButton = createCenteredButton("Play");
        JButton shopButton = createCenteredButton("Shop");

        playButton.addActionListener(e -> startGame());
        shopButton.addActionListener(e -> cardLayout.show(mainPanel, "shop"));

        menuPanel.add(playButton);
        menuPanel.add(Box.createVerticalStrut(12));
        menuPanel.add(shopButton);

        menuPanel.add(Box.createVerticalStrut(24));

        JLabel moneyLabel;
        if (coinIcon != null) {
            moneyLabel = new JLabel(String.valueOf(money), coinIcon, JLabel.CENTER);
            moneyLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
            moneyLabel.setIconTextGap(8);
        } else {
            moneyLabel = new JLabel(String.valueOf(money));
        }
        moneyLabel.setFont(loadCustomFont("/resources/fonts/Flappy-Bird.ttf", 40f));
        moneyLabel.setForeground(Color.YELLOW);
        moneyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuPanel.add(moneyLabel);

        menuMoneyLabel = moneyLabel;


        menuPanel.add(Box.createVerticalGlue());
        mainPanel.add(menuPanel, "menu");
    }

    private void createShopPanel() {
         Image bg = new ImageIcon(getClass().getResource("/resources/backgrounds/flappy_bird_night_bg.png")).getImage();
        BackgroundPanel shopPanel = new BackgroundPanel(bg);
        shopPanel.setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(null);
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Shop");
        title.setFont(loadCustomFont("/resources/fonts/Flappy-Bird.ttf", 55f));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(title);
        headerPanel.add(Box.createVerticalStrut(16));

        JLabel moneyLabel;
        if (coinIcon != null) {
            moneyLabel = new JLabel(String.valueOf(money), coinIcon, JLabel.CENTER);
            moneyLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
            moneyLabel.setIconTextGap(8);
        } else {
            moneyLabel = new JLabel(String.valueOf(money));
        }
        moneyLabel.setFont(loadCustomFont("/resources/fonts/Flappy-Bird.ttf", 55f));
        moneyLabel.setForeground(Color.WHITE);
        moneyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(moneyLabel);
        headerPanel.add(Box.createVerticalStrut(24));

        shopPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel skinsListPanel = new JPanel();
        skinsListPanel.setLayout(new BoxLayout(skinsListPanel, BoxLayout.Y_AXIS));
        skinsListPanel.setBackground(null);
        skinsListPanel.setOpaque(false);
        skinsListPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        for (BirdSkin skin : skins) {
            JPanel skinPanel = new JPanel();
            skinPanel.setLayout(new BoxLayout(skinPanel, BoxLayout.X_AXIS));
            skinPanel.setBackground(new Color(0,0, 0));
            skinPanel.setOpaque(true);
            skinPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(skin == currentSkin ? Color.YELLOW : Color.WHITE, 2),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            skinPanel.setMaximumSize(new Dimension(400, 60));

            try {
                ImageIcon skinIcon = new ImageIcon(getClass().getResource("/resources/birds/" + skin.getImagePath()));
                Image scaledImage = skinIcon.getImage().getScaledInstance(40, 30, Image.SCALE_SMOOTH);
                JLabel previewLabel = new JLabel(new ImageIcon(scaledImage));
                skinPanel.add(previewLabel);
                skinPanel.add(Box.createHorizontalStrut(12));
            } catch (Exception ex) {
                // Ignore
            }

            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setBackground(null);
            infoPanel.setOpaque(false);

            JLabel nameLabel = new JLabel(skin.getName());
            nameLabel.setForeground(Color.WHITE);
            nameLabel.setFont(loadCustomFont("/resources/fonts/FlappybirdyRegular-KaBW.ttf", 30f));
            infoPanel.add(nameLabel);

            JLabel priceLabel;
            if (skin.isUnlocked()) {
                priceLabel = new JLabel("(Owned)");
                priceLabel.setForeground(Color.GREEN);
            } else {
                if (coinIcon != null) {
                    Image smallCoin = coinIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                    priceLabel = new JLabel(String.valueOf(skin.getPrice()), new ImageIcon(smallCoin), JLabel.LEFT);
                    priceLabel.setIconTextGap(4);
                } else {
                    priceLabel = new JLabel(String.valueOf(skin.getPrice()));
                }
                priceLabel.setForeground(Color.WHITE);
            }
            priceLabel.setFont(loadCustomFont("/resources/fonts/Flappy-Bird.ttf",15f));
            infoPanel.add(priceLabel);

            skinPanel.add(infoPanel);
            skinPanel.add(Box.createHorizontalGlue());

            JButton actionButton = new JButton(skin.isUnlocked() ? "Select" : "Buy");
            actionButton.setFocusable(false);
            actionButton.setPreferredSize(new Dimension(80, 30));

            if (skin == currentSkin) {
                actionButton.setEnabled(false);
                actionButton.setText("Selected");
            }

            actionButton.addActionListener(e -> {
                if (skin.isUnlocked()) {
                    for (Component c : skinsListPanel.getComponents()) {
                        if (c instanceof JPanel) {
                            JPanel panel = (JPanel)c;
                            panel.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(Color.WHITE, 2),
                                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                            ));
                            for (Component bc : panel.getComponents()) {
                                if (bc instanceof JButton) {
                                    JButton btn = (JButton)bc;
                                    btn.setEnabled(true);
                                    if (btn.getText().equals("Selected")) {
                                        btn.setText("Select");
                                    }
                                }
                            }
                        }
                    }

                    this.currentSkin = skin;
                    gamePanel.changeBirdSkin(skin.getImagePath());
                    skinPanel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Color.YELLOW, 2),
                            BorderFactory.createEmptyBorder(8, 12, 8, 12)
                    ));
                    actionButton.setEnabled(false);
                    actionButton.setText("Selected");

                    JOptionPane.showMessageDialog(this, "Selected " + skin.getName());
                    SaveManager.save(money, skins, currentSkin.getName(),highScore);
                } else if (money >= skin.getPrice()) {
                    money -= skin.getPrice();
                    skin.unlock();
                    this.currentSkin = skin;
                    gamePanel.changeBirdSkin(skin.getImagePath());

                    priceLabel.setText("(Owned)");
                    priceLabel.setForeground(Color.GREEN);
                    actionButton.setText("Selected");
                    actionButton.setEnabled(false);
                    skinPanel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Color.YELLOW, 2),
                            BorderFactory.createEmptyBorder(8, 12, 8, 12)
                    ));

                    moneyLabel.setText(String.valueOf(money));
                    SaveManager.save(money, skins, currentSkin.getName(),highScore);
                    JOptionPane.showMessageDialog(this, "Purchased " + skin.getName() + "!");
                } else {
                    JOptionPane.showMessageDialog(this, "Not enough coins! Need " + skin.getPrice());
                }
            });

            skinPanel.add(actionButton);
            skinsListPanel.add(skinPanel);
            skinsListPanel.add(Box.createVerticalStrut(10));
        }

        JScrollPane scrollPane = new JScrollPane(skinsListPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        shopPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));


        JButton backButton = createCenteredButton("Back to Menu");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "menu"));
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        footerPanel.add(Box.createVerticalStrut(10));
        footerPanel.add(backButton);
        footerPanel.add(Box.createVerticalStrut(10));

        shopPanel.add(footerPanel, BorderLayout.SOUTH);
        mainPanel.add(shopPanel, "shop");
    }

    private void createGamePanel(int width, int height) {
        gamePanel = new FlappyBird(this,highScore);
        mainPanel.add(gamePanel, "game");
    }

private JButton createCenteredButton(String text) {
    JButton button = new JButton(text);
    button.setFont(loadCustomFont("/resources/fonts/FlappybirdyRegular-KaBW.ttf", 40f));
    button.setMaximumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
    button.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
    button.setAlignmentX(Component.CENTER_ALIGNMENT);
    button.setFocusPainted(false);
    button.setBackground(new Color(255, 255, 255));
    button.setForeground(Color.BLACK);
    return button;
}

private void stopMusic() {
    if (musicClip != null && musicClip.isRunning()) {
        musicClip.stop();
    }
}

private void restartMusic() {
    if (musicClip != null) {
        musicClip.setFramePosition(0); // volta ao início
        musicClip.loop(Clip.LOOP_CONTINUOUSLY);
        musicClip.start();
    }
}



    private Font loadCustomFont(String fontPath, float size) {
        try {
            java.net.URL fontUrl = getClass().getResource(fontPath);
            if (fontUrl != null) {
                Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontUrl.openStream());
                return customFont.deriveFont(size);
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar fonte: " + e.getMessage());
        }
        return new Font("Arial", Font.BOLD, (int)size);
    }

    public void startGame() {
        stopMusic();
        cardLayout.show(mainPanel, "game");
        gamePanel.requestFocus();
        gamePanel.startGame();
    }

    private Clip musicClip;


private void playMusicLoop() {
    try {
        InputStream is = getClass().getResourceAsStream("/resources/audio/MUSICA TESTE.wav");

        if (is == null) {
            is = getClass().getResourceAsStream("/audio/MUSICA TESTE.wav");
        }

        if (is == null) {
            throw new RuntimeException("Arquivo de áudio não encontrado!");
        }

        AudioInputStream audio = AudioSystem.getAudioInputStream(new BufferedInputStream(is));

        musicClip = AudioSystem.getClip();
        musicClip.open(audio);
        musicClip.loop(Clip.LOOP_CONTINUOUSLY);
        musicClip.start();

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    

public void returnToMenu(int scoreEarned) {
    money += scoreEarned;
    if (scoreEarned > highScore) {
        highScore = scoreEarned;
    }
    if (menuMoneyLabel != null) menuMoneyLabel.setText(String.valueOf(money));
    SaveManager.save(money, skins, currentSkin == null ? "Normal" : currentSkin.getName(),highScore);
    restartMusic();
    cardLayout.show(mainPanel, "menu");
}

    public int getMoney() {
        return money;
    }
}
