package edu.ramapo.tweisse.konojava;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity handles playing a game from start to finish. It allows both players to make moves
 * one after the other until someone wins.
 */
public class PlayGameActivity extends AppCompatActivity implements View.OnClickListener {

    /** The colors used for displaying the board. */
    private static final int BOARD_COLOR = 0xFFd8d8d8;
    private static final int SELECTED_COLOR = 0xFFc1ffc6;
    private static final int WHITE_COLOR = 0xFFffffff;
    private static final int BLACK_COLOR = 0xFF000000;
    private static final int VALUE_COLOR = 0xFFaaaaaa;

    /** The current tournament that we are playing. Holds the current game. */
    private Tournament m_tournament;

    /** The BoardView class used to display the board on the screen. */
    private BoardView m_boardView;

    /** The board size of the game we are playing. Stored because we reference it often. */
    private int m_boardSize;

    /** Holds the starting location of a possible human move. */
    private Point m_fromPoint;

    /** Holds the target position of a possible human move. */
    private Point m_toPoint;

    /** Holds the direction of a possible human move. */
    private MoveDirection m_moveDirection;

    /**
     * Prompt the user before going back. I.E. exiting the current tournament.
     */
    @Override
    public void onBackPressed(){
        // Alert the user that they will loose progress if they go back to the home screen.
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
     * Removes the title bar for this activity, and configures button colors and value initialization.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove the title bar.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_play_game);

        // Change the color of the computer move button and hide it by default.
        Button computerMoveButton = findViewById(R.id.computerMoveBtn);
        computerMoveButton.getBackground().setColorFilter(SELECTED_COLOR, PorterDuff.Mode.MULTIPLY);
        computerMoveButton.setVisibility(View.INVISIBLE);

        // Retrieve the serialized Tournament object passed in the intent.
        Intent intent = getIntent();
        m_tournament = (Tournament)intent.getSerializableExtra("tournament");
        m_boardSize = m_tournament.GetGame().GetBoard().GetSize(); // Assign to m_boardSize for later.
        m_boardView = new BoardView(this);

        // Initialize values when this activity starts.
        m_fromPoint = null;
        m_toPoint = null;
        m_moveDirection = null;

        // Create the necessary board UI elements.
        m_boardView.CreateBoard(R.id.boardGrid, m_boardSize);

