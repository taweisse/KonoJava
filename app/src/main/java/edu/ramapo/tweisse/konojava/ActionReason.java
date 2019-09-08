package edu.ramapo.tweisse.konojava;

/**
 * Describes the reason a given move was played.
 */
public enum ActionReason {
    RANDOM,
    ADVANCE,
    CAPTURE,
    BLOCK,
    ESCAPE;

    /**
     * Gets the weight or 'importance' of this reason. Used to compare 2 reasons for priority.
     * @return An integer describing the relative weight of the ActionReason.
     */
    public int GetWeight(){
        switch(this){
            case RANDOM:
                return 0;
            case ADVANCE:
                return 1;
            case CAPTURE:
                return 2;
            case BLOCK:
                return 3;
            case ESCAPE:
                return 4;
            default:
                return -1;
        }
    }

    /**
     * Gets an explanation of the reason for the move as a string.
     * @return A string containing the reason written in plain English.
     */
    public String GetReason(){
        switch(this){
            case RANDOM:
                return "to continue the game";
            case ADVANCE:
                return "to advance towards the opponent's home location";
            case CAPTURE:
                return "to capture the opponent";
            case BLOCK:
                return "to block the opponent";
            case ESCAPE:
                return "to escape capture from the opponent";
            default:
                return "";
        }
    }
}

