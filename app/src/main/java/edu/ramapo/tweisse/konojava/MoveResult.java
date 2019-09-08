package edu.ramapo.tweisse.konojava;

/**
 * Holds information about the result of playing a move on a board.
 */
public class MoveResult {

    private int m_points;       /** The number of points received by the player from this move. */
    private MoveError m_error;  /** The error that may have occurred during this move. */

    /**
     * Default constructor. Constructs a MoveResult with no points and no error.
     */
    MoveResult(){
        this(0, null);
    }

    /**
     * Builds a MoveResult object based on number of points returned and an error.
     * @param points An int holding the number of points that the played should receive from the move.
     * @param error A MoveError enum holding the error that occurred trying to play the move.
     */
    MoveResult(int points, MoveError error){
        m_points = points;
        m_error = error;
    }

    /**
     * Gets the number of points gained on the move.
     * @return An int holding the number points earned playing the move.
     */
    int GetPoints(){
        return m_points;
    }

    /**
     * Gets the error that occurred during the move.
     * @return A MoveError object holding the error that occurred trying to play the move.
     */
    MoveError GetError(){
        return m_error;
    }

    public static void main(String args[]){
//        MoveResult testMoveResult;
//        testMoveResult = new MoveResult();
//        testMoveResult = new MoveResult(5, null);
//        testMoveResult = new MoveResult(5, MoveError.EMPTY);
//
//        System.out.println(testMoveResult.GetError());
//        System.out.println(testMoveResult.GetPoints());
    }
}
