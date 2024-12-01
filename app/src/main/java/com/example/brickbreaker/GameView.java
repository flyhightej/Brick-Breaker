package com.example.brickbreaker;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

public class GameView extends View {

    Context context;
    float ballX, ballY;
    Velocity velocity = new Velocity(25, 32);
    Handler handler;
    final long UPDATE_MILLIS = 30;
    Runnable runnable;
    Paint playerPaint = new Paint();
    Paint textPaint = new Paint();
    Paint healthPaint = new Paint();
    Paint brickPaint = new Paint();
    float TEXT_SIZE = 120;
    float paddleX, paddleY;
    float oldX, oldPaddleX;
    int points = 0;
    int life = 3;
    Bitmap ball, paddle;
    int dWidth, dHeight;
    int ballWidth, ballHeight;
    MediaPlayer mpHit, mpMiss, mpBreak, mpWin, mpLoose;
    Random random;
    Brick[] bricks = new Brick[30];
    int numBricks = 0;
    int brokenBricks = 0;
    boolean gameOver = false;
    String playerName;
    DatabaseHelper dbHelper;

    public GameView(Context context, String playerName) {
        super(context);
        this.context = context;
        this.playerName = playerName;
        ball = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
        paddle = BitmapFactory.decodeResource(getResources(), R.drawable.paddle_bar);
        int desiredBallWidth = 150;   // Adjust to desired width in pixels
        int desiredBallHeight = 150;  // Adjust to desired height in pixels
        ball = Bitmap.createScaledBitmap(ball, desiredBallWidth, desiredBallHeight, true);
        int desiredPaddleWidth = 400;
        int desiredPaddleHeight = 100;
        paddle = Bitmap.createScaledBitmap(paddle, desiredPaddleWidth, desiredPaddleHeight, true);
        handler = new Handler();
        dbHelper = new DatabaseHelper(context); // Initialize with context
        runnable = new Runnable(){
            @Override
            public void run() {
                invalidate();
            }
        };
        mpHit = MediaPlayer.create(context, R.raw.hit);
        mpMiss = MediaPlayer.create(context, R.raw.miss);
        mpBreak = MediaPlayer.create(context, R.raw.breaking);
        mpWin =  MediaPlayer.create(context, R.raw.winning);
        mpLoose = MediaPlayer.create(context, R.raw.lost);
        playerPaint.setColor(Color.WHITE);
        playerPaint.setTextSize(80);
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setTextAlign(Paint.Align.LEFT);
        healthPaint.setColor(Color.GREEN);
        brickPaint.setColor(Color.argb(255,249,129,0));
        Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        dWidth = size.x;
        dHeight = size.y;
        random = new Random();
        ballX = random.nextInt(dWidth - 50);
        ballY = dHeight/3;
        paddleY = (float) ((dHeight * 4.7)/5);
        paddleX = dWidth/2 - paddle.getWidth()/2;
        ballWidth = ball.getWidth();
        ballHeight = ball.getHeight();
        createBricks();
    }

