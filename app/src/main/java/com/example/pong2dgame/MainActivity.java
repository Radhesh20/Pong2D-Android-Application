package com.example.pong2dgame;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;
    private TextView textScore, textHighScore, textLives, textTime;
    private Button buttonPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textScore = findViewById(R.id.textScore);
        textHighScore = findViewById(R.id.textHighScore);
        textLives = findViewById(R.id.textLives);
        textTime = findViewById(R.id.textTime);
        buttonPause = findViewById(R.id.buttonPause);
        gameView = findViewById(R.id.gameView);

        buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (gameView.isPaused()) {
                    gameView.resumeGame();
                    buttonPause.setText("Pause");
                } else {
                    gameView.pauseGame();
                    buttonPause.setText("Resume");
                }
            }
        });

        gameView.setUIUpdater(new GameView.UIUpdater() {
            @Override
            public void updateScore(final int score, final int highScore, final int lives) {
                runOnUiThread(() -> {
                    textScore.setText("Score: " + score);
                    textHighScore.setText("High Score: " + highScore);
                    textLives.setText("Lives: " + lives);
                });
            }

            @Override
            public void updateTime(final String timeText) {
                runOnUiThread(() -> textTime.setText(timeText));
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pauseGame();
        buttonPause.setText("Resume");
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resumeGame();
        buttonPause.setText("Pause");
    }
}
