package com.example.brickbreaker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Information
    private static final String DATABASE_NAME = "player_scores.db";
    private static final int DATABASE_VERSION = 1;

    // Table and Column Names
    private static final String TABLE_SCORES = "scores";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_PLAYER_NAME = "player_name";
    private static final String COLUMN_SCORE = "score";

    // SQL query to create the scores table
    private static final String CREATE_TABLE_SCORES =
            "CREATE TABLE " + TABLE_SCORES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PLAYER_NAME + " TEXT UNIQUE, " +
                    COLUMN_SCORE + " INTEGER" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SCORES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORES);
        onCreate(db);
    }

    // Insert or update a player's score in the database
    public void insertOrUpdateScore(String playerName, int score) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PLAYER_NAME, playerName);
        values.put(COLUMN_SCORE, score);

        // Check if the player already exists and update only if necessary
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_SCORE + " FROM " + TABLE_SCORES +
                " WHERE " + COLUMN_PLAYER_NAME + " = ?", new String[]{playerName});
        if (cursor.moveToFirst()) {
            int existingScore = cursor.getInt(0);
            if (score > existingScore) {
                db.update(TABLE_SCORES, values, COLUMN_PLAYER_NAME + " = ?", new String[]{playerName});
            }
        } else {
            db.insert(TABLE_SCORES, null, values); // Insert new record if player doesn't exist
        }
        cursor.close();
        db.close();
    }

    // Retrieve the top three players from the database
    public List<Map.Entry<String, Integer>> getTopThreePlayers() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Map.Entry<String, Integer>> topThreePlayers = new ArrayList<>();

        String query = "SELECT " + COLUMN_PLAYER_NAME + ", " + COLUMN_SCORE +
                " FROM " + TABLE_SCORES + " ORDER BY " + COLUMN_SCORE + " DESC LIMIT 3";
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            String playerName = cursor.getString(0);
            int score = cursor.getInt(1);
            topThreePlayers.add(new HashMap.SimpleEntry<>(playerName, score));
        }

        cursor.close();
        db.close();
        return topThreePlayers;
    }

    // Check if the new score qualifies for the top three and update accordingly
    public boolean updateIfTopThree(String playerName, int newScore) {
        List<Map.Entry<String, Integer>> topThree = getTopThreePlayers();
        if (topThree.size() < 3 || newScore > topThree.get(topThree.size() - 1).getValue()) {
            insertOrUpdateScore(playerName, newScore);
            return true;
        }
        return false;
    }

    public void deleteAllScores() {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            // Deletes all rows in the player_scores table
            db.delete(TABLE_SCORES, null, null);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting all scores", e);
        }
    }
}