    private void createBricks() {
        int brickWidth = dWidth / 8;
        int brickHeight = dHeight  / 16;
        for (int column=0; column<8; column++){
            for(int row=0; row<3; row++){
                bricks[numBricks] = new Brick(row, column, brickWidth, brickHeight);
                numBricks++;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        canvas.drawColor(Color.BLACK);
        ballX += velocity.getX();
        ballY += velocity.getY();
        if((ballX >= dWidth - ball.getWidth()) || ballX <= 0){  // Reverse horizontal direction if the ball hits the left/right walls
              velocity.setX(velocity.getX() * -1);
        }
        if(ballY <= 0){  // Reverse vertical direction if the ball hits the top wall
            velocity.setY(velocity.getY() * -1);
        }
        if(ballY > paddleY + paddle.getHeight()) { // Ball misses paddle
            ballX = 1 + random.nextInt(dWidth - ball.getWidth() - 1); // Reset ball horizontally
            ballY = dHeight / 3; // Reset ball vertically
            if (mpMiss != null) {
              if (life > 1) {
                  mpMiss.start();
              }
            }
            velocity.setX(xVelocity());
            velocity.setY(32); // Reset vertical velocity
            life--;
            if (life == 0) {
                gameOver = true;
                launchGameOver();
                mpLoose.start();
            }
        }
            if (((ballX + ball.getWidth()) >= paddleX)
                    && (ballX <= paddleX + paddle.getWidth())  //Paddle-Ball Collision:
                    && (ballY + ball.getHeight() >= paddleY)
                    && (ballY + ball.getHeight() <= paddleY + paddle.getHeight())) {
                if (mpHit != null) {
                    mpHit.start();
                }
                velocity.setX(velocity.getX() + 1); // Slightly increase horizontal speed
                velocity.setY((velocity.getY() + 1) * -1); // Reverse vertical direction
            }
            canvas.drawBitmap(ball, ballX, ballY, null);
            canvas.drawBitmap(paddle, paddleX, paddleY, null);
            for (int i = 0; i < numBricks; i++) { //Drawing and Managing Bricks:
                if (bricks[i].getVisibility()) {
                    canvas.drawRect(bricks[i].column * bricks[i].width + 1, bricks[i].row * bricks[i].height + 1, bricks[i].column * bricks[i].width + bricks[i].width - 1, bricks[i].row * bricks[i].height + bricks[i].height - 1, brickPaint);
                }
            }
            canvas.drawText("Player: " + playerName, 440, 240, playerPaint);
            canvas.drawText("" + points, 40, TEXT_SIZE, textPaint);

            if (life == 2) {
                healthPaint.setColor(Color.YELLOW);
            } else if (life == 1) {
                healthPaint.setColor(Color.RED);
            }
            canvas.drawRect(dWidth - 200, 30, dWidth - 200 + 60 * life, 80, healthPaint);
            for (int i = 0; i < numBricks; i++) { //Ball-Brick Collision:
                if (bricks[i].getVisibility()) {
                    if (ballX + ballWidth >= bricks[i].column * bricks[i].width
                            && ballX <= bricks[i].column * bricks[i].width + bricks[i].width
                            && ballY <= bricks[i].row * bricks[i].height + bricks[i].height
                            && ballY >= bricks[i].row * bricks[i].height) {
                        if (mpBreak != null) {
                            mpBreak.start(); // Play "brick break" sound
                        }
                        velocity.setY((velocity.getY() + 1) * -1); // Reverse vertical direction
                        bricks[i].setInvisible(); // Hide the brick
                        points += 10;
                        brokenBricks++;
                        if (brokenBricks == 24) {
                            launchGameOver();
                        }
                    }
                }
            }
            if (brokenBricks == numBricks) {
                gameOver = true;
                mpWin.start();
            }
            if (!gameOver) {
                handler.postDelayed(runnable, UPDATE_MILLIS); // Schedule the next frame
            }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        if(touchY >= paddleY){
            int action = event.getAction();
            if(action == MotionEvent.ACTION_DOWN){
                oldX = event.getX();
                oldPaddleX = paddleX;
            }
            if (action == MotionEvent.ACTION_MOVE){
                float shift = oldX - touchX;  // Calculate the horizontal shift from the initial touch
                float newPaddleX = oldPaddleX - shift; // Determine the new paddle position based on the shift
                if(newPaddleX <=0 )
                    paddleX = 0; //Left Edge
                else if(newPaddleX >= dWidth - paddle.getWidth())
                    paddleX = dWidth - paddle.getWidth(); //Right Edge
                else
                    paddleX = newPaddleX; //Updates paddle position if within bounds
            }
        }
        return true;
    }

    private void launchGameOver() {
        handler.removeCallbacksAndMessages(null);

        //Delete Database
        //dbHelper.deleteAllScores();

        // Save score using playerName and points
        dbHelper.insertOrUpdateScore(playerName, points);

        Intent intent = new Intent(context, GameOver.class);
        intent.putExtra("player_name", playerName);
        intent.putExtra("points", points );

        context.startActivity(intent);
        ((Activity) context).finish();
    }

    private int xVelocity(){  //each time the ball resets (after missing the paddle) -> new random horizontal direction
        int[] values = {-35, -30, -25, 25, 30, 35};
        int index = random.nextInt(6);
        return values[index];
    }

}
