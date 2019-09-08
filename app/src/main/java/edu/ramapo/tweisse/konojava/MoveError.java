package edu.ramapo.tweisse.konojava;

/**
 * Used to describe an error that may have occurred during a move.
 */
public enum MoveError {
    EMPTY,
    INVALID_COLOR,
    INVALID_DIRECTION,
    OCCUPIED,
    NO_CAPTURE,
    QUIT,
    NO_QUIT;

    /**
     * Gets an English representation of the MoveError. To explain to the user, for example.
     * @return A string containing an English representation of error.
     */
    @Override
    public String toString(){
        switch(this){
            case EMPTY:
                return "There is no piece at that location.";
            case OCCUPIED:
                return "You can not capture your own piece.";
            case NO_CAPTURE:
                return "This piece does not have the ability to capture.";
            case INVALID_COLOR:
                return "You can not move the opponent's piece.";
            case INVALID_DIRECTION:
                return "You cannot move off of the board.";
            case NO_QUIT:
                return "The computer would like to keep playing.";
            default:
                return "";
        }
    }
}
