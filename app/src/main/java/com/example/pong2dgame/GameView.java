package com.example.pong2dgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements Runnable {

    private Thread gameThread;
    private boolean isPlaying = true;
    private boolean isPaused = false;

    private float ballX, ballY, ballRadius = 20;
    private float ballSpeedX = 12, ballSpeedY = 8;

    private float paddleWidth = 20, paddleHeight = 200;
    private float playerPaddleY, aiPaddleY;
    private float playerPaddleX = 50;
    private float aiPaddleX;
    private float paddleSpeed = 15;

    private int screenWidth, screenHeight;
    private int score = 0, highScore = 0, lives = 3;

    private long startTime;
    private long pausedTimeOffset = 0;

    private final Handler handler = new Handler();
    private Paint paint;
    private SurfaceHolder holder;

    public interface UIUpdater {
        void updateScore(int score, int highScore, int lives);
        void updateTime(String timeText);
    }

    private UIUpdater uiUpdater;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        paint = new Paint();
    }

    public void setUIUpdater(UIUpdater updater) {
        this.uiUpdater = updater;
    }

    @Override
    public void run() {
        startTime = System.currentTimeMillis();

        while (isPlaying) {
            if (!holder.getSurface().isValid() || isPaused) continue;

            update();
            draw();
            updateTimer();

            try {
                Thread.sleep(16); // ~60 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void resumeGame() {
        isPaused = false;
        if (gameThread == null || !gameThread.isAlive()) {
            gameThread = new Thread(this);
            isPlaying = true;
            gameThread.start();
        }
        startTime = System.currentTimeMillis() - pausedTimeOffset;
    }

    public void pauseGame() {
        isPaused = true;
        pausedTimeOffset = System.currentTimeMillis() - startTime;
    }

    public boolean isPaused() {
        return isPaused;
    }

    private void update() {
        ballX += ballSpeedX;
        ballY += ballSpeedY;

        // Ball-wall (top/bottom) collision
        if (ballY < 0 || ballY > screenHeight) ballSpeedY *= -1;

        // Ball-player paddle collision (left)
        if (ballX - ballRadius <= playerPaddleX + paddleWidth &&
                ballY >= playerPaddleY &&
                ballY <= playerPaddleY + paddleHeight) {
            ballSpeedX *= -1;
            score++;
            if (score > highScore) highScore = score;
            updateScore();
        }

        // Ball-AI paddle collision (right)
        if (ballX + ballRadius >= aiPaddleX &&
                ballY >= aiPaddleY &&
                ballY <= aiPaddleY + paddleHeight) {
            ballSpeedX *= -1;
        }

        // Ball missed by player
        if (ballX < 0) {
            lives--;
            if (lives <= 0) {
                resetGame();
            } else {
                resetBall();
            }
            updateScore();
        }

        // Ball missed by AI - reward point
        if (ballX > screenWidth) {
            score++;
            resetBall();
            updateScore();
        }

        // Simple AI movement
        if (ballY < aiPaddleY + paddleHeight / 2) {
            aiPaddleY -= paddleSpeed;
        } else {
            aiPaddleY += paddleSpeed;
        }

        // Clamp AI paddle within screen
        if (aiPaddleY < 0) aiPaddleY = 0;
        if (aiPaddleY + paddleHeight > screenHeight) aiPaddleY = screenHeight - paddleHeight;
    }

    private void draw() {
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) return;

        canvas.drawColor(Color.BLACK);

        paint.setColor(Color.WHITE);

        // Ball
        canvas.drawCircle(ballX, ballY, ballRadius, paint);

        // Player paddle (left)
        canvas.drawRect(playerPaddleX, playerPaddleY, playerPaddleX + paddleWidth,
                playerPaddleY + paddleHeight, paint);

        // AI paddle (right)
        canvas.drawRect(aiPaddleX, aiPaddleY, aiPaddleX + paddleWidth,
                aiPaddleY + paddleHeight, paint);

        holder.unlockCanvasAndPost(canvas);
    }

    private void resetGame() {
        score = 0;
        lives = 3;
        resetBall();
    }

    private void resetBall() {
        ballX = screenWidth / 2f;
        ballY = screenHeight / 2f;
        ballSpeedX = (Math.random() > 0.5 ? 12 : -12);
        ballSpeedY = (Math.random() > 0.5 ? 8 : -8);
    }

    private void updateScore() {
        if (uiUpdater != null) {
            uiUpdater.updateScore(score, highScore, lives);
        }
    }

    private void updateTimer() {
        long elapsed = System.currentTimeMillis() - startTime;
        int seconds = (int) (elapsed / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;

        String timeStr = String.format("Time: %02d:%02d", minutes, seconds);
        if (uiUpdater != null) {
            uiUpdater.updateTime(timeStr);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
            playerPaddleY = event.getY() - paddleHeight / 2;
            if (playerPaddleY < 0) playerPaddleY = 0;
            if (playerPaddleY + paddleHeight > screenHeight)
                playerPaddleY = screenHeight - paddleHeight;
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        screenWidth = w;
        screenHeight = h;

        playerPaddleY = (h - paddleHeight) / 2;
        aiPaddleY = (h - paddleHeight) / 2;
        aiPaddleX = w - 50 - paddleWidth;

        resetBall();
        super.onSizeChanged(w, h, oldw, oldh);
    }
}
