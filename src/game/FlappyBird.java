package game;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.geom.AffineTransform;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 2400;
    int boardHeight = 1200;

    private GameMenu gameMenu;

    //images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    //bird class
    int birdX = boardWidth/15;
    int birdY = boardHeight/10;
    int birdWidth = 100;
    int birdHeight = 100;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
            this.img = this.img.getScaledInstance(birdWidth, birdHeight, Image.SCALE_SMOOTH);
        }
    }

    //pipe class
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 120;
    int pipeHeight = 600;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    //game logic
  //game logic
    Bird bird;
    int velocityX = -10; // Velocidade base
    int velocityY = 10;
    int gravity = 1;

    // Sistema de velocidade progressiva
    private double baseVelocity = -10; // Velocidade inicial
    private double currentVelocity = -10; // Velocidade atual
    private double velocityIncrement = 2.0; // Incremento a cada 20 pontos
    private int pointsForIncrement = 20; // Pontos necessários para cada aumento
    private int maxPointsForSpeed = 200; // Pontos máximos para aumento de velocidade
    private int lastSpeedIncreaseScore = 0; // Última pontuação em que a velocidade aumentou
    private int backgroundOffsetX = 0; // Posição X do fundo
    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false;
    double score = 0;
    private Clip Passagem_cano;
    

    FlappyBird(GameMenu gameMenu) {
        this.gameMenu = gameMenu;
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        //load images
        backgroundImg = new ImageIcon(getClass().getResource("/resources/backgrounds/2151120915.jpg")).getImage();
        birdImg = new ImageIcon(getClass().getResource("/resources/birds/flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("/resources/pipes/toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("/resources/pipes/bottompipe.png")).getImage();
        
        som_de_passagem();

        //bird
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        //place pipes timer
        placePipeTimer = new Timer(800, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        placePipeTimer.start();

        //game timer
        gameLoop = new Timer(1000/60, this);
        gameLoop.start();
    }

    void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight/4 - Math.random()*(pipeHeight/2));
        int openingSpace = boardHeight/4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }


    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }


public void draw(Graphics g) {
        
        g.drawImage(backgroundImg, 0, 0, this.boardWidth, this.boardHeight, null);

       
        Graphics2D g2d = (Graphics2D) g;

        // Desenha o fundo movendo
g.drawImage(backgroundImg, backgroundOffsetX, 0, this.boardWidth, this.boardHeight, null);
// Desenha o fundo novamente ao lado pra preencher o espaço
g.drawImage(backgroundImg, backgroundOffsetX + boardWidth, 0, this.boardWidth, this.boardHeight, null);
   

        AffineTransform oldTransform = g2d.getTransform();

        
        double rotation = Math.toRadians(velocityY * 4.0);

        
        rotation = Math.max(Math.toRadians(-25), Math.min(rotation, Math.toRadians(90)));

        // --- 4. Define o Ponto de Rotação (o centro do pássaro) ---
        int centerX = bird.x + bird.width / 2;
        int centerY = bird.y + bird.height / 2;

        // --- 5. Aplica a Rotação ---
       
        g2d.rotate(rotation, centerX, centerY);

        // --- 6. Desenha o Pássaro (já rotacionado) ---
        
        g2d.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);

        // --- 7. Restaura o "pincel" ao estado original ---
        // Remove a rotação para que o resto seja desenhado normalmente
        g2d.setTransform(oldTransform);

        // --- 8. Desenha Canos e Placar (sem rotação) ---
        //pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        //score
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf((int) score), 10, 35);
        }
        else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
    }

    public void move() {
        // Atualiza velocidade baseada na pontuação
        updateGameSpeed();

        backgroundOffsetX += (int) currentVelocity; // Move o fundo na mesma velocidade dos canos

// Se passou do tamanho da tela, reinicia (efeito loop)
            if (backgroundOffsetX < -boardWidth) {
             backgroundOffsetX = 0;
}
        
        //bird
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        //pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += (int) currentVelocity; // Usa a velocidade atualizada

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5;
                pipe.passed = true;
                
                som_Passagem();
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }
    
    
    private void som_de_passagem() 
    {
    	
    	try {
            java.net.URL soundUrl = getClass().getResource("/resources/sounds/Passagem_bird.wav");
            if (soundUrl != null) {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundUrl);
                Passagem_cano = AudioSystem.getClip();
                Passagem_cano.open(audioInputStream);
                System.out.println("Som de passagem carregado com sucesso!");
            } else {
                System.out.println("Arquivo de som não encontrado: /resources/sounds/Passagem_bird.wav");
            }
        } catch (Exception e) {
            System.out.println("Erro ao carregar som: " + e.getMessage());
        }
    }
    	
    
    private void som_Passagem()
    {
        if (Passagem_cano != null) 
        {
            Passagem_cano.stop(); // Para o som se estiver tocando
            Passagem_cano.setFramePosition(0); // Volta para o início
            Passagem_cano.start(); // Toca o som
        }
    }
 

    // Nova função para controlar a velocidade progressiva
    private void updateGameSpeed() {
        int currentScore = (int) score;
        
        // Verifica se atingiu uma nova marca de 20 pontos e não ultrapassou o máximo
        if (currentScore >= lastSpeedIncreaseScore + pointsForIncrement && currentScore <= maxPointsForSpeed) {
            currentVelocity = baseVelocity - (velocityIncrement * (currentScore / pointsForIncrement));
            lastSpeedIncreaseScore = (currentScore / pointsForIncrement) * pointsForIncrement;
            
            System.out.println("Velocidade aumentada para: " + currentVelocity + " aos " + currentScore + " pontos");
        }
        
        // Se passou de 100 pontos, mantém a velocidade dos 100 pontos
        if (currentScore > maxPointsForSpeed && lastSpeedIncreaseScore < maxPointsForSpeed) {
            currentVelocity = baseVelocity - (velocityIncrement * (maxPointsForSpeed / pointsForIncrement));
            lastSpeedIncreaseScore = maxPointsForSpeed;
            System.out.println("Velocidade máxima atingida: " + currentVelocity);
        }
    }

    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            placePipeTimer.stop();
            gameLoop.stop();
            gameMenu.returnToMenu((int)score);
        }
    }

    public void startGame() {
        bird.y = birdY;
        velocityY = 0;
        pipes.clear();
        gameOver = false;
        score = 0;
        
        // Reseta o sistema de velocidade
        currentVelocity = baseVelocity;
        lastSpeedIncreaseScore = 0;
        
        gameLoop.start();
        placePipeTimer.start();
    }

    public void changeBirdSkin(String imagePath) {
        birdImg = new ImageIcon(getClass().getResource("/resources/birds/" + imagePath)).getImage();
        bird.img = birdImg;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9;

            if (gameOver) {
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                gameOver = false;
                score = 0;
                gameLoop.start();
                placePipeTimer.start();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
