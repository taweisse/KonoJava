package edu.ramapo.tweisse.konojava;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Allows the user to configure a brand new tournament.
 */
public class NewTournamentActivity extends AppCompatActivity {

    /**
     * Overrides the back button so that we can have no back animation.
     */
    @Override
    public void onPause(){
        super.onPause();
        // Disable back animation.
        overridePendingTransition(0, 0);
    }

    /**
     * Fills various spinners with the correct values for the user to select. Hides portions of the UI by default.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_tournament);

        // Fill the board sizes spinner.
        String boardSizes[] = {"5", "7", "9"};
        Spinner boardSizeSpinner = findViewById(R.id.boardSizeSpinner);
        ArrayAdapter<String> adapter0 = new ArrayAdapter<>(NewTournamentActivity.this, android.R.layout.simple_spinner_item, boardSizes);
        adapter0.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        boardSizeSpinner.setAdapter(adapter0);

        // Fill the opponent type spinner.
        String playerTypes[] = {"Human", "Computer"};
        Spinner opponentTypeSpinner = findViewById(R.id.opponentTypeSpinner);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(NewTournamentActivity.this, android.R.layout.simple_spinner_item, playerTypes);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        opponentTypeSpinner.setAdapter(adapter1);

        // Fill the player color spinner.
        String playerColors[] = {"White", "Black"};
        Spinner playerColorSpinner = findViewById(R.id.playerColorSpinner);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(NewTournamentActivity.this, android.R.layout.simple_spinner_item, playerColors);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        playerColorSpinner.setAdapter(adapter2);

        // Hide the roll results until the user presses the roll dice button.
        LinearLayout diceResults = findViewById(R.id.diceResults);
        diceResults.setVisibility(View.INVISIBLE);

        // Get permissions if we don't have them.
        Serializer.VerifyStoragePermissions(this);
    }

    /**
     * Called when the roll dice button is clicked. Will choose which player goes first based on random
     * dice rolls.
     * @param view A reference to the roll dice button that was pressed.
     */
    public void RollDice(View view) {
        // Disable the roll dice button so we cant press it again.
        view.setEnabled(false);

        // Holds dice roll values for each player.
        int rolls[] = new int[4];

        // Loop until someone wins the dice roll.
        do {
            for (int i = 0; i < 4; i++){
                rolls[i] = Tournament.ThrowDice();
            }
        }
        while(rolls[0] + rolls[1] == rolls[2] + rolls[3]);

        // Show the user who rolled what.
        TextView curField = findViewById(R.id.p1r1);
        if (curField != null){
            curField.setText(Integer.toString(rolls[0]));
        }
        curField = findViewById(R.id.p1r2);
        if (curField != null){
            curField.setText(Integer.toString(rolls[1]));
        }
        curField = findViewById(R.id.p2r1);
        if (curField != null){
            curField.setText(Integer.toString(rolls[2]));
        }
        curField = findViewById(R.id.p2r2);
        if (curField != null){
            curField.setText(Integer.toString(rolls[3]));
        }

        // Tell the user which player won.
        String winner;
        if (rolls[0] + rolls[1] > rolls[2] + rolls[3]){
            winner = "1";
        }
        else {
            winner = "2";
        }
        curField = findViewById(R.id.rollWinner);
        if (curField != null){
            curField.setText(winner);
        }
        curField = findViewById(R.id.pickColorPlayer);
        if (curField != null){
            curField.setText(winner);
        }

        // The computer player should automatically pick a color.
        Spinner oppType = findViewById(R.id.opponentTypeSpinner);
        if (oppType != null && winner.equals("2") && oppType.getSelectedItem().toString().equals("Computer")){
            // Disable the player selector.
            oppType.setEnabled(false);

            Spinner playerColorSpinner = findViewById(R.id.playerColorSpinner);
            if (playerColorSpinner != null){
                playerColorSpinner.setSelection(0); // Choose the first item in the spinner. (White)
                playerColorSpinner.setEnabled(false);
            }
        }

        // Un-hide this section.
        LinearLayout diceResults = findViewById(R.id.diceResults);
        diceResults.setVisibility(View.VISIBLE);
    }

    /**
     * Create a new tournament based off of what the user has entered. Launches the first game in a new activity.
     * @param view A reference to the start tournament button that the user pressed.
     */
    public void CreateTournament(View view) {

        // Get Player 2's type from the spinner.
        PlayerType p2Type;
        Spinner opponentTypeSpinner = findViewById(R.id.opponentTypeSpinner);
        if (opponentTypeSpinner.getSelectedItem().toString().equals("Human")){
            p2Type = PlayerType.HUMAN;
        }
        else {
            p2Type = PlayerType.COMPUTER;
        }

        // Set player colors.
        // First, get the color that the winner selected.
        PlayerColor chosenColor = null;
        Spinner colorSpinner = findViewById(R.id.playerColorSpinner);
        if (colorSpinner != null){
            if (colorSpinner.getSelectedItem().toString().equals("White")){
                chosenColor = PlayerColor.WHITE;
            }
            else{
                chosenColor = PlayerColor.BLACK;
            }
        }

        // Handle the 4 possible cases for assigning player colors.
        PlayerColor p1Color = null;
        PlayerColor p2Color = null;
        int firstPlayer = 0;
        TextView player = findViewById(R.id.rollWinner);
        if (player != null && chosenColor != null){
            firstPlayer = Integer.parseInt(player.getText().toString());

            if (firstPlayer == 1 && chosenColor == PlayerColor.WHITE){
                p1Color = PlayerColor.WHITE;
                p2Color = PlayerColor.BLACK;
            }
            else if (firstPlayer == 1 && chosenColor == PlayerColor.BLACK){
                p1Color = PlayerColor.BLACK;
                p2Color = PlayerColor.WHITE;
            }
            else if (firstPlayer == 2 && chosenColor == PlayerColor.WHITE){
                p1Color = PlayerColor.BLACK;
                p2Color = PlayerColor.WHITE;
            }
            else {
                p1Color = PlayerColor.WHITE;
                p2Color = PlayerColor.BLACK;
            }
        }
        else {
            throw new IllegalArgumentException("Check the XML file, ids are not what they should be.");
        }

        // Get the chosen board size.
        int boardSize = 0;
        Spinner boardSizeSpinner = findViewById(R.id.boardSizeSpinner);
        if (boardSizeSpinner != null){
            boardSize = Integer.parseInt(boardSizeSpinner.getSelectedItem().toString());
        }

        // Create the players for this tournament. Player 1 will always be human.
        Player player1 = new Human(p1Color, 0);
        Player player2;
        if (p2Type == PlayerType.HUMAN){
            player2 = new Human(p2Color, 0);
        }
        else {
            player2 = new Computer(p2Color, 0);
        }

        Game thisGame = new Game(player1, player2, firstPlayer, new Board(boardSize));

        // Create the tournament. The first player will always be a human.
        Tournament thisTournament = new Tournament(PlayerType.HUMAN, 0, p2Type, 0, thisGame, 1, firstPlayer);

        // Switch to the PlayGame activity, and pass the new tournament we just created.
        Intent intent = new Intent(this, PlayGameActivity.class);
        intent.addFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("tournament", thisTournament);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
