package edu.ramapo.tweisse.konojava;

import android.os.Environment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Random;

/**
 * Holds all necessary data and functions to play a tournament consisting of one or more games.
 */
public class Tournament implements Serializable {

    /** A structure holding information about the players in this tournament. */
    private class TournamentPlayer implements Serializable {
        /** The type of this player. */
        public PlayerType m_type;

        /** The score of this player. */
        public int m_score;

        /**
         * Default constructor. Creates a TournamentPlayer with no type and no score.
         */
        TournamentPlayer(){
            this(null, 0);
        }

        /**
         * Constructs a TournamentPlayer of a given type and with given score.
         * @param type The PlayerType of this player.
         * @param score The score that this player should start with.
         */
        TournamentPlayer(PlayerType type, int score){
            m_type = type;
            m_score = score;
        }
    }

    /** Holds the 2 players playing this tournament. */
    private TournamentPlayer m_players[];

    /** The current Game object being played in this tournament. */
    private Game m_currentGame;

    /** The number of rounds we have played in this tournament. */
    private int m_roundNum;

    /** The number of the first player to move in the current game in this tournament. */
    private int m_nextPlayer;

    /** Has the current game ended yet? */
    private boolean m_gameOver;

    /** If this string is null, random numbers will be generated for dice rolls. If it is not, dice
     * rolls will be taken from the file contained in the string if it exists. */
    private static String m_diceFile = "diceRolls.txt";

    /** An array holding the dice rolls that were read from the m_diceFile file. */
    private static int m_diceFileRolls[] = new int[]{};

    /** Used by the dice rolling function to keep track of where in the dice rolls array we are. */
    private static int m_rollPos = 0;

    /**
     * Default constructor. Creates a tournament with null players playing a null game.
     */
    Tournament(){
        this(null, 0, null, 0, null, -1, -1);
    }

    /**
     * Constructs a custom Tournament object.
     * @param p1Type The PlayerType for player 1.
     * @param p1Score The score player 1 should start with.
     * @param p2Type The PlayerType for Player 2.
     * @param p2Score The score player 2 should start with.
     * @param game The Game object that this tournament will start playing first.
     * @param round The round number this tournament should start on.
     * @param nextPlayer The first player to play the current game. 1 or 2.
     */
    Tournament(PlayerType p1Type, int p1Score, PlayerType p2Type, int p2Score, Game game, int round, int nextPlayer){

        // Assign players.
        m_players = new TournamentPlayer[2];
        m_players[0] = new TournamentPlayer(p1Type, p1Score);
        m_players[1] = new TournamentPlayer(p2Type, p2Score);

        // Assign game data.
        m_currentGame = game;
        m_roundNum = round;
        m_nextPlayer = nextPlayer;

        m_gameOver = false;
    }

    /**
     * Gets the player type for a given player.
     * @param i The player number we want to get the type of. 1 or 2.
     * @return The PlayerType of player i.
     */
    public PlayerType GetPlayerType(int i){
        // Check that the index is in range.
        if (i != 1 && i != 2){
            throw new IllegalArgumentException("Invalid player number.");
        }
        // Get the correct player.
        return m_players[i - 1].m_type;
    }

    /**
     * Gets the player score for a given player.
     * @param i the player number we want to get the type of. 1 or 2.
     * @return The PlayerType of player i.
     */
    public int GetPlayerScore(int i){
        // Check that the index is in range.
        if (i != 1 && i != 2){
            throw new IllegalArgumentException("Invalid player number.");
        }
        // Get the correct player.
        return m_players[i - 1].m_score;
    }