        // Draw the board.
        UpdateView();
    }

    /**
     * Handle any of our buttons being pressed.
     * @param view The View element which was pressed on the screen.
     */
    public void onClick(View view) {
        int clickedId = view.getId();
        // Test if the button that was clicked was on the board.
        if (clickedId >= 0 && clickedId <= m_boardSize * m_boardSize) {
            BoardPress(clickedId, view);
        }

        // Handle the help button being pressed.
        else if (clickedId == R.id.helpBtn){
            HelpPress();
        }

        // Execute the computer move.
        else if (clickedId == R.id.computerMoveBtn){
            if (m_tournament.GetGame().GetPlayer(m_tournament.GetGame().GetNextPlayer()).getClass() == Computer.class){
                PlayMove(null);
                UpdateView();
            }
        }

        // Handle the quit button being pressed.
        else if (clickedId == R.id.quitBtn) {
            QuitPress();
        }

        // Handle the save button being pressed.
        else if (clickedId == R.id.saveBtn) {
            SavePress();
        }
    }

    /**
     * Writes a message to the on screen game log.
     * @param text The String we want to print to the log.
     */
    private void WriteToLog(String text){
        // Find the log TextView and append to its text.
        TextView log = findViewById(R.id.gameRecord);
        if (log != null){
            log.append("\n" + text);
        }
    }

    /**
     * Executes a move on the board. The Game object takes care of who plays next, etc.
     * @param move The Move object describing the move that we want to play.
     */
    private void PlayMove(Move move){
        Move chosenMove = m_tournament.GetGame().PrePlay(move);
        MoveError err = m_tournament.GetGame().Play(chosenMove);

        int lastPlayer = m_tournament.GetGame().GetNextPlayer() == 1 ? 2 : 1;

        if (err == null){
            // Print the move to the console.
            WriteToLog("Player " + lastPlayer + " executes " + chosenMove.toString() + "\n");
        }
        else if (err == MoveError.QUIT){
            WriteToLog("Player " + lastPlayer + " quits the game. \n");
        }
        else {
            // Tell the user what went wrong with the move.
            DisplayError(err);
        }
        // Update the view.
        UpdateView();

        // See if someone won this game.
        CheckForWinner();
    }

    /**
     * Handles a press on the board. IE the user wants to make a move.
     * @param clickedId The ID of the button that was pressed.
     * @param view The Button reference that was pressed.
     */
    private void BoardPress(int clickedId, View view){
        // If it is the computer's turn, the user shouldn't be able to click anything.
        if (m_tournament.GetGame().GetPlayer(m_tournament.GetGame().GetNextPlayer()).getClass() == Computer.class){
            return;
        }

        // Otherwise, get the button that was clicked.
        Button clickedButton = (Button)view;

        // If this is the first point the user is clicking, set it as the target location.
        if (m_fromPoint == null) {
            // See if the clicked cell has the correct color player in it for this turn.
            Point clickPoint = BoardView.NumberToPoint(clickedId, m_boardSize);
            if(m_tournament.GetGame().GetBoard().GetOccupantColor(clickPoint) == m_tournament.GetGame().GetPlayer(m_tournament.GetGame().GetNextPlayer()).m_color){
                m_fromPoint = clickPoint;

                // Change button color to indicate it is selected.
                clickedButton.getBackground().setColorFilter(SELECTED_COLOR, PorterDuff.Mode.MULTIPLY);
            }
        }
        // If there is already a starting point, the user can either click another point, or the same.
        // If the user clicks the same point, reset it to null so it can be changed.
        else if (m_fromPoint.equals(BoardView.NumberToPoint(clickedId, m_boardSize))) {
            m_fromPoint = null;
            // Change the color back to white since we are un-selecting.
            UpdateView();
        }
        // If the user clicks another button, set this as the target for the move.
        else {
            m_toPoint = BoardView.NumberToPoint(clickedId, m_boardSize);
            // Verify that a valid target was clicked. If not, reset the target to null.
            m_moveDirection = GetMoveDirection(m_fromPoint, m_toPoint);
            if (m_moveDirection == null) {
                m_toPoint = null;
            }
            // If a valid target got clicked, execute the move and update the board.
            else {
                // Reset color of the start location button.
                Button startButton = findViewById(PointToNumber(m_fromPoint));
                if (startButton != null) {
                    startButton.getBackground().setColorFilter(BOARD_COLOR, PorterDuff.Mode.MULTIPLY);
                }

                // Create a new move based on the board cells the user has entered.
                Move thisMove = new Move(m_fromPoint, m_moveDirection, Action.PLAY, null);
                if (thisMove.IsValid()) {
                    // Attempt the move.
                    PlayMove(thisMove);
                }
                // Reset local variables in preparation for the next move and update the display.
                m_fromPoint = null;
                m_toPoint = null;
                m_moveDirection = null;
            }
        }
    }

    /**
     * Handles a quit button press. Quits the current game if the user wants to.
     */
    private void QuitPress(){
        // Tell the user that they will loose 5 points if they quit.
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setCancelable(false);
        alert.setTitle("Quit?");
        alert.setMessage("If you quit now, you will loose 5 points. Continue?");
        alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Quit the game.
                PlayMove(new Move(Action.QUIT));
            }
        });
        alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing.
            }
        });
        alert.setIcon(android.R.drawable.ic_dialog_alert);
        alert.show();
    }

    /**
     *  Handles a save button press.
     */
    private void SavePress(){
        // Alert the user that the app will exit after saving if they continue.
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setCancelable(false);
        alert.setTitle("Save and quit now?");
        alert.setMessage("If you continue the current tournament will be saved and the application will exit.");
        alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SaveGame();
            }
        });
        alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing.
            }
        });
        alert.setIcon(android.R.drawable.ic_dialog_alert);
        alert.show();
    }

    /**
     * Handles a help button press. Displays a move suggestion to the user based on the computer AI.
     */
    private void HelpPress(){
        // Verify that it is the human player's turn currently.
        Player curPlayer = m_tournament.GetGame().GetPlayer(m_tournament.GetGame().GetNextPlayer());
        if (curPlayer.getClass() == Human.class){

            // Call the base class move finding function to get an AI move.
            final Move suggestion = curPlayer.FindBestMove(m_tournament.GetGame().GetBoard());

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Help");
            alert.setMessage("The computer suggests " + suggestion.toString() + " Would you like to play this move?");
            alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Play the move that the computer suggested.
                    PlayMove(suggestion);
                }
            });
            alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing.
                }
            });
            alert.setIcon(android.R.drawable.ic_dialog_alert);
            alert.show();
        }
    }

    /**
     * Saves the game in a new activity.
     */
    private void SaveGame(){
        // Launch the save activity and pass the current tournament.
        Intent intent = new Intent(this, SaveTournamentActivity.class);
        intent.putExtra("tournament", m_tournament);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * Display the end of tournament information to the user and return to the home screen.
     */
    private void EndTournament(){
        // Figure out who won and what their ending scores were.
        int winner = m_tournament.GetTournamentWinner();
        int p1Score = m_tournament.GetPlayerScore(1);
        int p2Score = m_tournament.GetPlayerScore(2);

        // Build a message describing who won to display to the user.
        String header = String.format("Player %d wins the tournament!", winner);
        String message = String.format("Player 1 scored %d overall. Player 2 scored %d. Thanks for playing!", p1Score, p2Score);

        // Show the user who won.
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setCancelable(false);
        alert.setTitle(header);
        alert.setMessage(message);
        alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Return to home screen.
                finish();
            }
        });
        alert.setIcon(android.R.drawable.ic_dialog_alert);
        alert.show();
    }

    /**
     * Launches the continue tournament activity set up parameters to continue playing another game.
     */
    private void ContinueTournament(){
        // Launch the continue tournament activity and pass the current tournament.
        Intent intent = new Intent(this, ContinueTournamentActivity.class);
        intent.putExtra("tournament", m_tournament);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    /**
     * Updates the screen based on the state of the Game object.
     */
    private void UpdateView(){

        // Update the board display.
        m_boardView.UpdateBoard(m_tournament.GetGame().GetBoard(), BOARD_COLOR, WHITE_COLOR, BLACK_COLOR, VALUE_COLOR);

        // Update the points and players fields under the board.
        TextView p1Score = findViewById(R.id.p1ScoreValue);
        TextView p2Score = findViewById(R.id.p2ScoreValue);
        TextView nextUp = findViewById(R.id.nextUpValue);
        ScrollView logScroller = findViewById(R.id.logScroller);

        if (p1Score != null){
            p1Score.setText(Integer.toString(m_tournament.GetGame().GetPlayer(1).GetPoints()));
        }
        if (p2Score != null){
            p2Score.setText(Integer.toString(m_tournament.GetGame().GetPlayer(2).GetPoints()));
        }
        if (nextUp != null){
            int nextPlayer = m_tournament.GetGame().GetNextPlayer();
            nextUp.setText("Player ");
            nextUp.append(Integer.toString(nextPlayer));

            // If the next player is the computer, show the move button for the user.
            Button computerMoveButton = findViewById(R.id.computerMoveBtn);
            Button helpButton = findViewById(R.id.helpBtn);
            if (computerMoveButton != null && helpButton != null){
                if (m_tournament.GetGame().GetPlayer(nextPlayer).getClass() == Computer.class){

                    computerMoveButton.setVisibility(View.VISIBLE);
                    helpButton.setEnabled(false);
                }
                else {
                    computerMoveButton.setVisibility(View.INVISIBLE);
                    helpButton.setEnabled(true);
                }
            }
        }
    }

    /**
     * Checks for a winner in the current game.
     */
    private void CheckForWinner(){
        // Determine if there was a winner to this game on the last move.
        int winner = m_tournament.GetGameWinner();
        if (winner != -1){
            DisplayWinner(winner);
        }
    }

    /**
     * Displays the winner of the current game, and asks the user if they would like to play another game.
     * @param win The number of the player who won the current game. 1 or 2, or 0 if the game is a tie.
     */
    private void DisplayWinner(int win){

        String title = null;
        String message = null;

        // If the game is not a tie, create the message telling who won.
        if (win == 1 || win == 2){
            int lose = win == 1 ? 2 : 1;

            int winPts = m_tournament.GetGame().GetPlayer(win).GetPoints();
            int losePts = m_tournament.GetGame().GetPlayer(lose).GetPoints();

            title = String.format("Player %d wins!", win);
            message = String.format("Player %d scored %d. Player %d scored %d. Player %d will earn %d points this round. Would you like to play another round?", win, winPts, lose, losePts, win, winPts - losePts);
        }
        // If the game is a tie, create a generic tie message.
        else if (win == 0){
            title = "Tie!";
            message = "The game was a tie! Neither player will earn points this round. Would you like to play another round?";
        }

        // Show the winner in an alert box to the user.
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setCancelable(false);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ContinueTournament();
            }
        });
        alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Show the results of the tournament.
                EndTournament();
            }
        });
        alert.setIcon(android.R.drawable.ic_dialog_alert);
        alert.show();
    }

    /**
     * Displays the error which resulted from a move.
     * @param err The MoveError object describing the outcome of the move we want to show the user.
     */
    private void DisplayError(MoveError err){
        if (err == null){
            return;
        }
        else {
            // Display the error that occurred on the screen.
            Toast errDisplay = Toast.makeText(this, err.toString(), Toast.LENGTH_SHORT);
            errDisplay.show();

            // Also write the error to the log.
            WriteToLog(err.toString() + "\n");
        }
    }

    /**
     * Converts a Point to a 1-D zero indexed array.
     * @param pt The Point holding a location on the current game's board.
     * @return An int holding a zero indexed array position representing the point on the board.
     */
    private int PointToNumber(Point pt){
        return ((pt.x - 1) * m_boardSize + pt.y - 1);
    }

    /**
     * Finds the direction of the move given a current and target location, if the target is a neighbor.
     * @param start The starting Point.
     * @param end The ending Point.
     * @return The MoveDirection describing what direction would result in a move from the start point to the end point.
     */
    private MoveDirection GetMoveDirection(Point start, Point end){
        // NW Move.
        if (start.x - 1 == end.x && start.y - 1 == end.y){
            return MoveDirection.NW;
        }
        // NE Move.
        else if (start.x - 1 == end.x && start.y + 1 == end.y){
            return MoveDirection.NE;
        }
        // SE Move.
        else if (start.x + 1 == end.x && start.y + 1 == end.y){
            return MoveDirection.SE;
        }
        // SW Move.
        else if (start.x + 1== end.x && start.y - 1 == end.y){
            return MoveDirection.SW;
        }
        // The target wasn't a valid neighbor.
        else {
            return null;
        }
    }
}
