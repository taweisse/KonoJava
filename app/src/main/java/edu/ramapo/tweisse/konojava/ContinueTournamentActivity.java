package edu.ramapo.tweisse.konojava;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Launched when a game ends. Allows the user to crate a new game within an existing tournament.
 */
public class ContinueTournamentActivity extends AppCompatActivity {

    /** Used to tell the tournament and the game who moved first this game. */
    private int m_nextPlayer;

    /** The Tournament object that we were just playing. We need to swap the game out in this. */
    private Tournament m_lastTourn;

    /**
     * Prompts the user before going back. I.E. exiting the current tournament.
     */
    @Override
    public void onBackPressed(){
        // Show the user an alert box telling them the tournament progress will be lost.
        AlertDialog.Builder backAlert = new AlertDialog.Builder(this);
        backAlert.setCancelable(false);
        backAlert.setTitle("Go Back?");
        backAlert.setMessage("Leaving this screen will cause tournament progress to be lost. Would you like to continue?");
        backAlert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Send the user back to the home page.
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });
        backAlert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing.
            }
        });
        backAlert.setIcon(android.R.drawable.ic_dialog_alert);
        backAlert.show();
    }

    /**
     * Fill various spinners with the proper values for the user to select.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_continue_tournament);

        // Fill the board sizes spinner.
        String boardSizes[] = {"5", "7", "9"};
        Spinner boardSizeSpinner = findViewById(R.id.boardSizeSpinner);
        ArrayAdapter<String> adapter0 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, boardSizes);
        adapter0.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        boardSizeSpinner.setAdapter(adapter0);

        // Fill the player color spinner.
        String playerColors[] = {"White", "Black"};
        Spinner playerColorSpinner = findViewById(R.id.playerColorSpinner);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, playerColors);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        playerColorSpinner.setAdapter(adapter2);

        // Describe the result of the last game to the user.
        m_lastTourn = (Tournament)getIntent().getSerializableExtra("tournament");
        if (m_lastTourn != null){

            String message;

            // Save who the first player was last game in case the tournament was a tie. He will go first again if this is the case.
            int firstPlayer = m_lastTourn.GetNextPlayer();

            int lastWinner = m_lastTourn.GetGameWinner();
            if (lastWinner == 0){
                message = String.format("The last game ended in a tie. Player %d will move first.", firstPlayer);
                m_nextPlayer = firstPlayer;
            }
            else {
                message = String.format("Player %d won the last game and will move first.", lastWinner);
                m_nextPlayer = lastWinner;
            }

            // Describe the result of last game.
            TextView desc = findViewById(R.id.lastTournMsg);
            if (desc != null){
                desc.setText(message);
            }

            // Show which user needs to choose a color.
            TextView pickColorInfo = findViewById(R.id.pickColorPlayer);
            if (pickColorInfo != null){
                pickColorInfo.setText(Integer.toString(m_nextPlayer));
            }

            // The computer player should automatically pick a color.
            if (m_lastTourn.GetPlayerType(m_nextPlayer) == PlayerType.COMPUTER){
                Spinner colorSpinner = findViewById(R.id.playerColorSpinner);
                playerColorSpinner.setSelection(0); // Choose the first item in the spinner. (White)
                playerColorSpinner.setEnabled(false);
            }
        }
    }

    /**
     * Starts a new game in the tournament that was passed to the activity.
     * @param view A reference to the new game button that was pressed.
     */
    public void NewGame(View view){
        // Get the previous game because we will need data from it.
        Game oldGame = m_lastTourn.GetGame();

        PlayerType p2Type = m_lastTourn.GetPlayerType(2);

        // Figure out which color each player should get depending on what the user selected.
        PlayerColor p1Color = null;
        PlayerColor p2Color = null;
        Spinner playerColor = findViewById(R.id.playerColorSpinner);
        if (playerColor != null){
            String choice = playerColor.getSelectedItem().toString();
            if (m_nextPlayer == 1 && choice.equals("White")){
                p1Color = PlayerColor.WHITE;
                p2Color = PlayerColor.BLACK;
            }
            else if (m_nextPlayer == 1 && choice.equals("Black")){
                p1Color = PlayerColor.BLACK;
                p2Color = PlayerColor.WHITE;
            }
            else if (m_nextPlayer == 2 && choice.equals("White")){
                p1Color = PlayerColor.BLACK;
                p2Color = PlayerColor.WHITE;
            }
            else{
                p1Color = PlayerColor.WHITE;
                p2Color = PlayerColor.BLACK;
            }
        }

        // Create the players for the new game.
        Player newP1 = new Human(p1Color, 0);
        Player newP2;
        if (p2Type == PlayerType.HUMAN){
            newP2 = new Human(p2Color, 0);
        }
        else {
            newP2 = new Computer(p2Color, 0);
        }

        // Get the board size that the user entered.
        int boardSize = 0;
        Spinner boardSizeSpinner = findViewById(R.id.boardSizeSpinner);
        if (boardSizeSpinner != null){
            boardSize = Integer.parseInt(boardSizeSpinner.getSelectedItem().toString());
        }

        // Create the new game.
        Game newGame = new Game(newP1, newP2, m_nextPlayer, new Board(boardSize));
        m_lastTourn.SetNewGame(newGame);

        // Switch to the PlayGame activity, and pass the new tournament we just created.
        Intent intent = new Intent(this, PlayGameActivity.class);
        intent.addFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("tournament", m_lastTourn);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
