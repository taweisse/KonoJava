package edu.ramapo.tweisse.konojava;

import java.io.Serializable;

/**
 * Holds information about a single game piece on a board.
 */
public class Piece implements Serializable {

    /** The color of this piece */
    private PlayerColor m_color;

    /** Does this piece have the ability to capture? */
    private boolean m_canCapture;

    /**
     * Default constructor. Constructs a Piece with null color without capture ability.
     */
    Piece(){
        this(null);
    }

    /**
     * Construct a Piece object with a specific color.
     * @param color A PlayerColor holding color that this piece should be.
     */
    Piece(PlayerColor color){
        m_color = color;
        m_canCapture = false;
    }

    /**
     * Gets the color of this piece.
     * @return A PlayerColor enum holding the color of this Piece.
     */
    PlayerColor GetColor(){
        return m_color;
    }

    /**
     * Gets this piece's ability to capture.
     * @return A boolean value. True if this piece can capture. False if not.
     */
    boolean CanCapture(){
        return m_canCapture;
    }

    /**
     * Gets weather or not this piece contains a player.
     * @return A boolean value. True if the piece belongs to a player. False if not.
     */
    boolean IsEmpty(){
        if (m_color == null){
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Upgrades this piece eo allow it to capture opponents.
     */
    void AllowCapture(){
        m_canCapture = true;
    }

    public static void main(String args[]){
    }
}
