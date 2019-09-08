package edu.ramapo.tweisse.konojava;

import android.graphics.Point;

/**
 * Describes a move on a board, and the reason for that move.
 */
public class Move {

    /** The location of the piece we want to move. */
    private Point m_loc;

    /** The cardinal direction of the move. */
    private MoveDirection m_dir;

    /** The action this move will perform. */
    private Action m_action;

    /** // The reasoning behind the move. */
    private ActionReason m_reason;

    /** The target that we are moving towards or away from. */
    private Point m_target;

    /**
     * Default constructor. Creates a null move.
     */
    Move(){
        SetMove(null, null, null, null, null);
    }

    /**
     * Constructor allows assignment of only a move action.
     * @param action The desired Action enum describing this move.
     */
    Move(Action action){
        SetMove(action);
    }

    /**
     * Constructor allows all but a target to be assigned.
     * @param loc The Point that this move originates from.
     * @param dir The Direction describing the direction of the move.
     * @param action The Action describing the action of this move.
     * @param reason The ActionReason describing what this move will accomplish.
     */
    Move(Point loc, MoveDirection dir, Action action, ActionReason reason) {
        SetMove(loc, dir, action, reason, null);
    }

    /**
     * Constructor allows all items to be assigned.
     * @param loc The Point that this move originates from.
     * @param dir The MoveDirection describing the direction of the move.
     * @param action The Action describing the action of this move.
     * @param reason The ActionReason describing what this move will accomplish.
     * @param target The Point that the target for the move is located on the board.
     */
    Move(Point loc, MoveDirection dir, Action action, ActionReason reason, Point target){
        SetMove(loc, dir, action, reason, target);
    }

    /**
     * Gets the location of this move.
     * @return The Point that this move originates at.
     */
    Point GetLocation(){
        return m_loc;
    }

    /**
     * Gets the direction of this move.
     * @return The MoveDirection describing the direction of the move.
     */
    MoveDirection GetDirection() {
        return m_dir;
    }

    /**
     * Gets this move's action.
     * @return The Action enum describing the action performed by this move.
     */
    Action GetAction(){
        return m_action;
    }

    /**
     * Gets the reason for this move.
     * @return The ActionReason describing the reason for playing this move.
     */
    public ActionReason GetReason(){
        return m_reason;
    }

    /**
     * Checks to see if this is a valid move.
     * @return A boolean value. True if the move is valid, false if it is not.
     */
    public boolean IsValid(){
        if (m_loc == null || m_dir == null){
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Produces a string describing the move.
     * @return A String describing the move in plain English, to display to the user.
     */
    @Override
    public String toString() {
        String desc = null;
        if (m_action == Action.PLAY){
            desc = "a move " + m_dir.toString() + " from " + m_loc.toString();
            if (m_reason != null){
                desc += (" to " + m_reason.GetReason());
            }

            if (m_target != null){
                desc += (" at " + m_target.toString() + ".");
            }
            else {
                desc += ".";
            }
        }
        else if (m_action == Action.QUIT){
            desc = "quits the game.";
        }
        return desc;
    }

    /**
     * Sets only the action of a move.
     * @param action The Action enum describing the action to set for this move.
     */
    void SetMove(Action action){
        SetMove(null, null, action, null, null);
    }

    /**
     * Sets all items of a move.
     * @param loc The Point that this move starts at.
     * @param dir The MoveDirection that this move should go.
     * @param action The Action describing the move type.
     * @param reason The ActionReason describing the reason for playing this move.
     * @param target The Point describing the position of the target of this move.
     */
    void SetMove(Point loc, MoveDirection dir, Action action, ActionReason reason, Point target){
        m_loc = loc;
        m_dir = dir;
        m_action = action;
        m_reason = reason;
        m_target = target;
    }

    public static void main(String args[]){
    }
}