    /**
     * Gets the number of the current game winner, and updates points accordingly
     * @return An integer holding the number of the player who won the current game. 1, 2, 0 if a tie, or -1 if no winner yet.
     */
    public int GetGameWinner(){
        boolean isWinner = m_currentGame.IsWinner();
        // If we have a winner, return the number and update scores.
        if (isWinner){
            // Update the tournament scores.
            int p1Pts = m_currentGame.GetPlayer(1).GetPoints();
            int p2Pts = m_currentGame.GetPlayer(2).GetPoints();

            int winnerNum;
            if (p1Pts > p2Pts){
                winnerNum = 1;
            }
            else if (p1Pts < p2Pts){
                winnerNum = 2;
            }
            else {
                return 0;
            }
            // Add points if the game is not over.
            if (!m_gameOver){
                m_players[winnerNum - 1].m_score += Math.abs(p1Pts - p2Pts);
                m_gameOver = true;
            }
            return winnerNum;
        }
        else {
            return -1;
        }
    }

    /**
     * Gets the winner of this tournament.
     * @return 1 if player 1 has won. 2 if player 2 has won. 0 if it is a tie.
     */
    public int GetTournamentWinner(){
        if (m_players[0].m_score > m_players[1].m_score){
            return 1;
        }
        else if (m_players[0].m_score < m_players[1].m_score){
            return 2;
        }
        else {
            // Return 0 if a tie occurred.
            return 0;
        }
    }

    /**
     * Gets the current round number for this tournament.
     * @return An integer holding the round number we are currently on in the tournament.
     */
    public int GetRoundNum(){
        return m_roundNum;
    }

    /**
     * Gets the inner Game object in order to make moves, etc.
     * @return The Game object currently being played in the tournament.
     */
    public Game GetGame(){
        return m_currentGame;
    }

    /**
     * Gets the next player to play first.
     * @return An integer holding the number of the first player to move in the current game.
     */
    public int GetNextPlayer(){
        return m_nextPlayer;
    }

    /**
     * Updates the game for when a new round starts.
     * @param game The new Game object we would like to start playing in this tournament.
     */
    public void SetNewGame(Game game){
        m_currentGame = game;
        // Reset the game over indicator.
        m_gameOver = false;
    }

    /**
     * Provides a single dice roll value, either randomly generated or from the provided file.
     * @return An integer holding a single random dice roll value.
     */
    public static int ThrowDice(){
        // If there is no file provided, roll the dice randomly.
        if (m_diceFile == null){
            // Generate a dice roll between 1 and 6.
            Random rand = new Random();
            int roll = rand.nextInt(6) + 1;
            return roll;
        }
        else{
            if (m_diceFileRolls.length == 0){
                try{
                    // Get the sd card directory.
                    File sdcard = Environment.getExternalStorageDirectory();
                    File file = new File(sdcard, m_diceFile);

                    if (file.exists()){
                        // Open the file up.
                        FileReader stream = new FileReader(file);
                        BufferedReader buff = new BufferedReader(stream);

                        // Read every line from the file.
                        String line;
                        while ((line = buff.readLine()) != null) {
                            // Split our line into an array of individual words.
                            String words[] = line.trim().split("\\s+");

                            // Skip blank lines, add lines with rolls in them to the array.
                            if (words.length == 0){
                                continue;
                            }
                            if (words.length == 2){
                                int tmpArray[] = new int[m_diceFileRolls.length + 2];
                                // Expand the array.
                                for (int i = 0; i < m_diceFileRolls.length; i++){
                                    tmpArray[i] = m_diceFileRolls[i];
                                }
                                tmpArray[tmpArray.length - 2] = Integer.parseInt(words[0]);
                                tmpArray[tmpArray.length - 1] = Integer.parseInt(words[1]);

                                m_diceFileRolls = tmpArray;
                            }
                        }
                    }
                }
                catch (IOException e){
                    m_diceFile = null;
                }
                catch (Exception e){
                    m_diceFile = null;
                }
            }
            else {
                // Get the next roll and update which roll we are on.
                int thisRoll = m_diceFileRolls[m_rollPos];
                if (m_rollPos < m_diceFileRolls.length - 1){
                    m_rollPos += 1;
                }
                else {
                    m_rollPos = 0;
                }
                return thisRoll;
            }
        }
        // We either loaded data or there was an error. Either way, call recursively to get a result.
        return ThrowDice();
    }
}
