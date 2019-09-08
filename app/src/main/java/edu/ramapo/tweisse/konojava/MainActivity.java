package edu.ramapo.tweisse.konojava;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

/**
 * The first activity to launch when the application is started. Allows the user to load an existing
 * tournament, or start a new one.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * No activity specific setup here.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Called when user taps the 'Start New Tournament' button.
     * @param view A reference to the start tournament button that was pressed.
     */
    public void StartTournament(View view) {
        // Launch the new tournament activity.
        Intent intent = new Intent(this, NewTournamentActivity.class);
        intent.addFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    /**
     * Called when a user taps the 'Load Saved Tournament' button.
     * @param view A reference to the load tournament button that was pressed.
     */
    public void LoadTournament(View view) {
        // Launch the load tournament activity.
        Intent intent = new Intent(this, LoadTournamentActivity.class);
        intent.addFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    public static void main(String args[]){
    }
}