package edu.ramapo.tweisse.konojava;

/**
 * Child of the Player class. Represents a computer player that can use the AI system to find moves.
 */
public class Computer extends Player {

    /**
     * Default constructor. Creates a Computer object with no color or points.
     */
    Computer(){
        this(null, 0);
    }

    /**
     * Initialize a player with a color and number of points.
     * @param color The PlayerColor that this Computer should be.
     * @param points The number of points that this Computer should start with.
     */
    Computer(PlayerColor color, int points){
        m_color = color;
        m_points = points;
    }

    /**
     * Get the type of this player.
     * @return COMPUTER PlayerType, since this is a computer player.
     */
    public PlayerType GetType(){
        return PlayerType.COMPUTER;
    }
}
