package edu.ramapo.tweisse.konojava;

import java.io.Serializable;

/**
 * Holds information about a game of Kono. Players and a board are stored here.
 */
public class Game implements Serializable {

    /** Holds the two player objects for this game. */
    private Player m_players[];

    /** The board object for this game. */
    private Board m_gameboard;

    /** The current player in the array. */
    private int m_curPlayer;

    /** Stores the winner in case someone quits the game. */
    private PlayerColor m_winner;

    /**
     * Default constructor. Initializes a null Game.
     */
    Game() {
        // Initialize the player array to 2 null players by default.
        this(null, null, 1, null);
    }

    /**
     * Constructor creates a new custom Game object.
     * @param p1 The Player object to represent player 1.
     * @param p2 The Player object to represent player 2.
     * @param curPlayer An integer holding who the first player up will be.
     * @param board The Board object that this game will be played on.
     */
    Game(Player p1, Player p2, int curPlayer, Board board){
        m_players = new Player[2];
        m_players[0] = p1;
        m_players[1] = p2;
        m_gameboard = board;
        if (curPlayer < 1 || curPlayer > 2){
            throw new IllegalArgumentException("Invalid player number.");
        }
        m_curPlayer = curPlayer - 1;
        m_winner = null;

        m_players[0].m_points = m_gameboard.GetPoints(m_players[0].m_color);
        m_players[1].m_points = m_gameboard.GetPoints(m_players[1].m_color);
    }

    /**
     * Gets the player at the given player number ordinal.
     * @param num The number of the player that we want to retrieve.
     * @return The Player object of the given number.
     */
    public Player GetPlayer(int num){
        if (num < 1 || num > 2){
            throw new IllegalArgumentException("Invalid player number.");
        }
        return m_players[num - 1];
    }

    /**
     * Gets the board object used in this game.
     * @return The Board object used by this game.
     */
    public Board GetBoard(){
        return m_gameboard;
    }

    /**
     * Gets the number of the player to make the next move in this game.
     * @return An int holding the next player to make a move.
     */
    public int GetNextPlayer(){
        return m_curPlayer + 1;
    }

    /**
     * Decides if someone has won the game yet or not.
     * @return A boolean value. True if the game has a winner, false if not.
     */
    public boolean IsWinner() {
        PlayerColor winner = m_winner;
        if (m_winner == null){
            winner = m_gameboard.GetWinner();
        }
        if (winner != null){
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Calls the PrePlay() function for whatever player is up next.
     * @param move The Move that we will pass to the player's PrePlay() function.
     * @return The Move object returned from the player's PrePlay() function.
     */
    public Move PrePlay(Move move){
        // Call PrePlay() on whichever player is up next, and return the move.
        return m_players[m_curPlayer].PrePlay(move, m_gameboard);
    }

    /**
     * Attempts to play a given Move object for the next player's turn.
     * @param move The Move that we want to play for this player's turn.
     * @return The MoveError describing the result of playing the move.
     */
    public MoveError Play(Move move){
        // Attempt to play the move for whichever player is up next.
        MoveError result = m_players[m_curPlayer].Play(move, m_gameboard);
        if (result == null || result == MoveError.QUIT){
            // If the move was successful, switch the turn to the next player.
            m_curPlayer = m_curPlayer == 0 ? 1 : 0;

            // If a player quits, set the winner to the other player.
            if (result == MoveError.QUIT){
                m_winner = m_players[m_curPlayer].GetColor();
            }
        }
        return result;
    }

    public static void main(String args[]){
    }
}
