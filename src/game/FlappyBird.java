package game;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.geom.AffineTransform;


public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 1920;
    int boardHeight = 1080;

    private GameMenu gameMenu;

    Image backgroundImg;
    Image groundImg;
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
    int pipeHeight = 420;

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
    Bird bird;
    int velocityX = -10;
    int velocityY = 10;
    int gravity = 1;

      // Sistema de velocidade progressiva
    private double baseVelocity = -10; // Velocidade inicial
    private double currentVelocity = -10; // Velocidade atual
    private double velocityIncrement = 1.5; // Incremento a cada 20 pontos
    private int pointsForIncrement = 20; // Pontos necessários para cada aumento
    private int maxPointsForSpeed = 300; // Pontos máximos para aumento de velocidade
    private int lastSpeedIncreaseScore = 0; // Última pontuação em que a velocidade aumentou
    private int backgroundOffsetX = 0; // Posição X do fundo
    private int bgWidth; // Largura do fundo para repetição (variável de instância)

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false;
    double score = 0;
    private int highScore;
    boolean canRestart = false;
    private Clip Passagem_cano;

    FlappyBird(GameMenu gameMenu, int initialHighScore) 
{
        this.gameMenu = gameMenu;
        this.highScore = initialHighScore;
        
        // Inicializa bgWidth para ser acessível em move()
        // A largura do fundo será a largura da imagem original (608px) para repetição
        this.bgWidth = 608; // Largura da imagem de fundo noturno para repetição

        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        //load images
        backgroundImg = new ImageIcon(getClass().getResource("/resources/backgrounds/flappy_bird_night_bg.png")).getImage(); // Nova imagem de fundo noturno
        groundImg = new ImageIcon(getClass().getResource("/resources/backgrounds/flappy_bird_ground.png")).getImage(); // Carrega a imagem do chão
        birdImg = new ImageIcon(getClass().getResource("/resources/birds/flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("/resources/pipes/toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("/resources/pipes/bottompipe.png")).getImage();

        som_de_passagem();

        //bird
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();


        //place pipes timer
        placePipeTimer = new Timer(1500, new ActionListener() {
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
        int openingSpace = (int) (boardHeight/3.5); // Ajustado para aproximadamente 29% da altura da tela (vão equilibrado)
        
        // Calcula a altura do chão (onde o chão verde listrado começa)
        int groundY = (int) (boardHeight * 0.83); // 83% da altura da tela
        
        // Limita a altura do cano inferior para não ultrapassar o chão
        int maxBottomPipeY = groundY - pipeHeight;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        
        // Se o cano inferior ultrapassar o chão, ajusta o cano superior para cima
        if (bottomPipe.y > maxBottomPipeY) {
            int overflow = bottomPipe.y - maxBottomPipeY;
            topPipe.y -= overflow;
            bottomPipe.y = maxBottomPipeY;
        }
        
        pipes.add(topPipe);
        pipes.add(bottomPipe);
    }


    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) 
{
        //background

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        // Desenha o fundo (céu/montanhas/chão) com repetição horizontal e proporção correta
        // A imagem de fundo tem 608x457 pixels (proporção 1.33:1)
        // Para manter a proporção, a altura deve ser: 608 * (457/608) = 457 pixels
        // Mas vamos escalar para a altura da tela: boardHeight
        // Então a largura escalada será: boardHeight * (608/457) = boardHeight * 1.33
    int bgHeight = this.boardHeight;
    int scaledBgWidth = (int) (bgHeight * (608.0 / 457.0));

    int bgW = scaledBgWidth;
    int bgH = bgHeight;

    int x1 = backgroundOffsetX;
    int x2 = backgroundOffsetX + bgW;

    g.drawImage(backgroundImg, x1, 0, bgW, bgH, null);
    g.drawImage(backgroundImg, x2, 0, bgW, bgH, null);

    if (backgroundOffsetX <= -bgW) {
        backgroundOffsetX = 0;
    }
   
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

        //pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
            g.setColor(Color.RED);
            g.drawRect(pipe.x, pipe.y, pipe.width, pipe.height);
            
        }

        //score
        g.setColor(Color.white);
        g.setFont(loadCustomFont("/resources/fonts/Flappy-Bird.ttf", 70f));
        

if (gameOver) {

    int currentScore = (int) this.score;
    int finalHighScore= Math.max(currentScore,highScore);
        
    int boxWidth = 700;
    int boxHeight = 400;
    int boxX =  400;
    int boxY =  200;

            // 1. Fundo semi-transparente para escurecer a tela
            g.setColor(new Color(0, 0, 0, 180)); 
            g.fillRect(0, 0, boardWidth, boardHeight);

            // 2. Caixa branca do Placar
            g.fillRect(boxX, boxY, boxWidth, boxHeight);
            
            // Borda da caixa
            g.setColor(Color.BLACK);
            g.drawRect(boxX, boxY, boxWidth, boxHeight);
            
            // 3. Título GAME OVER (Centralizado)
            String text = "GAME OVER";
            g.setColor(Color.RED);
            Font goFont = loadCustomFont("/resources/fonts/FlappybirdyRegular-KaBW.ttf", 100f);
            g.setFont(goFont);
            
            FontMetrics fm = g.getFontMetrics(goFont);
            int textX = boxX + (boxWidth - fm.stringWidth(text)) / 2;
            int textY = boxY + 80;
            g.drawString(text, textX, textY);
            
            // 4. Pontuação Atual (SCORE)
            String scoreText = "SCORE: " + currentScore;
            g.setColor(Color.BLACK);
            Font scoreFont = loadCustomFont("/resources/fonts/Flappy-Bird.ttf", 50f);
            g.setFont(scoreFont);
            
            fm = g.getFontMetrics(scoreFont);
            textX = boxX + (boxWidth - fm.stringWidth(scoreText)) / 2;
            textY = boxY + 180;
            g.drawString(scoreText, textX, textY);
            
            // 5. Melhor Pontuação (BEST)
            String highText = "BEST: " + finalHighScore;
            g.setColor(Color.BLUE);
            g.setFont(scoreFont);
            
            fm = g.getFontMetrics(scoreFont);
            textX = boxX + (boxWidth - fm.stringWidth(highText)) / 2;
            textY = boxY + 250;
            g.drawString(highText, textX, textY);

            // 6. Instrução para Reiniciar
            String restartText = "Press SPACE to return to Menu";
            g.setColor(Color.GRAY);
            Font restartFont = loadCustomFont("/resources/fonts/Flappy-Bird.ttf", 25f);
            g.setFont(restartFont);
            
            fm = g.getFontMetrics(restartFont);
            textX = boxX + (boxWidth - fm.stringWidth(restartText)) / 2;
            textY = boxY + 310;
            g.drawString(restartText, textX, textY);

        } else {
        g.drawString(String.valueOf((int) score), 10, 35);
        }

 }




    


    public void move() {

        updateGameSpeed();
        backgroundOffsetX += (int) currentVelocity; // Move o fundo na mesma velocidade dos canos

        // Se passou do tamanho da tela, reinicia (efeito loop)
        // O loop deve ser baseado na largura escalada da imagem de fundo
        int scaledBgWidth = (int) (boardHeight * (608.0 / 457.0)); // Largura escalada mantendo a proporção
        if (backgroundOffsetX < -scaledBgWidth) {
             backgroundOffsetX = 0;
}
        //bird
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        //pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += (int) currentVelocity;

            if (pipe.x + pipe.width < 0) {
                     pipes.remove(i);
                    i--;
            }

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5;
                pipe.passed = true;
                som_Passagem();
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        // Colisão com o chão (base)
        // O chão verde listrado começa em aproximadamente 83% da altura da tela
        int groundY = (int) (boardHeight * 0.83); // Aproximadamente onde o chão começa
        
        if (bird.y + bird.height > groundY) {
            gameOver = true;
        }
        
        // Colisão com o topo da tela (opcional, mas bom para evitar que o pássaro suma)
        if (bird.y < 0) {
            bird.y = 0;
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
            canRestart = false;
        }
    }

    public void startGame() {
        bird.y = birdY;
        velocityY = 0;
        pipes.clear();
        gameOver = false;
        score = 0;


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
public void keyPressed(KeyEvent e) 
{
if (e.getKeyCode() == KeyEvent.VK_SPACE)
 {

    // Se perdeu, só permita restart se o jogador apertar espaço DE NOVO
    if (gameOver) {
        return; // evita sair do menu imediatamente
    }

    velocityY = -9;
}
}



        private Font loadCustomFont(String fontPath, float size) 
    {
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
        
    

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
public void keyReleased(KeyEvent e) 
{
   if (gameOver && e.getKeyCode() == KeyEvent.VK_SPACE) {
    int finalScore = (int) score;
    if(finalScore > highScore)
    {
        highScore= finalScore;
    }
    canRestart = true;
    gameMenu.returnToMenu((int)score);  // só volta quando o jogador APERTA de novo 
}
}
}

