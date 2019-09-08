package edu.ramapo.tweisse.konojava;

/**
 * A subclass of Player. Represents a human player of a game.
 */
public class Human extends Player {

    /**
     * Default constructor. Creates a new Human with no color or points.
     */
    Human(){
        this (null, 0);
    }

    /**
     * Initialize a player with a given color and points.
     * @param color The PlayerColor that this player should have.
     * @param points The number of points that this player should start with.
     */
    Human(PlayerColor color, int points){
        m_color = color;
        m_points = points;
    }

    /**
     * Gets the type of this player.
     * @return The HUMAN PlayerType, since this is a human object.
     */
    public PlayerType GetType(){
        return PlayerType.HUMAN;
    }

    /**
     * Since the human player will not need to pick its own move, simply return what it was passed.
     * @param move A Move object describing the move we may play.
     * @param board A Board object holding the board we will use to pick a move.
     * @return The same move that was passed. The human does not need help from the AI system to get a move.
     */
    @Override
    public Move PrePlay(Move move, Board board){
        return move;
    }
}
