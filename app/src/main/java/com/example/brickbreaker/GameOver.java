package com.example.brickbreaker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import java.util.Map;

public class GameOver extends AppCompatActivity {

    TextView tvPoints; // Current score display
    TextView tvPlayer1Name, tvPlayer1Score;
    TextView tvPlayer2Name, tvPlayer2Score;
    TextView tvPlayer3Name, tvPlayer3Score;
    ImageView ivNewHighest;
    DatabaseHelper dbHelper;  // Shared DatabaseHelper instance

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_over); // Ensure this layout includes the top 3 TextViews

        // Initialize UI components
        ivNewHighest = findViewById(R.id.ivNewHighest);
        tvPoints = findViewById(R.id.tvPoints);
        tvPlayer1Name = findViewById(R.id.tvPlayer1Name);
        tvPlayer1Score = findViewById(R.id.tvPlayer1Score);
        tvPlayer2Name = findViewById(R.id.tvPlayer2Name);
        tvPlayer2Score = findViewById(R.id.tvPlayer2Score);
        tvPlayer3Name = findViewById(R.id.tvPlayer3Name);
        tvPlayer3Score = findViewById(R.id.tvPlayer3Score);

        // Retrieve the current score and player name from the Intent
        int currentScore = getIntent().getIntExtra("points", 0);
        String currentPlayerName = getIntent().getStringExtra("player_name");

        // Set the current score display
        tvPoints.setText(String.valueOf(currentScore));

        // Initialize DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Check if the current score qualifies for the top 3
        boolean isNewHighScore = dbHelper.updateIfTopThree(currentPlayerName, currentScore);

        // Retrieve and display the top 3 players
        List<Map.Entry<String, Integer>> topThreePlayers = dbHelper.getTopThreePlayers();
        displayTopThreePlayers(topThreePlayers);

        // Control visibility of the new high score indicator
        ivNewHighest.setVisibility(isNewHighScore ? View.VISIBLE : View.GONE);
    }

    private void displayTopThreePlayers(List<Map.Entry<String, Integer>> topThreePlayers) {
        // Check if the topThreePlayers list is empty
        if (topThreePlayers.isEmpty()) {

            tvPlayer1Name.setText("");
            tvPlayer1Score.setText("");

            tvPlayer2Name.setText("");
            tvPlayer2Score.setText("");

            tvPlayer3Name.setText("");
            tvPlayer3Score.setText("");
            return;
        }

        // Display the top players' information
        for (int i = 0; i < topThreePlayers.size(); i++) {
            Map.Entry<String, Integer> player = topThreePlayers.get(i);
            switch (i) {
                case 0:
                    tvPlayer1Name.setText(player.getKey());
                    tvPlayer1Score.setText(String.valueOf(player.getValue()));
                    break;
                case 1:
                    tvPlayer2Name.setText(player.getKey());
                    tvPlayer2Score.setText(String.valueOf(player.getValue()));
                    break;
                case 2:
                    tvPlayer3Name.setText(player.getKey());
                    tvPlayer3Score.setText(String.valueOf(player.getValue()));
                    break;
            }
        }
    }

    public void restart(View view){
        Intent intent = new Intent(GameOver.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void exit(View view){
        finish();
    }

}
