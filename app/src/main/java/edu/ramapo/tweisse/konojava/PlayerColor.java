package edu.ramapo.tweisse.konojava;

/**
 * Describes the color of a Player or Piece object.
 */
public enum PlayerColor {
    WHITE,
    BLACK,
    BOTH;

    /**
     * Finds the opponent to a given color.
     * @param player The PlayerColor of the Player we want to find the opponent for.
     * @return A PlayerColor holding the opposite color to what was passed.
     */
    public static PlayerColor Opponent(PlayerColor player){
        switch(player){
            case WHITE:
                return BLACK;
            case BLACK:
                return WHITE;
            default:
                return null;
        }
    }
}