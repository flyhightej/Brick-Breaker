package com.example.brickbreaker;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ImageButton playButton = findViewById(R.id.playButton);
        Animation verticalVibration = AnimationUtils.loadAnimation(this, R.anim.vibrate_animation);
        playButton.startAnimation(verticalVibration);

    }

    public void startGame(View view){

        EditText playerNameInput = findViewById(R.id.playerNameInput);
        String playerName = playerNameInput.getText().toString().trim();

        // Validate that the player entered a name
        if (playerName.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pass the player's name to the custom GameView
        GameView gameView = new GameView(this, playerName); // Pass player name here
        setContentView(gameView);
    }
}